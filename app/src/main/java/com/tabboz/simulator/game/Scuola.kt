@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997-1999 by Andrea Bonomi
//
// Traduzione di scuola.c. La tabella MaterieMem e' definita in GameState.kt
// (i voti, MaterieMem[i].xxx, vengono modificati di proposito).

/********************************************************************/
/* Scuola...                                                        */
/********************************************************************/

suspend fun Scuola(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    var i: Int
    var i2: Int

    if (message == WM_INITDIALOG) {
        scelta = 1
        CheckDlgButton(hDlg, 110, true) /* Seleziona agraria */

        tmp = "Corrompi il prof di ${MaterieMem[1].nome}"
        SetDlgItemText(hDlg, 101, tmp)
        tmp = if (sesso == 'M')
            "Minaccia il prof di ${MaterieMem[1].nome}"
        else
            "Seduci il prof di ${MaterieMem[1].nome}"

        SetDlgItemText(hDlg, 102, tmp)
        tmp = "Studia ${MaterieMem[1].nome}"
        SetDlgItemText(hDlg, 103, tmp)

        CalcolaStudio()
        ScriviVoti(hDlg) /* Scrive i voti, soldi, reputazione e studio nelle apposite caselle */
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101 -> { /* Corrompi i professori */
                if (!CheckVacanza(hDlg)) {
                    i = 30 + (random(30) * 2) /* 21 Apr 1998 - I valori dei soldi e' meglio che siano sempre pari, in modo da facilitare la divisione x gli euro... */
                    tmp = "Ma... forse per ${MostraSoldi(i.toLong())} potrei dimenticare i tuoi ultimi compiti in classe..."
                    i2 = MessageBox(
                        hDlg, tmp,
                        "Corrompi i professori", MB_YESNO or MB_ICONQUESTION
                    )

                    if (i2 == IDYES) {
                        if (Soldi >= i) {
                            Soldi -= i
                            tmp = "scuola: Corrompi un professore per ${MostraSoldi(i.toLong())}"
                            writelog(tmp)
                            MaterieMem[scelta].xxx += 3
                            if (MaterieMem[scelta].xxx > 10)
                                MaterieMem[scelta].xxx = 10
                        } else {
                            if (MaterieMem[scelta].xxx < 2)
                                MaterieMem[scelta].xxx -= 2
                            MessageBox(
                                hDlg,
                                "Cosa ??? Prima cerchi di corrompermi, poi si scopre che non hai abbastanza soldi !!!",
                                "Errore critico", MB_OK or MB_ICONSTOP
                            )
                        }
                    }

                    Evento(hDlg)
                    Aggiorna(hDlg)
                }
                return true
            }

            102 -> { /* Minaccia-Seduci i professori */
                if (!CheckVacanza(hDlg)) {
                    if (sesso == 'M') { // Maschietto - minaccia prof.
                        if ((Reputazione >= 30) || (random(10) < 1)) {
                            MaterieMem[scelta].xxx += 2
                            if (MaterieMem[scelta].xxx > 10)
                                MaterieMem[scelta].xxx = 10
                        } else {
                            if (sound_active != 0)
                                TabbozPlaySound(402)
                            MessageBox(
                                hDlg,
                                "Cosa ??? Credi di farmi paura piccolo pezzettino di letame vestito da zarro... Deve ancora nasce chi puo' minacciarmi...",
                                "Bella figura", MB_OK or MB_ICONINFORMATION
                            )
                            if (Reputazione > 3)
                                Reputazione -= 2

                            if (MaterieMem[scelta].xxx > 2)
                                MaterieMem[scelta].xxx -= 1
                        }
                    } else { // Femminuccia - seduci prof.
                        if ((Fama >= 50) || (random(10) < 2)) {
                            MaterieMem[scelta].xxx += 2
                            if (MaterieMem[scelta].xxx > 10)
                                MaterieMem[scelta].xxx = 10
                        } else {
                            if (sound_active != 0)
                                TabbozPlaySound(402)
                            MessageBox(
                                hDlg,
                                "Infastidito dalla tua presenza, il prof ti manda via a calci.",
                                "Bella figura", MB_OK or MB_ICONINFORMATION
                            )
                            if (Reputazione > 3)
                                Reputazione -= 2

                            if (MaterieMem[scelta].xxx > 2)
                                MaterieMem[scelta].xxx -= 1
                        }
                    }

                    Aggiorna(hDlg)
                    Evento(hDlg)
                }

                return true
            }

            103 -> { /* Studia */
                if (!CheckVacanza(hDlg)) {
                    if (Reputazione > 10) /* Studiare costa fatica... */
                        Reputazione -= 5 /* (oltre che Reputazione e Fama...) */

                    if (Fama > 5)
                        Fama -= 1

                    MaterieMem[scelta].xxx += 1
                    if (MaterieMem[scelta].xxx > 10)
                        MaterieMem[scelta].xxx = 10

                    Aggiorna(hDlg)
                    Evento(hDlg)
                }

                return true
            }

            110, 111, 112, 113, 114, 115, 116, 117, 118 -> {
                scelta = wParam - 109
                tmp = "Corrompi il prof di ${MaterieMem[scelta].nome}"
                SetDlgItemText(hDlg, 101, tmp)
                tmp = if (sesso == 'M')
                    "Minaccia il prof di ${MaterieMem[scelta].nome}"
                else
                    "Seduci il prof di ${MaterieMem[scelta].nome}"
                SetDlgItemText(hDlg, 102, tmp)
                tmp = "Studia ${MaterieMem[scelta].nome}"
                SetDlgItemText(hDlg, 103, tmp)
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

/* Scrive i voti nelle apposite caselle */
fun ScriviVoti(parent: Dlg) {
    var tmp: String
    ScuolaRedraw = 0

    SetDlgItemText(parent, 104, MostraSoldi(Soldi))
    tmp = "$Reputazione/100"
    SetDlgItemText(parent, 105, tmp)
    tmp = "$Studio/100"
    SetDlgItemText(parent, 106, tmp)

    for (i in 1 until 10) {
        tmp = "${MaterieMem[i].xxx}"
        SetDlgItemText(parent, i + 119, tmp)
    }
}

/* Calcola Studio */
fun CalcolaStudio() {
    var i2: Int

    i2 = 0
    for (i in 1 until 10)
        i2 += MaterieMem[i].xxx

    i2 = i2 * 10

    Studio = i2 / 9
}

/* Aggiorna */
fun Aggiorna(parent: Dlg) {
    var tmp: String

    CalcolaStudio()

    if (ScuolaRedraw == 1)
        ScriviVoti(parent)

    SetDlgItemText(parent, 104, MostraSoldi(Soldi))
    tmp = "$Reputazione/100"
    SetDlgItemText(parent, 105, tmp)
    tmp = "$Studio/100"
    SetDlgItemText(parent, 106, tmp)

    tmp = "${MaterieMem[scelta].xxx}"
    SetDlgItemText(parent, scelta + 119, tmp)
}

suspend fun CheckVacanza(parent: Dlg): Boolean {
    if (x_vacanza != 0) {
        MessageBox(
            parent,
            "Non puoi andare a scuola in un giorno di vacanza !",
            "Scuola", MB_OK or MB_ICONINFORMATION
        )
        return true
    } else
        return false
}
