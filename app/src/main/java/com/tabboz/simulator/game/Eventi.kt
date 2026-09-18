@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997-1999 by Andrea Bonomi
//
// Traduzione di eventi.c. La variabile globale `messaggio` e le variabili
// figTemp / nomeTemp sono definite in GameState.kt; DueDonne e' in Tipa.kt.

/********************************************************************/
/* EVENTI CASUALI...                                                */
/********************************************************************/

suspend fun Evento(hInstance: Dlg?) {
    var caso: Int = 0
    var i: Int = 0
    var tmp: String = ""

    if (Fortuna < 0)
        Fortuna = 0 /* Prima che qualcuno bari... */
    if (Fortuna > 100)
        Fortuna = 100

    // if ( (AdV - 1) != 0 ) return;

    Giorno(hInstance)

    if (Tempo_trascorso_dal_pestaggio > 0)
        Tempo_trascorso_dal_pestaggio--

    /* Sigarette -------------------------------------------------------- */
    if (sizze > 0) {
        sizze--
        if (sizze == 0) {
            MessageBox(
                hInstance,
                "Apri il tuo pacchetto di sigarette e lo trovi disperatamente vuoto...",
                "Sei senza sigarette !", MB_OK or MB_ICONSTOP
            )
            if (Reputazione > 10)
                Reputazione -= 3
        } else if (sizze < 3)
            MessageBox(
                hInstance,
                "Ti accorgi che stai per finire le tue sizze.",
                "Sigarette...", MB_OK or MB_ICONINFORMATION
            )
    }

    /* Cellulare ----------------------------------------16 Apr 1999----- */

    if ((AbbonamentData.creditorest > 0) && (CellularData.stato > -1)) {
        AbbonamentData.creditorest -= 1
        if (Fama < 55)
            Fama++
        if (AbbonamentData.creditorest == 0) {
            MessageBox(
                hInstance,
                "Cerchi di telefonare e ti accorgi di aver finito i soldi a tua disposizione...",
                "Telefonino", MB_OK or MB_ICONSTOP
            )
        } else if (AbbonamentData.creditorest < 3)
            MessageBox(
                hInstance,
                "Ti accorgi che stai per finire la ricarica del tuo telefonino.",
                "Telefonino", MB_OK or MB_ICONINFORMATION
            )
    }

    if (CellularData.stato == 1) { // Cellulate 'morente'...
        CellularData.stato = -1
        MessageBox(
            hInstance,
            "Dopo una vita di duro lavoro, a furia di prendere botte, il tuo cellulare si spacca...",
            "Telefonino", MB_OK or MB_ICONSTOP
        )
    }

    /* Rapporti Tipa ---------------------------------------------------- */

    if (Rapporti > 3) {
        i = random(5) - 3
        if (i > 0)
            Rapporti--
    }

    if (Rapporti > 0)
        if (Rapporti < 98) {
            i = random(((Rapporti + Fortuna + Fama) * 3) + 1) + 1
            if (i < 11) { /* da 1 a 10, la donna ti molla... */
                if (sound_active != 0)
                    TabbozPlaySound(603)
                Rapporti = 0
                FigTipa = 0

                if (sesso == 'M') {
                    messaggio = LoadString(1040 + i) /* 1041 -> 1050 */
                    MessageBox(
                        hInstance,
                        messaggio,
                        "La tipa ti molla...", MB_OK or MB_ICONSTOP
                    )
                } else {
                    messaggio = LoadString(1340 + i) /* 1041 -> 1050 */
                    MessageBox(
                        hInstance,
                        messaggio,
                        "Vieni mollata...", MB_OK or MB_ICONSTOP
                    )
                }

                Reputazione -= (11 - i) // quelle con numero piu' basso, sono peggiori...
                if (Reputazione < 0)
                    Reputazione = 0
            }
        }

    /* Lavoro ----------------------------------------------------------- */

    if (impegno > 3) {
        i = random(7) - 3
        if (i > 0)
            impegno--
    }

    if (numeroditta > 0) {
        i = random(impegno * 2 + Fortuna * 3)
        if (i < 2) { /* perdi il lavoro */
            impegno = 0
            giorni_di_lavoro = 0
            stipendio = 0
            numeroditta = 0

            // LoadString(hInst, (1040 + i), (LPSTR)messaggio, 255);  /* 1041 -> 1050 */
            // MessageBox( hInstance,
            //	(LPSTR)messaggio,
            //	"Perdi il lavoro...", MB_OK | MB_ICONSTOP);

            if (sound_active != 0)
                TabbozPlaySound(504)

            tmp = "Un bel giorno ti svegli e scopri di essere stat$ao licenziat$ao."
            MessageBox(
                hInstance, tmp,
                "Perdi il lavoro...", MB_OK or MB_ICONSTOP
            )
        }
    }

    /* Paghetta --------------------------------------------------------- */

    if (x_giornoset == 6) { /* Il Sabato c'e' la paghetta... */
        if (Studio >= 45) {
            Soldi += Paghetta
            tmp = "eventi: Paghetta (${MostraSoldi(Paghetta)})"
            writelog(tmp)

            if (Studio >= 80) {
                if (sound_active != 0)
                    TabbozPlaySound(1100)
                Soldi += Paghetta
                writelog("eventi: Paghetta doppia !!!")

                MessageBox(
                    hInstance,
                    "Visto che vai bene a scuola, ti diamo il doppio della paghetta...",
                    "Paghetta settimanale", MB_OK or MB_ICONINFORMATION
                )
            }
        } else {
            if (sound_active != 0)
                TabbozPlaySound(1200)
            Soldi += (Paghetta / 2)
            tmp = "eventi: Meta' paghetta (${MostraSoldi(Paghetta)})..."
            writelog(tmp)

            MessageBox(
                hInstance,
                "Finche' non andrai bene a scuola, ti daremo solo meta' della paghetta...",
                "Paghetta settimanale", MB_OK or MB_ICONINFORMATION
            )
        }
    }

    /* Eventi casuali --------------------------------------------------- */
    caso = random(100 + (Fortuna * 2))

    //	caso = 21;	/* TEST - TEST - TEST - TEST - TEST - TEST - TEST */

    tmp = "eventi: Evento casule n. $caso"
    writelog(tmp)

    if (caso < 51) {
        when (caso) {

            // -------------- Metalloni e Manovali ---------------------------------------------------------------------

            1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> run {

                if (sesso == 'F')
                    return@run // Se sei una tipa non vieni pestata...

                Reputazione -= caso
                if (Reputazione < 0)
                    Reputazione = 0

                /* 12 GIUGNO 1998 - LE FINESTRE 100,101 E 102 FANNO CRASCHIARE TUTTO ! */
                /* TEMPORANEA SOLUZIONE: */
                /* i=103 + random(3);	// 103 - 105 <-------------------------------- */

                i = 100 + random(6) /* 100 - 105 */

                tmp = "eventi: Metallaro n. $i"
                writelog(tmp)

                DialogBox(i, hInstance, ::MostraMetallone)

                Tempo_trascorso_dal_pestaggio = 5
            }

            // -------------- Scooter -----------------------------------------------------------------------

            11, 12, 13, 14, 15, 16, 17, 18, 19, 20 -> {
                if ((ScooterData.stato != -1) && (ScooterData.attivita == 1)) {

                    if (CellularData.stato > -1) { // A furia di prendere botte, il cellulare si spacca...
                        CellularData.stato -= random(8)
                        // 0 = 'morente', -1 = 'morto'
                        if (CellularData.stato < 0)
                            CellularData.stato = 0
                    }

                    if (caso < 17) {
                        ScooterData.stato -= 35
                        // Camionista ---------------------
                        DialogBox(106, hInstance, ::MostraMetallone)
                        writelog("eventi: Scooter - Camionista...")
                    } else {
                        ScooterData.stato -= 20
                        // Muro ! --------------------------
                        DialogBox(107, hInstance, ::MostraMetallone)
                        writelog("eventi: Scooter - Muro...")
                    }

                    Reputazione -= 2
                    if (Reputazione < 0)
                        Reputazione = 0

                    if (ScooterData.stato <= 0) {
                        MessageBox(
                            hInstance,
                            "Quando ti rialzi ti accorgi che il tuo scooter e' ormai ridotto ad un ammasso di rottami.",
                            "Scooter Distrutto", MB_OK or MB_ICONSTOP
                        )
                        ScooterData.stato = -1
                        ScooterData.attivita = 0
                        ScooterData = ScooterMem[0].copy()
                        writelog("eventi: Lo scooter si e' completamente distrutto...")
                    }
                }
            }

            // -------------- Figosita' --------------------------------------------------------------------

            // ATTENZIONE: nel C i case 21..30 cadono l' uno nell' altro (nessun break):
            //   21,22,23 -> Fama -= 5, poi Fama -= 1, poi Fama -= 1
            //   24,25    -> Fama -= 1, poi Fama -= 1
            //   26,27    -> Fama -= 1
            //   28,29,30 -> solo il blocco comune
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30 -> {
                if (caso in 21..23) // case 21, 22, 23
                    Fama -= 5
                if (caso in 21..25) // case 24, 25 (+ fallthrough da 21, 22, 23)
                    Fama -= 1
                if (caso in 21..27) // case 26, 27 (+ fallthrough da 21..25)
                    Fama -= 1

                messaggio = LoadString(1000 + caso)
                tmp = "Sei fortunat$ao..."
                MessageBox(
                    hInstance,
                    messaggio, tmp, MB_OK or MB_ICONSTOP
                )
                Fama -= 2
                if (Fama < 0)
                    Fama = 0

                writelog("eventi: Evento riguardante la figosita'...")
            }

            // -------------- Skuola --------------------------------------------------------------------------

            31, 32, 33, 34, 35, 36, 37, 38, 39,
            40 -> { // Durante i giorni di vacanza non ci sono eventi riguardanti la scuola
                if (x_vacanza == 0) {
                    i = random(9) + 1 // Fino alla versione 0.5 c'era scritto 10 ed era un bug...
                    messaggio = LoadString(1000 + caso)
                    messaggio += MaterieMem[i].nome
                    MessageBox(
                        hInstance,
                        messaggio,
                        "Scuola...", MB_OK or MB_ICONSTOP
                    )

                    if (MaterieMem[i].xxx >= 2)
                        MaterieMem[i].xxx -= 2
                    CalcolaStudio()
                    writelog("eventi: Evento riguardante la scuola")
                    ScuolaRedraw = 1 /* E' necessario ridisegnare la finestra della scuola... */
                }
            }

            // -------------- Tipa/o ---------------------------------------------------------------------------

            41,
            42 -> run { // Una tipa/o ci prova... 7 Maggio 1999

                if (Fama < 35)
                    return@run // Figosita' < 35 = nessuna speranza...

                figTemp = random(Fama - 30) + 30 // Figosita' minima tipa = 30...

                if (sesso == 'M') {
                    nomeTemp = LoadString(200 + random(20)) // 200 -> 219 [nomi tipe]
                    tmp = "Una tipa, di nome $nomeTemp (Figosita' $figTemp/100), ci prova con te'...\nCi stai ???"
                } else {
                    nomeTemp = LoadString(1200 + random(20)) // 200 -> 219 [nomi tipi]
                    tmp = "Una tipo, di nome $nomeTemp (Figosita' $figTemp/100), ci prova con te'...\nCi stai ???"
                }

                if (MessageBox(hInstance, tmp, "Qualcuno ti caga...", MB_YESNO) == IDNO) {
                    if ((figTemp >= 79) && (Rapporti < 1) && (sesso == 'M')) { // Se non hai gia' una tipa e rifiuti una figona...
                        MessageBox(
                            hInstance,
                            "Appena vengono a sapere che non ti vuoi mettere insieme ad una figona come quella, i tuoi amici ti prendono a scarpate.",
                            "Idiota...", MB_OK or MB_ICONSTOP
                        )
                        Reputazione -= 4
                        if (Reputazione < 0)
                            Reputazione = 0
                    }
                    return@run
                }

                // Controlla che tu non abbia gia' una tipa -------------------------
                if (Rapporti > 0) { // hai gia' una tipa..<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    if (sesso == 'M')
                        DialogBox(92, hInstance, ::DueDonne)
                    else
                        DialogBox(192, hInstance, ::DueDonne)
                } else { // bravo, no hai una tipa...<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                    Nometipa = nomeTemp
                    FigTipa = figTemp
                    Rapporti = 45 + random(15)
                    Fama += FigTipa / 10
                    if (Fama > 100)
                        Fama = 100
                    Reputazione += FigTipa / 13
                    if (Reputazione > 100)
                        Reputazione = 100
                }

                writelog("eventi: Una tipa//o ci prova...")
            }

            43, // Domande inutili... 11 Giugno 1999
            44 -> {

                if ((Rapporti > 0) && (sesso == 'M')) {
                    if (caso == 43) {
                        i = MessageBox(
                            hInstance,
                            "Mi ami ???",
                            "Domande inutili della Tipa...", MB_YESNO or MB_ICONQUESTION
                        )
                        if (i != IDYES) {
                            MessageBox(
                                hInstance,
                                "Sei sempre il solito stronzo.. non capisco perche' resto ancora con uno come cosi'...",
                                "Risposta sbagliata...", MB_OK or MB_ICONSTOP
                            )
                            Rapporti -= 45
                            if (Rapporti < 5)
                                Rapporti = 5
                        }
                    } else {
                        i = MessageBox(
                            hInstance,
                            "Ma sono ingrassata ???",
                            "Domande inutili della Tipa...", MB_YESNO or MB_ICONQUESTION
                        )
                        if (i != IDNO) {
                            MessageBox(
                                hInstance,
                                "Sei un bastardo, non capisci mai i miei problemi...",
                                "Risposta sbagliata...", MB_OK or MB_ICONSTOP
                            )
                            Rapporti -= 20
                            if (Rapporti < 5)
                                Rapporti = 5
                        }
                    }
                }

                writelog("eventi: Domande inutili della tipa...")
            }

            45, 46, 47, 48 -> {
                writelog("eventi: Evento riguardante la tipa//o (da fare...)")
            }

            // -------------- Vari ed eventuali ----------------------------------------------------------------

            49, 50 -> {
                if (CellularData.stato > -1) {
                    CellularData.stato -= random(8)
                    // 0 = 'morente', -1 = 'morto'
                    if (CellularData.stato < 0)
                        CellularData.stato = 0
                    MessageBox(
                        hInstance,
                        "Il telefonino di cade di tasca e vola per terra...",
                        "Telefonino", MB_OK or MB_ICONSTOP
                    )
                    writelog("eventi: Telefonino - Cade...")
                }
            }

            else -> {
            }
        }
    }

    SalvaTutto() // #ifdef TABBOZ_EM
}

//
//	MOSTRA METALLONE, non e' solo per i metalloni, e' molto utile anche per mostrare
//	tutte quelle finestre in cui non c'e' altro di particolare se non il pulsante di
//	[OK] ed un nome casuale di [VIA]...
//

suspend fun MostraMetallone(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        tmp = LoadString(450 + random(50))

        /* 15 giugno 1998 - La prima lettera viene scritta minuscola (appare "via..." al posto di "Via..." ) */

        tmp = tmp.replaceFirstChar { it.lowercaseChar() }
        SetDlgItemText(hDlg, 111, tmp)

        if (sound_active != 0)
            TabbozPlaySound(1400)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {

            IDCANCEL, IDOK -> {
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}
