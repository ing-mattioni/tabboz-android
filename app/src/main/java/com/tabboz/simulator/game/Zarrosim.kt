@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// =============================================================================
// zarrosim.c + newproteggi.c
// Tabboz Simulator - Copyright (c) 1997-2000 Andrea Bonomi
// Traduzione meccanica dal C: stessi nomi, stessi testi, stessa logica.
// =============================================================================

// --- static locali dei file C (vedi guida al porting) ------------------------

/** `static char tmpsesso` di FormatTabboz. */
private var tmpsesso = 'M'

/** `static int temp_debug` di Configuration. */
private var temp_debug = 0

//*******************************************************************
// Calcola Sesso 29 Maggio 1999 - Maschietto o Femminucica
//*******************************************************************

private fun CalcolaSesso() {
    if (sesso == 'M') {
        ao = 'o'
        un_una = "un"
    } else {
        ao = 'a'
        un_una = "una"
    }
}

//*******************************************************************
// ResetMe  26 Marzo 1999 - Reset del Tabboz Simulator
//*******************************************************************

suspend fun ResetMe(primavolta: Int) {
    var i: Int
    var tmp: String

    TabbozRedraw = 1
    Soldi = 10
    Paghetta = 30
    Reputazione = 0
    Fama = 0
    Rapporti = 0
    Stato = 100
    impegno = 0
    giorni_di_lavoro = 0
    numeroditta = 0

    Residenza = "Milano"
    Nometipa = ""

    City = LoadString(400 + random(22))

    tmp = LoadString(450 + random(50))
    Street = "$tmp n. ${1 + random(150)}"

    i = 1
    while (i < 10) {
        MaterieMem[i].xxx = 0
        i++
    }

    CalcolaStudio()

    x_mese = 9
    x_giorno = 30
    x_giornoset = 1
    x_anno_bisesto = 0

    comp_mese = random(12) + 1
    comp_giorno = random(InfoMese[comp_mese - 1].num_giorni) + 1

    if (primavolta != 0) {
        // Se e' la prima volta che uso il tabboz resetta anche la configurazione...
        STARTcmdShow = 1
        timer_active = 1
        sound_active = 1
        euro = 0
        sesso = 'M'
        Nome = "Tizio"
        Cognome = "Caio"
        CalcolaSesso()
    }

    if (sesso == 'F') {
        Nome = "Nessuna"
        Cognome = "In Particolare"
    }

    sizze = 0
    current_testa = 0
    current_gibbotto = 0
    current_pantaloni = 0
    current_scarpe = 0

    ScooterData.stato = -1

    AbbonamentData.creditorest = -1
    CellularData.stato = -1
}

//*******************************************************************
// Formattazione iniziale Tabbozzo (scelta sesso, nome...) 14-01-2000
//*******************************************************************

suspend fun FormatTabboz(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        CheckDlgButton(hDlg, 102, true)
        if (random(2) == 1)
            tmpsesso = 'M'
        else
            tmpsesso = 'F'

        ComboAddString(hDlg, 110, "3.5\", 1.44MB, 512 bytes/sector")
        ComboSetCurSel(hDlg, 110, 0)
        return true
    }

    if (message == WM_COMMAND) {
        when (wParam) {
            100 -> {
                tmpsesso = 'M'
            }
            101 -> {
                tmpsesso = 'F'
            }
            102 -> {
                if (random(2) == 1)
                    tmpsesso = 'M'
                else
                    tmpsesso = 'F'
            }

            IDOK -> {
                ResetMe(0)
                sesso = tmpsesso
                CalcolaSesso()
                EndDialog(hDlg, 1)
                return true
            }

            IDCANCEL -> {
                if (firsttime == 1) {
                    sesso = tmpsesso
                    CalcolaSesso()
                }
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

//*******************************************************************
// InitTabboz
//*******************************************************************

suspend fun InitTabboz() {
    var tmp: String

    nome_del_file_su_cui_salvare = ""

    // Inizializzazione dei numeri casuali...
    randomize()

    // Inizializza un po' di variabile...
    boolean_shutdown = 0 /* 0=resta dentro, 1=uscita, 2=shutdown 19 Giugno 1999 / 14 Ottoble 1999 */

    Fortuna = 0                        /* Uguale a me...               */
    ScooterData = ScooterMem[0].copy() /* nessuno scooter              */
    Attesa = ATTESAMAX                 /* attesa per avere soldi...    */
    ImgSelector = 0                    /* W l' arte di arrangiarsi...  */
    timer_active = 1                   /* 10 Giugno  1998 */
    fase_di_avvio = 1                  /* 11 Giugno  1998 */
    Tempo_trascorso_dal_pestaggio = 0  /* 12 Giugno  1998 */
    current_tipa = 0                   /* 6  Maggio  1999 */

    // TABBOZ_DEBUG
    debug_active = atoi(RRKey("Debug"))

    if (debug_active < 0)
        debug_active = 0

    if (debug_active == 1) {
        openlog()
        tmp = "tabboz: Starting Tabboz Simulator $VERSION $BUILD_DATE"
        writelog(tmp)
    }

    // Ottieni i nomi dei creatori di sto coso...
    Andrea = LoadString(10)
    Caccia = LoadString(11)
    Daniele = LoadString(12)
    Obscured = LoadString(2)

    /* Registra la Classe BMPView - E' giusto metterlo qui ??? - 25 Feb 1999 */
    RegisterBMPViewClass()

    /* Registra la Classe BMPTipa - 6 Maggio 1999 */
    RegisterBMPTipaClass()

    firsttime = 0
    CaricaTutto()

    // 15 Mar 1998 - Ora mostra anche il logo iniziale
    // 12 Mar 1999 - A causa di un riordino generale, e' stata spostata qui...
    if (STARTcmdShow != 0)
        DialogBox(LOGO, null, ::Logo)

    // 14 Gen 1999 - Formattazione iniziale Tabbozzo
    if (firsttime == 1) {
        DialogBox(FORMAT, null, ::FormatTabboz) // MAKEINTRESOURCE(15)
    }
}

//*******************************************************************
//  Carica le caratteristiche dal registro o da un file...
//*******************************************************************

private suspend fun CaricaTutto() {
    var tmp: String
    var i: Int

    /* Prima che vengano caricate le informazioni... */
    /* azzera il checksum... 15 Marzo 1999           */
    new_reset_check()

    /* Cerca le informazioni registrate */

    Soldi = new_check_l(atol(RRKey("Soldi"))) // ATOL, visto che Soldi e' un LONG, non un semplice INT

    Paghetta = new_check_l(atol(RRKey("Paghetta")))

    Reputazione = vvc(new_check_i(atoi(RRKey("Reputazione"))))

    Studio = vvc(new_check_i(atoi(RRKey("Studio"))))

    Fama = vvc(new_check_i(atoi(RRKey("Fama"))))

    Rapporti = vvc(new_check_i(atoi(RRKey("Rapporti"))))

    Stato = vvc(new_check_i(atoi(RRKey("Stato"))))

    DDP = new_check_l(atol(RRKey("DdP"))) // ATOL, visto che e' un LONG, non un semplice INT

    FigTipa = vvc(new_check_i(atoi(RRKey("FigTipa"))))

    Nome = TabbozReadKey("Nome") ?: ""
    Cognome = TabbozReadKey("Cognome") ?: ""
    Nometipa = TabbozReadKey("Nometipa") ?: ""

    val xCity = TabbozReadKey("City")
    if (xCity == null)
        firsttime = 1
    else
        City = xCity

    TabbozReadKey("Residenza")?.let { Residenza = it }

    TabbozReadKey("Street")?.let { Street = it }

    // la serie di 9 "A" messe nella riga sotto NON E' CASUALE
    // non sostituirla con altre lettere !

    tmp = TabbozReadKey("Materie") ?: "AAAAAAAAA"

    i = 1
    while (i < 10) {
        // In C tmp e' un buffer di 128 caratteri: se la chiave e' piu' corta di 9
        // caratteri si legge oltre la fine della stringa (il valore viene poi azzerato).
        MaterieMem[i].xxx = (if (i - 1 < tmp.length) tmp[i - 1].code else 65) - 65
        if ((MaterieMem[i].xxx < 0) || (MaterieMem[i].xxx > 10))
            MaterieMem[i].xxx = 0
        i++
    }
    CalcolaStudio()

    Fortuna = vvc(new_check_i(atoi(RRKey("Fortuna"))))

    // Se e' la prima volta che parte il Tabboz Simulator, la data e' impostata al 30 Settembre
    x_mese = atoi(RRKey("Mese"))
    if (x_mese < 1)
        x_mese = 9
    x_giorno = atoi(RRKey("Giorno"))
    if (x_giorno < 1)
        x_giorno = 30
    x_giornoset = atoi(RRKey("GiornoSet"))
    if (x_giornoset < 1)
        x_giornoset = 1
    x_anno_bisesto = atoi(RRKey("AnnoBisestile"))
    if (x_anno_bisesto > 3)
        x_anno_bisesto = 3

    // Se non e' gia' settato,setta il compleanno
    comp_mese = atoi(RRKey("CompMese"))
    if (comp_mese < 1)
        comp_mese = random(12) + 1

    comp_giorno = atoi(RRKey("CompGiorno"))
    if (comp_giorno < 1)
        comp_giorno = random(InfoMese[comp_mese - 1].num_giorni) + 1

    numeroditta = vvc(atoi(RRKey("NumeroDitta")))
    scad_pal_giorno = vvc(atoi(RRKey("ScadPalGiorno")))
    scad_pal_mese = vvc(atoi(RRKey("ScadPalMese")))

    impegno = vvc(new_check_i(atoi(RRKey("Impegno"))))
    giorni_di_lavoro = vvc(new_check_i(atoi(RRKey("GiorniDiLavoro"))))
    stipendio = new_check_i(atoi(RRKey("Stipendio")))
    if (stipendio < 0)
        stipendio = 0

    sizze = new_check_i(atoi(RRKey("Sigarette")))
    if (sizze < 0)
        sizze = 0

    current_testa = vvc(new_check_i(atoi(RRKey("Testa"))))
    current_gibbotto = vvc(new_check_i(atoi(RRKey("Giubbotto"))))
    current_pantaloni = vvc(new_check_i(atoi(RRKey("Pantaloni"))))
    current_scarpe = vvc(new_check_i(atoi(RRKey("Scarpe"))))

    euro = atoi(RRKey("Euro"))
    if (euro < 0) {
        euro = 0
    }

    tmp = RRKey("Sesso")
    sesso = if (tmp.isEmpty()) Char(0) else tmp[0] // in C: tmp[0] su stringa vuota e' 0
    if ((sesso != 'M') && (sesso != 'F'))
        sesso = 'M'

    CalcolaSesso()

    STARTcmdShow = atoi(RRKey("STARTcmdShow"))
    if (STARTcmdShow < 0)
        STARTcmdShow = 1

    timer_active = atoi(RRKey("TimerActive"))
    if (timer_active < 0)
        timer_active = 1

    sound_active = atoi(RRKey("SoundActive"))
    if (sound_active < 0)
        sound_active = 1

    ScooterData.speed = new_check_i(atoi(RRKey("Scooter\\Speed")))
    if (ScooterData.speed < 0)
        ScooterData.speed = 0
    ScooterData.marmitta = new_check_i(atoi(RRKey("Scooter\\Marmitta")))
    if (ScooterData.marmitta < 0)
        ScooterData.marmitta = 0
    ScooterData.carburatore = new_check_i(atoi(RRKey("Scooter\\Carburatore")))
    if (ScooterData.carburatore < 0)
        ScooterData.carburatore = 0
    ScooterData.cc = new_check_i(atoi(RRKey("Scooter\\CC")))
    if (ScooterData.cc < 0)
        ScooterData.cc = 0
    ScooterData.filtro = new_check_i(atoi(RRKey("Scooter\\Filtro")))
    if (ScooterData.filtro < 0)
        ScooterData.filtro = 0
    ScooterData.prezzo = new_check_i(atoi(RRKey("Scooter\\Prezzo")))
    if (ScooterData.prezzo < 0)
        ScooterData.prezzo = 0
    ScooterData.attivita = new_check_i(atoi(RRKey("Scooter\\Attivita")))
    if (ScooterData.attivita < 0)
        ScooterData.attivita = 0

    ScooterData.stato = new_check_i(atoi(RRKey("Scooter\\Stato"))) /* -1 = nessuno */

    benzina = new_check_i(atoi(RRKey("Scooter\\Benzina")))
    if (benzina < 0)
        benzina = 0

    /*  antifurto               = atoi (RRKey("Scooter\\Antifurto") ); */

    ScooterData.nome = TabbozReadKey("Scooter\\Nome") ?: "nessuno"

    //  Cellulare ----------------------

    AbbonamentData.dualonly = new_check_i(atoi(RRKey("Cellular\\DualOnly")))
    AbbonamentData.creditorest = new_check_i(atoi(RRKey("Cellular\\CreditoRest")))
    val xNomeAbb = TabbozReadKey("Cellular\\NomeAbb")
    if (xNomeAbb == null)
        AbbonamentData.creditorest = -1
    else
        AbbonamentData.nome = xNomeAbb

    CellularData.dual = new_check_i(atoi(RRKey("Cellular\\DualBand")))
    CellularData.stato = new_check_i(atoi(RRKey("Cellular\\Stato")))
    CellularData.prezzo = new_check_i(atoi(RRKey("Cellular\\Prezzo")))
    val xNomeCel = TabbozReadKey("Cellular\\Nome")
    if (xNomeCel == null)
        CellularData.stato = -1
    else
        CellularData.nome = xNomeCel

    if (firsttime != 0) /* 29 Novembre 1998 */
        ResetMe(1)

    /* Controllo eventuali errori nella data (o data non settata...) */
    if (x_giorno < 1)
        x_giorno = 1
    if (x_giorno > 31)
        x_giorno = 1
    if (x_mese < 1)
        x_mese = 1
    if (x_mese > 12)
        x_mese = 1
    if (x_giornoset < 1)
        x_giornoset = 1
    if (x_giornoset > 7)
        x_giornoset = 1

    x_giorno-- /* Per evitare che avanzi di giorno ogni volta che si apre il programma */
    x_giornoset--
    Giorno(null)

    // TABBOZ_DEBUG
    // Non si possono mettere le infomazioni del counter nel file di log
    // perche' sarebbe facilissimo hackerale...
    //     sprintf(tmp,"tabboz: (R) new_counter %lu", new_counter);
    //     writelog(tmp);
    //     sprintf(tmp,"tabboz: (R) read_counter %lu", atoi(RRKey("SoftCheck")) );
    //     writelog(tmp);

    // ven 10 marzo 2000
    // Guarda se qualche "bastardino" ha modificato dei valori nel registro...
    if ((new_counter - atoi(RRKey("SoftCheck"))) != 0L) {
        // ResetMe(0);  // TODO
    }
}

//*******************************************************************
//      Fine Programma
//*******************************************************************

suspend fun FineProgramma(caller: String) {
    var tmp: String

    // TABBOZ_DEBUG
    tmp = "tabboz: FineProgramma chiamato da <$caller>"
    writelog(tmp)

    if (nome_del_file_su_cui_salvare.isEmpty()) {
        /* Salva lo stato del tabbozzo */
        /* 0.8.1pr 29 Novembre 1998 Ora non salva piu' nel WIN.INI con WriteProfileString,
           ma salva nel registro di configurazione... */
        TabbozAddKey("Exe", "zarrosim")
    }

    SalvaTutto()
}

//*******************************************************************
//      Salva le caratteristiche nel registro o su di un file
//*******************************************************************

fun SalvaTutto() {
    var tmp: String
    var i: Int

    // TABBOZ_DEBUG
    writelog("SalvaTutto")

    new_reset_check()

    tmp = "${new_check_l(Soldi)}"
    TabbozAddKey("Soldi", tmp)

    tmp = "${new_check_l(Paghetta)}"
    TabbozAddKey("Paghetta", tmp)

    tmp = "${new_check_i(Reputazione)}"
    TabbozAddKey("Reputazione", tmp)

    tmp = "${new_check_i(Studio)}"
    TabbozAddKey("Studio", tmp)

    tmp = "${new_check_i(Fama)}"
    TabbozAddKey("Fama", tmp)

    tmp = "${new_check_i(Rapporti)}"
    TabbozAddKey("Rapporti", tmp)

    tmp = "${new_check_i(Stato)}"
    TabbozAddKey("Stato", tmp)

    tmp = "${new_check_l(DDP)}"
    TabbozAddKey("DdP", tmp)

    tmp = "${new_check_i(FigTipa)}"
    TabbozAddKey("FigTipa", tmp)

    TabbozAddKey("Nome", Nome)
    TabbozAddKey("Cognome", Cognome)
    TabbozAddKey("Nometipa", Nometipa)
    TabbozAddKey("City", City)
    TabbozAddKey("Residenza", Residenza)
    TabbozAddKey("Street", Street)

    val tmpMaterie = "123456789".toCharArray() // 9 materie
    i = 1
    while (i < 10) {
        tmpMaterie[i - 1] = Char(65 + MaterieMem[i].xxx)
        i++
    }
    tmp = String(tmpMaterie)

    TabbozAddKey("Materie", tmp)

    tmp = "${new_check_i(Fortuna)}"
    TabbozAddKey("Fortuna", tmp)

    tmp = "$x_mese"
    TabbozAddKey("Mese", tmp)
    tmp = "$x_giorno"
    TabbozAddKey("Giorno", tmp)
    tmp = "$x_giornoset"
    TabbozAddKey("GiornoSet", tmp)
    tmp = "$x_anno_bisesto"
    TabbozAddKey("AnnoBisestile", tmp)

    tmp = "$comp_mese"
    TabbozAddKey("CompMese", tmp)
    tmp = "$comp_giorno"
    TabbozAddKey("CompGiorno", tmp)

    tmp = "$numeroditta"
    TabbozAddKey("NumeroDitta", tmp)
    tmp = "$scad_pal_giorno"
    TabbozAddKey("ScadPalGiorno", tmp)
    tmp = "$scad_pal_mese"
    TabbozAddKey("ScadPalMese", tmp)

    tmp = "${new_check_i(impegno)}"
    TabbozAddKey("Impegno", tmp)
    tmp = "${new_check_i(giorni_di_lavoro)}"
    TabbozAddKey("GiorniDiLavoro", tmp)
    tmp = "${new_check_i(stipendio)}"
    TabbozAddKey("Stipendio", tmp)

    tmp = "${new_check_i(sizze)}"
    TabbozAddKey("Sigarette", tmp)

    tmp = "${new_check_i(current_testa)}"
    TabbozAddKey("Testa", tmp)

    tmp = "${new_check_i(current_gibbotto)}"
    TabbozAddKey("Giubbotto", tmp)

    tmp = "${new_check_i(current_pantaloni)}"
    TabbozAddKey("Pantaloni", tmp)

    tmp = "${new_check_i(current_scarpe)}"
    TabbozAddKey("Scarpe", tmp)

    tmp = "$euro"
    TabbozAddKey("Euro", tmp)

    tmp = "$STARTcmdShow"
    TabbozAddKey("STARTcmdShow", tmp)

    tmp = "$timer_active"
    TabbozAddKey("TimerActive", tmp)

    tmp = "$sound_active"
    TabbozAddKey("SoundActive", tmp)

    tmp = "$sesso"
    TabbozAddKey("Sesso", tmp)

    // TABBOZ_DEBUG
    tmp = "$debug_active"
    TabbozAddKey("Debug", tmp)

    tmp = "${new_check_i(ScooterData.speed)}"
    TabbozAddKey("Scooter\\Speed", tmp)
    tmp = "${new_check_i(ScooterData.marmitta)}"
    TabbozAddKey("Scooter\\Marmitta", tmp)
    tmp = "${new_check_i(ScooterData.carburatore)}"
    TabbozAddKey("Scooter\\Carburatore", tmp)
    tmp = "${new_check_i(ScooterData.cc)}"
    TabbozAddKey("Scooter\\CC", tmp)
    tmp = "${new_check_i(ScooterData.filtro)}"
    TabbozAddKey("Scooter\\Filtro", tmp)
    tmp = "${new_check_i(ScooterData.prezzo)}"
    TabbozAddKey("Scooter\\Prezzo", tmp)
    tmp = "${new_check_i(ScooterData.attivita)}"
    TabbozAddKey("Scooter\\Attivita", tmp)

    tmp = "${new_check_i(ScooterData.stato)}"
    TabbozAddKey("Scooter\\Stato", tmp)

    tmp = "${new_check_i(benzina)}"
    TabbozAddKey("Scooter\\Benzina", tmp)

    /*  sprintf(tmp,"%d",antifurto);
        TabbozAddKey("Scooter\\Antifurto", tmp); */

    TabbozAddKey("Scooter\\Nome", ScooterData.nome)

    tmp = "${new_check_i(AbbonamentData.dualonly)}"
    TabbozAddKey("Cellular\\DualOnly", tmp)
    tmp = "${new_check_i(AbbonamentData.creditorest)}"
    TabbozAddKey("Cellular\\CreditoRest", tmp)

    TabbozAddKey("Cellular\\NomeAbb", AbbonamentData.nome)

    tmp = "${new_check_i(CellularData.dual)}"
    TabbozAddKey("Cellular\\DualBand", tmp)
    tmp = "${new_check_i(CellularData.stato)}"
    TabbozAddKey("Cellular\\Stato", tmp)
    tmp = "${new_check_i(CellularData.prezzo)}"
    TabbozAddKey("Cellular\\Prezzo", tmp)

    TabbozAddKey("Cellular\\Nome", CellularData.nome)

    tmp = "$new_counter"
    TabbozAddKey("SoftCheck", tmp)

    TabbozAddKey("Version", VERSION)
}

/********************************************************************/
/* About...                                                         */
/********************************************************************/

suspend fun About(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var buf: String
    var tmp: String
    var i: Int

    if (message == WM_INITDIALOG) {
        tmp = Andrea
        SetDlgItemText(hDlg, 110, tmp)
        tmp = Caccia
        SetDlgItemText(hDlg, 111, tmp)
        tmp = Daniele
        SetDlgItemText(hDlg, 112, tmp)

        tmp = "$VERSION, $BUILD_DATE"
        SetDlgItemText(hDlg, 115, tmp)

        tmp = Obscured /* Obscured Truckware (se il nome e' diverso, crash ! ) */
        SetDlgItemText(hDlg, 116, tmp)

        cheat = 0
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            IDOK -> {
                EndDialog(hDlg, 1)
                return true
            }
            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            203, 113 -> {
                Atinom(hDlg)
                return true
            }

            257 -> {
                cheat = cheat + 1
                if (cheat >= 10) {
                    /* Lunedi' 13 Aprile 1998 - Ora il trukko viene un attimo modificato... */

                    buf = "$Nome $Cognome"

                    tmp = LoadString(13) /* Dino... */
                    if (tmp == buf) {
                        Soldi = Soldi + 1000
                        Reputazione = random(4)
                        Fama = random(40)
                    }

                    tmp = LoadString(14) /* Fratello di Dino... */
                    if (tmp == buf) {
                        Soldi = Soldi + 1000
                        Reputazione = random(30)
                        Fama = random(5)
                    }

                    if (Daniele == buf) {
                        /* Murdock, ti regala una macchinina... */
                        ScooterData = ScooterMem[7].copy()
                        benzina = 850
                        Reputazione = 100
                    }

                    if (Caccia == buf) {
                        /* Caccia fa' aumentare i dindi... */
                        Soldi = Soldi + 10000
                        Fama = 100
                    }

                    if (Andrea == buf) {
                        /* Io porto la scuola e la tipa al 100% */
                        i = 1
                        while (i < 10) {
                            MaterieMem[i].xxx = 10
                            i++
                        }
                        CalcolaStudio()
                        if (Rapporti > 1)
                            Rapporti = 100
                        impegno = 100
                        numeroditta = 1
                        stipendio = 5000
                    }

                    cheat = 0
                }
                return true // case 257 prosegue nel default: return (TRUE)
            }
            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Logo...                                                          */
/********************************************************************/

suspend fun Logo(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {

        if (sound_active != 0)
            TabbozPlaySound(0)

        if (fase_di_avvio == 1)
            SetTimer(hDlg, 10000) /* 10 Secondi */

        return true
    }

    if (message == WM_TIMER) {
        if (fase_di_avvio == 1) {
            KillTimer(hDlg) /* Distrugge il timer... */
            EndDialog(hDlg, 1)
        }
    }

    if (message == WM_COMMAND) {
        when (wParam) {
            IDOK, 202, 203 -> {
                if (fase_di_avvio == 1) {
                    KillTimer(hDlg)
                }
                EndDialog(hDlg, 1)
                return true
            }
            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Spegnimi... 11 giugno 1998                                       */
/********************************************************************/

suspend fun Spegnimi(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        boolean_shutdown = 1 // Uscita normale...

        CheckDlgButton(hDlg, 102, false)
        CheckDlgButton(hDlg, 101, true)

        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {

            101 -> { // Un bug nella v 0.8.51pr impediva l' uscita corretta...
                boolean_shutdown = 1 // 1=uscita
                return true
            }

            102 -> {
                boolean_shutdown = 2 // 2=shutdown
                return true
            }

            110 -> {
                MessageBox(hDlg,
                    "'Spegni il computer ed esci di casa'\n\nPubblicita' Progresso per il recupero dei giovani disadattati a causa dei computer sponsorizzata da Obscured Truckware.",
                    "Guida del Tabboz Simulator", MB_OK)
                return true
            }

            IDOK -> {
                SpegniISuoni() // TABBOZ32
                EndDialog(hDlg, 1)
                return true
            }

            IDCANCEL -> {
                boolean_shutdown = 0 // non e' proprio un boolean, ma va bene lo stesso...
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Configuration...                                                 */
/********************************************************************/

suspend fun Configuration(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        // TABBOZ_DEBUG
        temp_debug = debug_active

        if (STARTcmdShow != 0)
            CheckDlgButton(hDlg, 106, true)
        if (euro != 0)
            CheckDlgButton(hDlg, 107, true)
        if (timer_active != 0)
            CheckDlgButton(hDlg, 108, true)
        // TABBOZ_DEBUG
        if (debug_active != 0)
            CheckDlgButton(hDlg, 109, true)
        if (sound_active != 0)
            CheckDlgButton(hDlg, 110, true)

        if (Fortuna >= 20)
            CheckDlgButton(hDlg, 101, true)
        else if (Fortuna >= 15)
            CheckDlgButton(hDlg, 102, true)
        else if (Fortuna >= 10)
            CheckDlgButton(hDlg, 103, true)
        else if (Fortuna >= 5)
            CheckDlgButton(hDlg, 104, true)
        else
            CheckDlgButton(hDlg, 105, true)

        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {
            101 -> {
                Fortuna = 20
                return true /* Livelli di Difficolta' */
            }
            102 -> {
                Fortuna = 15
                return true
            }
            103 -> {
                Fortuna = 10
                return true
            }
            104 -> {
                Fortuna = 5
                return true
            }
            105 -> {
                Fortuna = 0
                return true
            }
            106 -> {
                STARTcmdShow = if (STARTcmdShow != 0) 0 else 1
                return true
            }
            107 -> {
                euro = if (euro != 0) 0 else 1
                return true
            }
            108 -> {
                timer_active = if (timer_active != 0) 0 else 1
                return true
            }
            // TABBOZ_DEBUG
            109 -> {
                debug_active = if (debug_active != 0) 0 else 1
                return true
            }
            110 -> {
                sound_active = if (sound_active != 0) 0 else 1
                return true
            }

            203 -> { // Reset - 26 Marzo 1999
                /* EndDialog(hDlg, TRUE); */

                // Se il tabboz e' chiamato con il parametro "config", hWndMain NON ESITE !
                /* if (hWndMain != 0) */
                /*     ShowWindow(hWndMain, WIN_PICCOLO); */

                DialogBox(FORMAT, hDlg, ::FormatTabboz) // MAKEINTRESOURCE(15)

                return true
            }

            IDOK, IDCANCEL -> {
                SalvaTutto() // TABBOZ_EM
                // TABBOZ_DEBUG
                if (debug_active != temp_debug) {
                    if (debug_active != 0) {
                        openlog()
                        writelog("tabboz: Start logging...")
                    } else {
                        writelog("tabboz: Stop logging...")
                        closelog()
                    }
                }
                EndDialog(hDlg, 1)
                return true
            }
            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Personal Information...                                          */
/********************************************************************/

suspend fun PersonalInfo(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        tmp = " $comp_giorno ${InfoMese[comp_mese - 1].nome}"
        SetDlgItemText(hDlg, 103, tmp) // Data di nascita
        tmp = "${comp_giorno * 13 + comp_mese * 3 + 6070}"
        SetDlgItemText(hDlg, 104, tmp) // Numero documento di nascita (inutile ma da' spessore...)

        if (numeroditta < 1) { // Professione
            if (sesso == 'M')
                tmp = "Studente"
            else
                tmp = "Studentessa"
        } else
            tmp = "Sfruttat$ao"

        SetDlgItemText(hDlg, 108, tmp)

        tmp = "Nat$ao il"
        SetDlgItemText(hDlg, 109, tmp) // Nata/o il
        if (sesso == 'M')
            SetDlgItemText(hDlg, 110, "Celibe")
        else
            SetDlgItemText(hDlg, 110, "Libera")

        SetDlgItemText(hDlg, 111, Residenza) // Residenza

        SetDlgItemText(hDlg, 107, Street) // Indirizzo (inutile ma da' spessore...)

        SetDlgItemText(hDlg, 105, City) // Citta' di nascita (inutile ma da' spessore...)

        SetDlgItemText(hDlg, 101, Cognome) // Cognome

        SetDlgItemText(hDlg, QX_NOME, Nome) // Nome

        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {
            111 -> {
                Residenza = GetDlgItemText(hDlg, wParam)
            }

            107 -> {
                Street = GetDlgItemText(hDlg, wParam)
            }

            105 -> {
                City = GetDlgItemText(hDlg, wParam)
            }

            101 -> { /* Cognome del tabbozzo */
                Cognome = GetDlgItemText(hDlg, wParam)
            }

            QX_NOME -> { /* Nome del tabbozzo */
                Nome = GetDlgItemText(hDlg, wParam)
            }

            IDOK, IDCANCEL -> {
                SalvaTutto() // TABBOZ_EM
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Famiglia...                                                      */
/********************************************************************/

suspend fun Famiglia(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        tmp = "Papa', mi dai ${MostraSoldi(100)} ?"
        SetDlgItemText(hDlg, 103, tmp)
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        SetDlgItemText(hDlg, 105, MostraSoldi(Paghetta))
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101 -> { /* Chiedi aumento paghetta */
                if (Studio > 40) {
                    if (((Studio - Paghetta + Fortuna) > (75 + random(50))) && (Paghetta < 96)) {
                        tmp = "Va bene... ti daremo ${MostraSoldi(5)} di paghetta in piu'..."
                        MessageBox(hDlg, tmp,
                            "Aumento paghetta !", MB_OK or MB_ICONINFORMATION)
                        Paghetta += 5
                        Evento(hDlg)
                    } else {
                        MessageBox(hDlg,
                            "Vedi di scordartelo... Dovra' passare molto tempo prima che ti venga aumentata la paghetta...",
                            "Errore irrecuperabile", MB_OK or MB_ICONHAND)
                        Evento(hDlg)
                    }
                } else {
                    MessageBox(hDlg,
                        "Quando andrai meglio a scuola, forse...",
                        "Errore irrecuperabile", MB_OK or MB_ICONHAND)
                }
                SetDlgItemText(hDlg, 105, MostraSoldi(Paghetta))
                return true
            }

            102 -> { // Chiedi paghetta extra
                if (Studio >= 40) {
                    if (Attesa == 0) {
                        Attesa = ATTESAMAX
                        Soldi += 10
                        // TABBOZ_DEBUG
                        tmp = "famiglia: paghetta extra (${MostraSoldi(10)})"
                        writelog(tmp)
                        Evento(hDlg)
                    } else {
                        MessageBox(hDlg,
                            "Ma insomma ! Non puoi continuamente chiedere soldi ! Aspetta ancora qualche giorno. Fai qualche cosa di economico nel frattempo...",
                            "Non te li diamo", MB_OK or MB_ICONHAND)
                        Evento(hDlg)
                    }
                } else {
                    tmp = "Quando andrai meglio a scuola potrai tornare a chiederci dei soldi, non ora. " +
                        "Ma non lo sai che per la tua vita e' importante studiare, e dovresti impegnarti " +
                        "di piu, perche' quando ti impegni i risultati si vedono, solo che sei svogliat$ao " +
                        "e non fai mai nulla, mi ricordo che quando ero giovane io era tutta un altra cosa... " +
                        "allora si' che i giovani studiavano..."

                    MessageBox(hDlg, tmp,
                        "Errore irrecuperabile", MB_OK or MB_ICONHAND)
                }

                SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
                return true
            }

            103 -> { // Papa, mi dai 100000 lire ?
                if (sound_active != 0)
                    TabbozPlaySound(801)
                MessageBox(hDlg,
                    "Non pensarci neanche lontanamente...",
                    "Errore irrecuperabile", MB_OK or MB_ICONHAND)
                Evento(hDlg)
                return true
            }

            IDOK, IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

//*******************************************************************
// Compagnia...
//*******************************************************************

suspend fun Compagnia(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var buf: String
    var tmp: String
    var i: Int
    var i2: Int

    if (message == WM_INITDIALOG) {
        tmp = "$Reputazione/100"
        SetDlgItemText(hDlg, 104, tmp)
        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {
            101 -> { // Gareggia con lo scooter...
                if (ScooterData.stato <= 0) {
                    MessageBox(hDlg,
                        "Con quale scooter vorresti gareggiare, visto che non lo possiedi ?",
                        "Gareggia con lo scooter", MB_OK or MB_ICONINFORMATION)
                    return true
                }
                if (ScooterData.attivita != 1) {
                    buf = "Purtroppo non pui gareggiare visto che il tuo scooter e' ${n_attivita[ScooterData.attivita]}."
                    MessageBox(hDlg, buf, "Gareggia con lo scooter", MB_OK or MB_ICONINFORMATION)
                    return true
                }

                if (sound_active != 0)
                    TabbozPlaySound(701)
                i = 1 + random(6) // 28 Aprile 1998 - (E' cambiato tutto cio' che riguarda gli scooter...)
                buf = "Accetti la sfida con un tabbozzo che ha un ${ScooterMem[i].nome} ?"
                i2 = MessageBox(hDlg,
                    buf,
                    "Gareggia con lo scooter", MB_YESNO or MB_ICONQUESTION)

                if (ScooterData.stato > 30)
                    ScooterData.stato -= random(2)

                if (i2 == IDYES) {
                    //                if ( (ScooterMem[i].speed + 70 + random(50)) > (ScooterData.speed + ScooterData.stato + Fortuna) ) {
                    if ((ScooterMem[i].speed + 80 + random(40)) > (ScooterData.speed + ScooterData.stato + Fortuna)) {
                        // perdi
                        if (Reputazione > 80)
                            Reputazione -= 3
                        if (Reputazione > 10)
                            Reputazione -= 2
                        MessageBox(hDlg,
                            "Dopo pochi metri si vede l' inferiorita' del tuo scooter...",
                            "Hai perso", MB_OK or MB_ICONSTOP)
                    } else {
                        // vinci
                        Reputazione += 10
                        if (Reputazione > 100)
                            Reputazione = 100
                        MessageBox(hDlg,
                            "Con il tuo scooter, bruci l' avversario in partenza...",
                            "Hai vinto", MB_OK or MB_ICONINFORMATION)
                    }
                } else {
                    if (Reputazione > 80) // Se non accetti la sfida, perdi rep...
                        Reputazione -= 3
                    if (Reputazione > 10)
                        Reputazione -= 2
                }
                benzina -= 5
                if (benzina < 1)
                    benzina = 0
                showscooter = 0
                CalcolaVelocita(hDlg)

                Evento(hDlg)
                tmp = "$Reputazione/100"
                SetDlgItemText(hDlg, 104, tmp)
                return true
            }

            102 -> {
                // Uscendo con la propria compagnia si puo' arrivare
                // solamente a reputazione = 57
                if (Reputazione < 57)
                    Reputazione += 1
                if (Reputazione < 37) // Se la rep e' bassa, sale + in fretta
                    Reputazione += 1
                if (Reputazione < 12)
                    Reputazione += 1

                Evento(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            103 -> { /* 12 Giugno 1998 - Qualche mese dopo gli altri pulsanti della finestra... */
                if (Reputazione < 16) {
                    MessageBox(hDlg,
                        "Con la scarsa reputazione che hai, tutti trovano qualcosa di meglio da fare piuttosto che venire.",
                        "Chiama la Compagnia", MB_OK or MB_ICONSTOP)
                    Evento(hDlg)
                    return true
                }
                if (Tempo_trascorso_dal_pestaggio > 0) {
                    if (random(2) == 1) {
                        MessageBox(hDlg,
                            "Dopo aver visto i tuoi amici, chi ti ha picchiato non si fara' piu' vedere in giro x un bel pezzo...",
                            "Chiama la Compagnia", MB_OK or MB_ICONINFORMATION)
                        if (Reputazione < 80)
                            Reputazione += 3
                    } else {
                        MessageBox(hDlg,
                            "Anche i tuoi amici, al gran completo, vengono sacagnati di botte da chi ti aveva picchiato, accorgendosi cosi' che non sei solo tu ad essere una chiavica, ma lo sono anche loro...",
                            "Chiama la Compagnia", MB_OK or MB_ICONINFORMATION)
                        if (Reputazione < 95)
                            Reputazione += 5
                    }
                    Evento(hDlg)
                } else {
                    MessageBox(hDlg,
                        "Visto che non c'e' nessuno da minacciare, tutti se ne vanno avviliti...",
                        "Chiama la Compagnia (perche'?)", MB_OK or MB_ICONSTOP)
                }
                tmp = "$Reputazione/100"
                SetDlgItemText(hDlg, 104, tmp)
                return true
            }
            IDOK -> {
                EndDialog(hDlg, 1)
                return true
            }
            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }
            else -> return true
        }
    }

    return false
}

//*******************************************************************
// Nomoney...
//*******************************************************************

suspend fun nomoney(parent: Dlg?, tipo: Int) {
    var tmp: String
    when (tipo) {
        DISCO -> {
            tmp = "Appena entrat$ao ti accorgi di non avere abbastanza soldi per pagare il biglietto.\n Un energumeno buttafuori ti deposita gentilmente in un cassonetto della spazzatura poco distante dalla discoteca."
            MessageBox(parent, tmp,
                "Bella figura", MB_OK or MB_ICONSTOP)
            if (Reputazione > 3)
                Reputazione -= 1
        }
        VESTITI -> {
            tmp = "Con cosa avresti intenzione di pagare, stronzett$ao ??? Caramelle ???"
            MessageBox(parent, tmp,
                "Bella figura", MB_OK or MB_ICONSTOP)
            if (Fama > 12)
                Fama -= 3
            if (Reputazione > 4)
                Reputazione -= 2
        }
        PALESTRA -> {
            if (sesso == 'M') {
                MessageBox(parent,
                    "L' enorme istruttore di bodybulding ultra-palestrato ti suona come una zampogna e ti scaraventa fuori dalla palestra.",
                    "Non hai abbastanza soldi...", MB_OK or MB_ICONSTOP)
            } else {
                MessageBox(parent,
                    "L' enorme istruttore di bodybulding ultra-palestrato ti scaraventa fuori dalla palestra.",
                    "Non hai abbastanza soldi...", MB_OK or MB_ICONSTOP)
            }
            if (Fama > 14)
                Fama -= 4 /* Ah,ah ! fino al 10 Jan 1999 c'era scrittto Reputazione-=4... */
            if (Reputazione > 18)
                Reputazione -= 4
        }
        SCOOTER -> {
            if (sesso == 'M') {
                MessageBox(parent,
                    "L' enorme meccanico ti affera con una sola mano, ti riempe di pugni, e non esita a scaraventare te ed il tuo motorino fuori dall' officina.",
                    "Non hai abbastanza soldi", MB_OK or MB_ICONSTOP)
                if (Reputazione > 7)
                    Reputazione -= 5
                if (ScooterData.stato > 7)
                    ScooterData.stato -= 5
            } else {
                MessageBox(parent,
                    "Con un sonoro calcio nel culo, vieni buttata fuori dall' officina.",
                    "Non hai abbastanza soldi", MB_OK or MB_ICONSTOP)
                if (Reputazione > 6)
                    Reputazione -= 4
                if (Fama > 3)
                    Fama -= 2
            }
        }
        TABACCAIO -> {
            tmp = "Fai fuori dal mio locale, brut$ao pezzente !, esclama il tabaccaio con un AK 47 in mano..."
            MessageBox(parent, tmp,
                "Non hai abbastanza soldi...", MB_OK or MB_ICONSTOP)
            if (Fama > 2)
                Fama -= 1
        }
        CELLULRABBONAM -> {
            tmp = "Forse non ti sei accorto di non avere abbastanza soldi, stronzett$ao..."
            MessageBox(parent, tmp,
                "Non hai abbastanza soldi...", MB_OK or MB_ICONSTOP)
            if (Fama > 2)
                Fama -= 1
        }
    }
}

//*******************************************************************
// Aggiorna la finestra principale
//*******************************************************************

fun AggiornaPrincipale(parent: Dlg) {
    var tmp: String

    ShowWindow(parent, true)

    tmp = "$Nome $Cognome"
    SetDlgItemText(parent, QX_NOME, tmp)

    SetDlgItemText(parent, QX_SOLDI, MostraSoldi(Soldi))

    tmp = "$Fama/100" // Figosita'
    SetDlgItemText(parent, 151, tmp)

    tmp = "$Reputazione/100" // Reputazione
    SetDlgItemText(parent, 152, tmp)

    tmp = "$Studio/100" // Profitto scolastico
    SetDlgItemText(parent, 153, tmp)

    if (Rapporti != 0) {
        tmp = Nometipa // Nometipa
        SetDlgItemText(parent, 155, tmp)
        tmp = "$Rapporti/100" // Rapporti con la tipa
        SetDlgItemText(parent, 154, tmp)
    } else {
        tmp = " " // Nometipa
        SetDlgItemText(parent, 155, tmp)
        SetDlgItemText(parent, 154, tmp)
    }

    if (ScooterData.stato != -1) {
        tmp = ScooterData.nome
        SetDlgItemText(parent, 150, tmp) // Nomescooter
        tmp = "${ScooterData.stato}/100"
        SetDlgItemText(parent, 156, tmp) // Stato scooter
    } else {
        tmp = " "
        SetDlgItemText(parent, 150, tmp) // Nomescooter
        SetDlgItemText(parent, 156, tmp) // Stato scooter
    }

    // Calendario
    tmp = "${InfoSettimana[x_giornoset - 1].nome} $x_giorno ${InfoMese[x_mese - 1].nome}"
    SetDlgItemText(parent, 157, tmp)

    if (sesso == 'M') { // Non usare la variabile "ao" xche' qui e' necessario
        SetMenuItemText(parent, QX_TIPA, "&Tipa...")
        SetDlgItemText(parent, 133, "Tipa") // che ci sia scritto Tipa x il maschietto e
        SetDlgItemText(parent, 170, "Rapporto con la tipa")
    } else { //  Tipo x la femminuccia...
        SetMenuItemText(parent, QX_TIPA, "&Tipo...")
        SetDlgItemText(parent, 133, "Tipo")
        SetDlgItemText(parent, 170, "Rapporto con il tipo")
    }

    InvalidateRect(parent)
}

//*******************************************************************
// TabbozWndProc - handle Main dialog messages (modeless)
//
// This is a modeless dialog box procedure that controls this
// entire application.
//
// paramaters:
//             hWnd          - The window handle for this message
//             message       - The message number
//             wParam        - The WORD parmater for this message
//             lParam        - The LONG parmater for this message
//
//*******************************************************************

suspend fun TabbozWndProc(hWnd: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {

    when (message) {

        WM_ENDSESSION -> {
            if (wParam != 0)
                FineProgramma("end session")
        }

        WM_SYSCOMMAND -> {
            when (wParam) {
                QX_ABOUT -> {
                    /* Display about box. */
                    DialogBox(ABOUT, hWnd, ::About)

                    return true
                }

                SC_CLOSE -> {
                    DialogBox(SPEGNIMI, hWnd, ::Spegnimi)

                    if (boolean_shutdown != 0)
                        EndDialog(hWnd, 1) // Chiudi la baracca...

                    return true
                }
            }
            return false
        }

        WM_INITDIALOG -> {
            hWndMain = hWnd
            // Scrive quanti soldi ci sono... ( ed ora scrive anche molta altra roba...)
            AggiornaPrincipale(hWnd)

            /* Disabilita i menu Apri e Salva con nome */
            EnableMenuItem(hWnd, 106, false)
            EnableMenuItem(hWnd, 107, false)
            TabbozRedraw = 1

            fase_di_avvio = 0 /* 11 Giugno 1998 */
            return true
        }

        WM_COMMAND -> {
            when (wParam) {
                QX_LOAD -> {
                }

                QX_SAVE -> {
                }

                QX_CLOSE -> {
                    DialogBox(SPEGNIMI, hWnd, ::Spegnimi)

                    if (boolean_shutdown != 0)
                        EndDialog(hWnd, 1) // Chiudi la baracca...

                    return true
                }

                QX_ABOUT -> {
                    /* Display about box. */
                    DialogBox(ABOUT, hWnd, ::About)

                    AggiornaPrincipale(hWnd)
                }

                QX_LOGO -> {
                    DialogBox(LOGO, hWnd, ::Logo)

                    AggiornaPrincipale(hWnd)
                }

                QX_CONFIG -> {
                    /* Display configuration box. */
                    DialogBox(CONFIGURATION, hWnd, ::Configuration)

                    AggiornaPrincipale(hWnd)
                }
                //          case 251: /* Ex Immagine Tabbozzo */
                QX_INFO -> {
                    /* Display Personal Information box. */
                    DialogBox(PERSONALINFO, hWnd, ::PersonalInfo)

                    AggiornaPrincipale(hWnd)
                }
                QX_FAMIGLIA -> {
                    ShowWindow(hWnd, false)
                    /* Display Famiglia box. */
                    DialogBox(FAMIGLIA, hWnd, ::Famiglia)

                    AggiornaPrincipale(hWnd)
                }
                QX_DISCO -> {
                    ShowWindow(hWnd, false)
                    /* Display Disco box. */
                    DialogBox(DISCO, hWnd, ::Disco)

                    AggiornaPrincipale(hWnd)
                }
                QX_COMPAGNIA -> {
                    ShowWindow(hWnd, false)
                    /* Display Compagnia box. */
                    DialogBox(COMPAGNIA, hWnd, ::Compagnia)

                    AggiornaPrincipale(hWnd)
                }
                QX_SCUOLA -> {
                    if (x_vacanza == 0) {
                        ShowWindow(hWnd, false)
                        /* Display Scuola box. */
                        DialogBox(SCUOLA, hWnd, ::Scuola)
                        AggiornaPrincipale(hWnd)
                    } else {
                        MessageBox(hWnd,
                            "Non puoi andare a scuola in un giorno di vacanza !",
                            "Scuola", MB_OK or MB_ICONINFORMATION)
                    }
                }
                QX_SCOOTER -> {
                    /* Display Scooter box. */
                    ShowWindow(hWnd, false)

                    DialogBox(SCOOTER, hWnd, ::Scooter)

                    AggiornaPrincipale(hWnd)
                }
                QX_VESTITI -> {
                    /* Display Vestiti box. */
                    ShowWindow(hWnd, false)

                    DialogBox(VESTITI, hWnd, ::Vestiti)

                    AggiornaPrincipale(hWnd)
                }
                QX_TABACCHI -> {
                    ShowWindow(hWnd, false)
                    RunTabacchi(hWnd)
                    AggiornaPrincipale(hWnd)
                }
                QX_PALESTRA -> {
                    ShowWindow(hWnd, false)
                    RunPalestra(hWnd)
                    AggiornaPrincipale(hWnd)
                }
                QX_CELLULAR -> { // 31 Marzo 1999
                    ShowWindow(hWnd, false)
                    DialogBox(CELLULAR, hWnd, ::Cellular)
                    AggiornaPrincipale(hWnd)
                }
                QX_VESTITI1, QX_VESTITI2, QX_VESTITI3, QX_VESTITI4, QX_VESTITI5 -> {
                    ShowWindow(hWnd, false)
                    RunVestiti(hWnd, (wParam - QX_VESTITI1 + BAUHOUSE))
                    AggiornaPrincipale(hWnd)
                }
                QX_TIPA -> {
                    ShowWindow(hWnd, false)
                    if (sesso == 'M')
                        DialogBox(TIPA, hWnd, ::Tipa)
                    else
                        DialogBox(190, hWnd, ::Tipa)

                    AggiornaPrincipale(hWnd)
                }
                QX_LAVORO -> {
                    ShowWindow(hWnd, false)
                    // Display Lavoro box.
                    DialogBox(LAVORO, hWnd, ::Lavoro)

                    AggiornaPrincipale(hWnd)
                }

                // TABBOZ_EM
                QX_BMP -> {
                    BMPViewWndProc(hWnd, WM_LBUTTONDOWN, wParam, lParam)
                }

                else -> {
                }
            }
        }
    }

    return false
}

//*******************************************************************

fun MostraSoldi(i: Long): String {
    val tmp: String

    if (euro != 0)
        tmp = "${i / 2}e"
    else if (i == 0L)
        tmp = "0 L."
    else
        tmp = "${i}000 L."

    return tmp
}

/*********************************************************************/

suspend fun Atinom(hInstance: Dlg?) {

    MessageBox(hInstance,
        "Il biglietto e' valido solo dopo la convalida.Il biglietto deve essere conservato per tutta la durata " +
            "del viaggio. Il diritto a viaggiare cessa al termine della tratta corrispondente al valore del biglietto. " +
            "Il passeggero che al controllo non fosse in grado di presentare il biglietto o lo presentasse irriconoscibile, " +
            "o comunque non valido, verra' abbattuto. La notifica del decesso verra' inviata ai parenti solo previo pagamento " +
            "delle spese postali.",
        "Norme di utilizzo", MB_OK or MB_ICONINFORMATION)
}

/*********************************************************************/

/* 15 Giugno 1998 - v0.7.1 - Verifica Valori Chiave */
fun vvc(i: Int): Int {
    if (i < 0)
        return 0
    else if (i > 100)
        return 100
    else
        return i
}

/*********************************************************************/
/* Il log viene gestito dalla funzione writelog dell' API: qui restano
   solo due funzioni vuote. */

private fun openlog() {
}

private fun closelog() {
}

//*******************************************************************
// PROCEDURA PRINCIPALE per la versione Windows.

suspend fun WinMain(): Int {
    /* Inizializza il programma */
    InitTabboz()

    /* Finestra principale */
    DialogBox(MAIN, null, ::TabbozWndProc)

    /* Chiusura */

    // Nuova chiusura - 19 Giugno 1999, speriamo che ora non crashi piu'...
    if (boolean_shutdown == 2) {
        FineProgramma("shutdown") // Salvataggio partita...
        // TABBOZ_DEBUG
        if (debug_active != 0) {
            writelog("tabboz: end (exit + shutdown)")
            closelog()
        }
    } else {
        FineProgramma("main") // Salvataggio partita...
        // TABBOZ_DEBUG
        if (debug_active != 0) {
            writelog("tabboz: end (standard exit)")
            closelog()
        }
    }

    return boolean_shutdown
}

// =============================================================================
// newproteggi.c
// (C) Copyright 1998-1999 by Andrea Bonomi
// =============================================================================

/* 14 Marzo 1999 ------------------------------------------------------------ */

// 19 Giugno 1999 - credo che questa funzione faccia crashiare il Tabboz al' uscita.
/*
void clock_r1(u_long i1)
{
     if (i1 > 255 )
         new_counter=crctab[( (i1 % 255) >> 8) & 255] ^ (i1 << 8) ^ new_counter + 1999;
     new_counter=crctab[(i1 >> 8) & 255] ^ (i1 << 8) ^ new_counter + 79;
}
*/

// Nuova funzione ------------------------------------------------------------------

private var p_ruota1 = 0
private var p_ruota2 = 0
private val ruota1 = intArrayOf(0xed0f, 0xff00, 0x3392, 0xabcd, 0xc79c, 0x23df, 0x0006, 0xc39c)
private val ruota2 = intArrayOf(0xb47f, 0xc37b, 0x0070, 0x1999, 0xfb1e)

/** new_counter e' un u_long a 32 bit: dopo ogni operazione si maschera con 0xFFFFFFFF. */
private fun clock_r1(i1: Long) {
    var a1: Long
    var a2: Long

    a1 = (((i1 and 0xFFFFFFFFL) or ruota1[p_ruota1].toLong()) * (p_ruota2 + 1)) and 0xFFFFFFFFL
    p_ruota1++
    if (p_ruota1 > 7)
        p_ruota1 = 0

    a2 = ((a1 and ruota2[p_ruota2].toLong()) * (p_ruota1 + 1)) and 0xFFFFFFFFL
    p_ruota2++
    if (p_ruota2 > 4)
        p_ruota2 = 0

    if (a2 >= a1)
        a2 -= a1
    else
        a2 += a1
    a2 = a2 and 0xFFFFFFFFL

    new_counter = (new_counter + a2) and 0xFFFFFFFFL
}

/* -------------------------------------------------------------------------- */

fun new_reset_check() {
    new_counter = 0
    p_ruota1 = 0
    p_ruota1 = 0 // (sic: nell' originale p_ruota2 non viene azzerato)
}

/* -------------------------------------------------------------------------- */

fun new_check_i(i: Int): Int {
    clock_r1(i.toLong() and 0xFFFFFFFFL)
    return i
}

/* -------------------------------------------------------------------------- */

fun new_check_l(i: Long): Long {
    clock_r1(i)
    return i
}

/* -------------------------------------------------------------------------- */
