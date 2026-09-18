@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997-2000 by Andrea Bonomi
// 31 Maggio 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di tipa.c (figTemp, nomeTemp e tipahDlg sono in GameState.kt).

/** `static int spostamento`. */
private var spostamento = 0

/** `static char descrizione[30]`. */
private var descrizione = ""

/** `static int i` di CercaTipa. */
private var CercaTipa_i = 0

/** `static int i` di DueDiPicche. */
private var DueDiPicche_i = 0

// ------------------------------------------------------------------------------------------
// Tipa...
// ------------------------------------------------------------------------------------------

suspend fun Tipa(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var buf: String
    var tmp: String
    var lasciaoraddoppia: Int

    when (message) {

        WM_INITDIALOG -> {
            if (sesso == 'M')
                spostamento = 0
            else
                spostamento = 100
            AggiornaTipa(hDlg)
            tipahDlg = hDlg
            return true
        }

        WM_COMMAND -> {
            when (wParam) {
                110 -> { // Cerca tipa
                    DialogBox(CERCATIPA + spostamento, hDlg, ::CercaTipa)

                    AggiornaTipa(hDlg)
                    return true
                }

                111 -> { // Lascia tipa
                    if (Rapporti <= 0) {
                        if (sesso == 'M')
                            MessageBox(
                                hDlg,
                                "Scusa, che ragazza avresti intenzione di lasciare ???",
                                "Lascia Tipa", MB_OK or MB_ICONINFORMATION
                            )
                        else
                            MessageBox(
                                hDlg,
                                "Scusa, che tipo avresti intenzione di lasciare, visto che sei sola come un cane ???",
                                "Lascia Tipo", MB_OK or MB_ICONINFORMATION
                            )
                        return true
                    }

                    tmp = "Sei proprio sicuro di voler lasciare $Nometipa ?"

                    if (sesso == 'M')
                        lasciaoraddoppia = MessageBox(
                            hDlg,
                            tmp,
                            "Lascia tipa", MB_YESNO or MB_ICONQUESTION
                        )
                    else
                        lasciaoraddoppia = MessageBox(
                            hDlg,
                            tmp,
                            "Molla tipo", MB_YESNO or MB_ICONQUESTION
                        )

                    if (lasciaoraddoppia == IDYES) {
                        if (sound_active != 0)
                            TabbozPlaySound(603)
                        Rapporti = 0

                        if ((FigTipa >= 79) && (sesso == 'M')) {
                            MessageBox(
                                hDlg,
                                "Appena vengono a sapere quello che hai fatto, i tuoi amici ti prendono a scarpate.\nQualcuno, piu' furbo di te, va a consolarla...",
                                "Idiota...", MB_OK or MB_ICONINFORMATION
                            )
                            Reputazione -= 8
                            if (Reputazione < 0)
                                Reputazione = 0
                        }

                        if ((FigTipa <= 40) && (sesso == 'M')) {
                            Reputazione += 4
                            if (Reputazione > 100)
                                Reputazione = 100
                        }

                        Evento(hDlg)
                    }
                    AggiornaTipa(hDlg)
                    return true
                }

                112 -> {
                    if (Rapporti <= 0) {
                        if (sesso == 'M')
                            MessageBox(
                                hDlg,
                                "Scusa, che ragazza vorresti chiamare ???",
                                "Non sei molto intelligente...", MB_OK or MB_ICONINFORMATION
                            )
                        else
                            MessageBox(
                                hDlg,
                                "Scusa, che ragazzo vorresti chiamare, visto che sei sola ???",
                                "Non sei molto intelligente...", MB_OK or MB_ICONINFORMATION
                            )
                        return true
                    }

                    if ((Soldi <= 5) && ((AbbonamentData.creditorest < 2) && (CellularData.stato < 0))) {
                        MessageBox(
                            hDlg,
                            "" +
                                "Sei fai ancora una telefonata, ti spezzo le gambe" +
                                ", disse tuo padre con un accetta in mano...",
                            "Non toccare quel telefono...", MB_OK or MB_ICONSTOP
                        )
                    } else {
                        if (sound_active != 0)
                            TabbozPlaySound(602)
                        // 5 Maggio 1999 - Telefono di casa o telefonino ???

                        if ((AbbonamentData.creditorest >= 2) && (CellularData.stato > -1))
                            AbbonamentData.creditorest -= 2
                        else
                            Soldi -= 5

                        tmp = "tipa: Telefona alla tipa//o (${MostraSoldi(5)})"
                        writelog(tmp)

                        if (Rapporti <= 60)
                            Rapporti++
                    }
                    AggiornaTipa(hDlg)
                    return true
                }

                113 -> {
                    if (Rapporti <= 0) {
                        if (sesso == 'M')
                            MessageBox(
                                hDlg,
                                "Scusa, con che tipa vorresti uscire ???",
                                "Non sei molto intelligente...", MB_OK or MB_ICONINFORMATION
                            )
                        else
                            MessageBox(
                                hDlg,
                                "Scusa, ma con chi vorresti uscire, ???",
                                "Non sei molto intelligente...", MB_OK or MB_ICONINFORMATION
                            )
                        return true
                    }

                    if ((ScooterData.stato <= 0) && (sesso == 'M')) {
                        MessageBox(
                            hDlg,
                            "Finche' non comprerai lo scooter, non usciremo piu' insieme...",
                            "Compra lo scooter", MB_OK or MB_ICONSTOP
                        )
                        return true
                    }

                    if ((ScooterData.attivita != 1) && (sesso == 'M')) {
                        buf = "Finche' il tuo scooter restera' ${n_attivita[ScooterData.attivita]} non potremo uscire insieme..."
                        MessageBox(hDlg, buf, "Risistema la scooter", MB_OK or MB_ICONINFORMATION)
                        return true
                    }

                    if ((ScooterData.stato <= 35) && (sesso == 'M')) {
                        MessageBox(
                            hDlg,
                            "Finche' non riparerai lo scooter, non usciremo piu' insieme...",
                            "Ripara lo scooter", MB_OK or MB_ICONSTOP
                        )
                        return true
                    }

                    if (Soldi < 15) {
                        if (sesso == 'M')
                            MessageBox(
                                hDlg,
                                "Se mi vuoi portare fuori, cerca di avere almeno un po' di soldi...",
                                "Sei povero", MB_OK or MB_ICONSTOP
                            )
                        else
                            MessageBox(
                                hDlg,
                                "Oh tipa... cioe' non posso pagare sempre tutto io, cioe' ohhhh...",
                                "Che palle", MB_OK or MB_ICONSTOP
                            )
                        return true
                    }

                    Soldi -= 15
                    tmp = "tipa: Esci con la tipa/o (${MostraSoldi(15)})"
                    writelog(tmp)

                    Rapporti += 5
                    if (Rapporti > 100)
                        Rapporti = 100

                    if (FigTipa > Fama)
                        Fama++
                    if (Fama > 100)
                        Fama = 100

                    benzina -= 3
                    if (benzina < 1)
                        benzina = 0
                    CalcolaVelocita(hDlg)

                    AggiornaTipa(hDlg)
                    return true
                }

                130 -> {
                    BMPTipaWndProc(hDlg, WM_LBUTTONDOWN, wParam, lParam)
                }

                IDCANCEL, IDOK -> {
                    tipahDlg = null // Non si sa' mai...
                    EndDialog(hDlg, 1)
                    return true
                }

                else -> return true
            }
        }
    }

    return false
}

// ------------------------------------------------------------------------------------------
// Due Donne - 22 Aprile 1999
// ------------------------------------------------------------------------------------------

suspend fun DueDonne(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        tmp = "Resto con $Nometipa"
        SetDlgItemText(hDlg, 2, tmp)
        if (Nometipa == nomeTemp) { // Se le tipe si chiamano tutte e due con lo stesso nome
            if (sesso == 'M')
                tmp = "Preferisco quella nuova"
            else
                tmp = "Preferisco quello nuovo"
        } else
            tmp = "Preferisco $nomeTemp"
        SetDlgItemText(hDlg, 102, tmp)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101 -> { // Ottima scelta...
                if (sesso == 'M')
                    tmp = "Mentre sei appartato con la $nomeTemp, arriva la tua ragazza, $Nometipa, ti tira uno schiaffo e ti lascia." +
                        "Capendo finalmente di che pasta sei fatto, anche la $nomeTemp si allontana..."
                else
                    tmp = "$Nometipa viene a sapere che di $nomeTemp, gli spacca la faccia e ti molla..." +
                        "Dopo questa tragica esperienza anche $nomeTemp sparisce..."

                Rapporti = 0
                Reputazione -= 8
                if (Reputazione < 0)
                    Reputazione = 0
                Fama -= 4
                if (Fama < 0)
                    Fama = 0

                MessageBox(
                    hDlg,
                    tmp,
                    "La vita e' bella", MB_OK or MB_ICONSTOP
                )
                EndDialog(hDlg, 1)
                return true
            }

            102 -> { // Preferisci quella nuova...
                Nometipa = nomeTemp
                FigTipa = figTemp
                Rapporti = 30 + random(15)
                Fama += FigTipa / 10
                if (Fama > 100)
                    Fama = 100
                Reputazione += FigTipa / 13
                if (Reputazione > 100)
                    Reputazione = 100
                EndDialog(hDlg, 1)
                return true
            }

            IDCANCEL -> { // Resti con la tua vecchia ragazza, bravo...
                EndDialog(hDlg, 1)
                return true
            }

            else -> {
                EndDialog(hDlg, 1)
                return true
            }
        }
    }

    return false
}

// ------------------------------------------------------------------------------------------
// Cerca Tipa
// ------------------------------------------------------------------------------------------

suspend fun CercaTipa(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {

        figTemp = random(71) + 30 // 30 -> 100

        if (sesso == 'M') {
            DescrizioneTipa(figTemp)
            CercaTipa_i = 200 + random(20) // 200 -> 219 [nomi tipe]
        } else {
            DescrizioneTipo(figTemp)
            CercaTipa_i = 1200 + random(20) // 1200 -> 1219 [nomi tipi]
        }

        SetDlgItemText(hDlg, 107, descrizione)
        nomeTemp = LoadString(CercaTipa_i)

        tmp = nomeTemp
        SetDlgItemText(hDlg, 105, tmp)

        tmp = "$figTemp/100"
        SetDlgItemText(hDlg, 106, tmp)

        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            101 -> {
                // Calcola se ce la fa o meno con la tipa... ------------------------------------

                if (((figTemp * 2) + random(50)) <= (Fama + Reputazione + random(30))) {
                    // E' andata bene... ----------------------------------------------------

                    if (sesso == 'M')
                        MessageBox(
                            hDlg,
                            "Con il tuo fascino nascosto da tabbozzo, seduci la tipa e ti ci metti insieme.",
                            "E' andata bene !", MB_OK or MB_ICONINFORMATION
                        )
                    else
                        MessageBox(
                            hDlg,
                            "Ora non ti puoi piu' lamentare di essere sola...",
                            "Qualcono ti caga...", MB_OK or MB_ICONINFORMATION
                        )

                    // ...ma comunque controlla che tu non abbia gia' una tipa -------------------------
                    if (Rapporti > 0) { // hai gia' una tipa..
                        DialogBox(92 + spostamento, hDlg, ::DueDonne)
                    } else { // bravo, no hai una tipa...
                        Nometipa = nomeTemp
                        FigTipa = figTemp
                        Rapporti = 30 + random(15)
                        Fama += FigTipa / 10
                        if (Fama > 100)
                            Fama = 100
                        Reputazione += FigTipa / 13
                        if (Reputazione > 100)
                            Reputazione = 100
                    }
                } else {
                    // 2 di picche... -------------------------------------------------------
                    if (sound_active != 0)
                        TabbozPlaySound(601)

                    DialogBox(95, hDlg, ::DueDiPicche)
                }
                Evento(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

// ------------------------------------------------------------------------------------------
// Abbina una descrizione(breve) alla figosita' di una tipa.
// ------------------------------------------------------------------------------------------

fun DescrizioneTipa(f: Int) {
    var buf: String

    if (f > 97)
        buf = "Ultramegafiga"
    else if (f > 90)
        buf = "Fighissima"
    else if (f > 83)
        buf = "Molto figa"
    else if (f > 72)
        buf = "Figa"
    else if (f > 67)
        buf = "Abbastanza Figa"
    else if (f > 55)
        buf = "Interessante"
    else if (f > 45)
        buf = "Passabile"
    else if (f > 35)
        buf = "Puo' piacere.."
    else
        buf = "E' un tipo..."

    descrizione = buf
}

// ------------------------------------------------------------------------------------------
// Abbina una descrizione(breve) alla figosita' di un tipo.
// ------------------------------------------------------------------------------------------

fun DescrizioneTipo(f: Int) {
    var buf: String

    if (f > 97)
        buf = "Ultramegafigo"
    else if (f > 90)
        buf = "Bellissimo"
    else if (f > 83)
        buf = "Molto figo"
    else if (f > 72)
        buf = "Bello"
    else if (f > 67)
        buf = "Abbastanza Figo"
    else if (f > 55)
        buf = "Interessante"
    else if (f > 45)
        buf = "Passabile"
    else if (f > 35)
        buf = "Puo' piacere.."
    else
        buf = "Inutile..."

    descrizione = buf
}

// ------------------------------------------------------------------------------------------
// 2 di picche (la vita e' bella...)
// ------------------------------------------------------------------------------------------

suspend fun DueDiPicche(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) { // un giorno fortunato...
        DDP++            // log due di picche...
        Reputazione -= 2 // decremento reputazione
        if (Reputazione < 0)
            Reputazione = 0

        Fama -= 2 // decremento figosita'
        if (Fama < 0)
            Fama = 0

        // IN QUESTA PARTE C'ERA UN BUG CHE FACEVA CRASCIARE IL TABBOZ SIMULATOR...

        if (sesso == 'M')
            DueDiPicche_i = 300 + random(20) // 300 -> 319 [sfighe varie]
        else
            DueDiPicche_i = 1300 + random(20) // 300 -> 319 [sfighe varie]

        tmp = LoadString(DueDiPicche_i)
        SetDlgItemText(hDlg, 105, tmp)

        DueDiPicche_i = 0

        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            201 -> {
                DueDiPicche_i++
                if (DueDiPicche_i > 5) {
                    tmp = "Fino ad ora hai preso $DDP due di picche !\nNon ti preoccupare, capita a tutti di prendere qualche due di picche nella vita ..."
                    MessageBox(
                        hDlg,
                        tmp, "La vita e' bella...", MB_OK or MB_ICONINFORMATION
                    )
                    DueDiPicche_i = 0
                }
                return true
            }

            IDCANCEL, IDOK -> {
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

// ------------------------------------------------------------------------------------------
// AggiornaTipa
// ------------------------------------------------------------------------------------------

fun AggiornaTipa(hDlg: Dlg) {
    var tmp: String
    if (Rapporti == 0) {
        // NOTA: bug dell' originale, manca l' else (il testo "Cerca Tipo" sovrascrive sempre "Cerca Tipa")
        if (sesso == 'M')
            SetDlgItemText(hDlg, 101, "Cerca Tipa")
        SetDlgItemText(hDlg, 101, "Cerca Tipo")

        tmp = ""
        SetDlgItemText(hDlg, 105, tmp)
        SetDlgItemText(hDlg, 106, tmp)
        SetDlgItemText(hDlg, 107, tmp)
    } else {
        if (sesso == 'M')
            SetDlgItemText(hDlg, 101, "Cerca Nuova Tipa")
        else
            SetDlgItemText(hDlg, 101, "Cerca Nuovo Tipo")
        tmp = Nometipa
        SetDlgItemText(hDlg, 105, tmp)
        tmp = "$FigTipa"
        SetDlgItemText(hDlg, 106, tmp)
        tmp = "$Rapporti/100"
        SetDlgItemText(hDlg, 107, tmp)
    }
    tmp = "$Fama/100"
    SetDlgItemText(hDlg, 104, tmp)
}

// ------------------------------------------------------------------------------------------
// 4 gennaio 1999
// ------------------------------------------------------------------------------------------

suspend fun MostraSalutieBaci(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {

            205, IDOK, IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }
    return false
}
