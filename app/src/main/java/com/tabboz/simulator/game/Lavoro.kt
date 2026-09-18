@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1998-1999 by Andrea Bonomi
// 31 Maggio 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di lavoro.c. Le variabili numeroditta, impegno, stipendio,
// giorni_di_lavoro, punti_scheda, Risposte1/2/3, LavoroMem, scheda, accetto
// e NUM_DITTE sono definite in GameState.kt.

/** `static int Rcheck;` di lavoro.c */
private var Rcheck = 0

/** `static int Lcheck;` di lavoro.c */
private var Lcheck = 0

/********************************************************************/
/* Lavoro...                                                        */
/********************************************************************/

suspend fun GiornoDiLavoro(hDlg: Dlg, s: String): Boolean {
    var tmp: String

    if (numeroditta < 1) {
        tmp = "Forse non ti ricordi che sei disokkupat$ao..."
        MessageBox(hDlg, tmp, s, MB_OK or MB_ICONINFORMATION)
        return true
    }

    if (x_vacanza == 2) {
        tmp = "Arrivat$ao davanti ai cancelli della ditta li trovi inrimediabilmente chiusi..."
        MessageBox(hDlg, tmp, s, MB_OK or MB_ICONINFORMATION)
        return true
    }

    return false
}

suspend fun Lavoro(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    var n_ditta: Int
    var i: Int

    if (message == WM_INITDIALOG) {
        if (sesso == 'M')
            SetDlgItemText(hDlg, 113, "Fai il leccaculo")
        else
            SetDlgItemText(hDlg, 113, "Fai la leccaculo")

        AggiornaLavoro(hDlg)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {

            110 -> { // Cerca Lavoro ----------------------------------------------------------------------------------
                if (x_vacanza == 2) {
                    tmp = "Arrivat$ao davanti ai cancelli della ditta li trovi inrimediabilmente chiusi..."
                    MessageBox(hDlg, tmp, "Cerca Lavoro", MB_OK or MB_ICONINFORMATION)
                    return true
                }

                if (numeroditta > 0) {
                    MessageBox(
                        hDlg,
                        "Forse non ti ricordi che hai gia' un lavoro...",
                        "Cerca Lavoro", MB_OK or MB_ICONINFORMATION
                    )
                    return true
                }

                Rcheck = 0 /* Check per il numero di risposte */
                Lcheck = 0 /* Check delle risposte */

                n_ditta = random(NUM_DITTE) + 1

                DialogBox(389 + n_ditta, hDlg, ::CercaLavoro)

                if (accetto == IDNO) { // Viva la finezza...
                    MessageBox(
                        hDlg,
                        "Allora sparisci...",
                        "Cerca Lavoro", MB_OK or MB_ICONINFORMATION
                    )
                    return true
                }

                scheda = random(9)
                punti_scheda = 0

                DialogBox(200 + scheda, hDlg, ::CercaLavoro)

                /* Facciamo finta che la scheda venga effettivamente tenuta in considerazione...            */
                /* forse in un futuro verranno controllate le risposte, ma per ora non servono a nulla.     */

                /* 24 Maggio 1998 - v0.6.94 */
                /* Le risposte cominciano a venire controllate.... */
                for (i in 0 until 3)
                    if (Risposte1[i] != 0)
                        Rcheck += 1
                for (i in 0 until 3)
                    if (Risposte2[i] != 0)
                        Rcheck += 10
                for (i in 0 until 3)
                    if (Risposte3[i] != 0)
                        Rcheck += 100

                if (Rcheck != 111) {
                    if (sesso == 'M')
                        MessageBox(
                            hDlg,
                            "Mi spieghi perche' dovremmo assumere qualcuno che non e' neanche in grado di mettere delle crocette su un foglio ???",
                            "Sei un po' stupido...", MB_OK or MB_ICONQUESTION
                        )
                    else
                        MessageBox(
                            hDlg,
                            "Signorina,mi spieghi perche' dovremmo assumere qualcuno che non e' neanche in grado di mettere delle crocette su un foglio ???",
                            "Sei un po' stupida...", MB_OK or MB_ICONQUESTION
                        )
                    AggiornaLavoro(hDlg)
                    return true
                }

                if ((Reputazione + Fortuna + random(80)) > random(200)) {
                    impegno = 10 + random(20)
                    giorni_di_lavoro = 1
                    stipendio = 1000 + (random(10) * 100)
                    numeroditta = n_ditta

                    tmp = if (sesso == 'M')
                        "SEI STATO ASSUNTO ! Ora sei un felice dipendente della ${LavoroMem[n_ditta].nome} !"
                    else
                        "SEI STATO ASSUNTA ! Ora sei una felice dipendente della ${LavoroMem[n_ditta].nome} !"

                    MessageBox(hDlg, tmp, "Hai trovato lavoro !", MB_OK or MB_ICONINFORMATION)
                } else {
                    if (sesso == 'M')
                        MessageBox(
                            hDlg,
                            "Mi dispiace ragazzo, ma non sei riuscito a superare il test... Ora puoi anche portare la tua brutta faccia fuori dal mio ufficio, prima che ti faccia buttare fuori a calci... Grazie e arrivederci...",
                            "Cerca Lavoro", MB_OK or MB_ICONINFORMATION
                        )
                    else
                        MessageBox(
                            hDlg,
                            "Mi dispiace signorina, ma non e' riuscito a superare il test... Se ne vada immediatamente, grazie...",
                            "Cerca Lavoro", MB_OK or MB_ICONINFORMATION
                        )

                    if (Reputazione > 10)
                        Reputazione -= 2
                }

                Evento(hDlg)
                AggiornaLavoro(hDlg)
                return true
            }

            111 -> { /* Licenziati ------------------------------------------------------------------------------------ */
                if (GiornoDiLavoro(hDlg, "Licenziati"))
                    return true

                tmp = "Sei proprio sicur$ao di voler dare le dimissioni dalla ${LavoroMem[numeroditta].nome} ?"
                accetto = MessageBox(
                    hDlg,
                    tmp,
                    "Licenziati", MB_YESNO or MB_ICONQUESTION
                )

                if (accetto == IDYES) {
                    numeroditta = 0
                    impegno = 0
                    giorni_di_lavoro = 0
                    stipendio = 0
                    Evento(hDlg)
                }
                AggiornaLavoro(hDlg)
                return true
            }

            112 -> { /* Chiedi aumento salario ------------------------------------------------------------------------ */
                if (GiornoDiLavoro(hDlg, "Chiedi aumento salario"))
                    return true

                if (impegno > 90) {
                    if ((30 + Fortuna) > (30 + random(50))) {
                        tmp = "Forse per questa volta potremmo darti qualcosina in piu'..."
                        MessageBox(
                            hDlg, tmp,
                            "Chiedi aumento salario", MB_OK or MB_ICONINFORMATION
                        )
                        stipendio += ((random(1) + 1) * 100)
                        impegno -= 30
                        Evento(hDlg)
                    } else {
                        if (sesso == 'M')
                            MessageBox(
                                hDlg,
                                "Vedi di scordartelo,bastardo...",
                                "Chiedi aumento salario", MB_OK or MB_ICONHAND
                            )
                        else
                            MessageBox(
                                hDlg,
                                "Vedi di scordartelo,stronzetta...",
                                "Chiedi aumento salario", MB_OK or MB_ICONHAND
                            )

                        impegno -= 20
                        Evento(hDlg)
                    }
                } else {
                    MessageBox(
                        hDlg,
                        "Che cosa vorresti ??? SCORDATELO !!!!",
                        "Chiedi aumento salario", MB_OK or MB_ICONHAND
                    )
                }

                AggiornaLavoro(hDlg)
                return true
            }

            113 -> { /* Fai il leccaculo ------------------------------------------------------------------------------ */
                if (sesso == 'M') {
                    if (GiornoDiLavoro(hDlg, "Fai il leccaculo"))
                        return true
                } else {
                    if (GiornoDiLavoro(hDlg, "Fai la leccaculo"))
                        return true
                }

                if (sound_active != 0)
                    TabbozPlaySound(503)

                if (Reputazione > 20) /* Facendo il leccaculo perdi reputazione e fama... */
                    Reputazione -= 1
                if (impegno < 99)
                    impegno++

                i = random(Fortuna + 3)
                if (i == 0)
                    Evento(hDlg)

                AggiornaLavoro(hDlg)
                return true
            }

            114 -> { /* Elenco ditte ---------------------------------------------------------------------------------- */
                if (numeroditta == 0) {
                    DialogBox(210, hDlg, ::ElencoDitte)
                } else {
                    DialogBox(numeroditta + 289, hDlg, ::CercaLavoro)
                }
                return true
            }

            115 -> { /* Sciopera  ----------------------------------------------------------------------------------  */
                if (GiornoDiLavoro(hDlg, "Sciopera"))
                    return true

                if (sound_active != 0)
                    TabbozPlaySound(502)
                if (Reputazione < 85)
                    Reputazione += 10

                if (impegno > 19)
                    impegno -= 15

                i = random(Fortuna + 3)
                if (i == 0)
                    Evento(hDlg)

                Evento(hDlg)
                AggiornaLavoro(hDlg)
                return true
            }

            116 -> { /* Lavora  ----------------------------------------------------------------------------------  */
                if (GiornoDiLavoro(hDlg, "Lavora"))
                    return true
                if (impegno < 85)
                    impegno++

                if (sound_active != 0)
                    TabbozPlaySound(501)
                Evento(hDlg)
                AggiornaLavoro(hDlg)
                return true
            }

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

/* Cerca Lavoro ------------------------------------------------------------------------------- */

suspend fun CercaLavoro(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        for (i in 0 until 3) /* Azzera le risposte... */
            Risposte1[i] = 0
        for (i in 0 until 3)
            Risposte2[i] = 0
        for (i in 0 until 3)
            Risposte3[i] = 0
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101, 102, 103 -> {
                Risposte1[wParam - 101] = if (Risposte1[wParam - 101] != 0) 0 else 1
                return true
            }

            104, 105, 106 -> {
                Risposte2[wParam - 104] = if (Risposte2[wParam - 104] != 0) 0 else 1
                return true
            }

            107, 108, 109 -> {
                Risposte3[wParam - 107] = if (Risposte3[wParam - 107] != 0) 0 else 1
                return true
            }

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                accetto = IDNO
                return true
            }

            IDOK -> {
                EndDialog(hDlg, 1)
                accetto = IDYES
                return true
            }
        }
    }

    return false
}

fun AggiornaLavoro(hDlg: Dlg) {
    var tmp: String
    if (numeroditta < 1) { // Nessun lavoro
        tmp = ""
        SetDlgItemText(hDlg, 105, tmp) // Ditta
        SetDlgItemText(hDlg, 106, tmp) // Stipendio
        SetDlgItemText(hDlg, 107, tmp) // Impegno
    } else {
        tmp = LavoroMem[numeroditta].nome
        SetDlgItemText(hDlg, 105, tmp)                             // Ditta
        SetDlgItemText(hDlg, 106, MostraSoldi(stipendio.toLong())) // Stipendio
        tmp = "$impegno/100"
        SetDlgItemText(hDlg, 107, tmp) // Impegno
    }
    SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
}

suspend fun ElencoDitte(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) { // Informazioni su ogni ditta
            100, 101, 102, 103, 104, 105, 106, 107, 108, 109 -> {
                DialogBox(wParam + 190, hDlg, ::CercaLavoro)
                return true
            }

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                EndDialog(hDlg, 1)
                return true
            }
        }
    }

    return false
}
