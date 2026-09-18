@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997-2000 by Andrea Bonomi
// 5 Giugno 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di tempo.c. Le tabelle InfoMese/InfoSettimana/InfoVacanze e le
// variabili x_giorno, x_mese, x_anno_bisesto, x_giornoset, x_vacanza,
// scad_pal_giorno, scad_pal_mese sono definite in GameState.kt.

/** `static char natale2;` di tempo.c */
private var natale2 = 0

// ------------------------------------------------------------------------------------------
// Giorno...
// ------------------------------------------------------------------------------------------

suspend fun Giorno(hInstance: Dlg?) {
    var tmp: String

    x_giorno++
    if (x_giorno > InfoMese[x_mese - 1].num_giorni) {
        if ((x_mese == 2) && (x_anno_bisesto == 1) && (x_giorno == 29)) {
            MessageBox(
                hInstance, "Anno bisesto, anno funesto...",
                "Anno Bisestile", MB_OK or MB_ICONSTOP
            )
        } else {
            x_giorno = 1
            x_mese += 1
        }
    }

    if (x_mese > 12) {
        x_mese = 1
        x_anno_bisesto = +1
        if (x_anno_bisesto > 3)
            x_anno_bisesto = 0
        /* Capodanno();	*/
    }

    x_giornoset++
    if (x_giornoset > 7) {
        x_giornoset = 1
        if (current_testa > 0) {
            current_testa--  // Ogni 7 giorni diminuisce l' abbronzatura - 26 Feb 1999
            TabbozRedraw = 1 // e si deve aggiornare il disegno... (BUG ! Mancava fino alla versione 0.83pr )
        }
    }

    /* ---------------> S T I P E N D I O <--------------- */

    if (impegno > 0) {
        giorni_di_lavoro += 1 // C' era scritto =+1 al posto di +=1

        if ((x_giorno == 27) && (giorni_di_lavoro > 3)) {
            val stipendietto: Long /* Stipendio calcolato secondo i giorni effettivi di lavoro */

            stipendietto = if (giorni_di_lavoro > 29)
                stipendio.toLong()
            else
                stipendio.toLong() * giorni_di_lavoro.toLong() / 30

            giorni_di_lavoro = 1

            tmp = "Visto che sei stat$ao $un_una brav$ao dipendente sottomess$ao, ora ti arriva il tuo misero stipendio di ${MostraSoldi(stipendietto)}"

            MessageBox(
                hInstance, tmp,
                "Stipendio !", MB_OK or MB_ICONINFORMATION
            )

            Soldi += stipendietto

            tmp = "giorno: Stipendio (${MostraSoldi(stipendietto)})"
            writelog(tmp)
        }
    }

    /* ---------------> P A L E S T R A <---------------    21 Apr 1998	*/

    if (scad_pal_mese == x_mese)
        if (scad_pal_giorno == x_giorno) {
            MessageBox(
                hInstance,
                "E' appena scaduto il tuo abbonamento della palestra...",
                "Palestra", MB_OK or MB_ICONINFORMATION
            )
            scad_pal_giorno = 0
            scad_pal_mese = 0
            writelog("giorno: E' scaduto l' abbonamento alla palestra")
        }

    /* Calcola i giorni di vacanza durante l' anno ( da finire...)	*/
    x_vacanza = 0
    current_tipa = 0

    /* Hai gia' ricevuto gli auguri di natale ??? 04/01/1999*/
    natale2 = 0

    when (x_mese) {
        1 -> { /* Gennaio --------------------------------------------------------- */
            if (x_giorno < 7) {
                x_vacanza = 1
                if (Rapporti > 0)
                    current_tipa = 1 /* 6 Maggio 1999 - Tipa vestita da Babbo Natale...*/
            }
            /* Vacanze di Natale */
        }

        6 -> { /* Giugno ---------------------------------------------------------- */
            if (x_giorno == 15)
                MessageBox(
                    hInstance,
                    "Da domani iniziano le vacanza estive !",
                    "Ultimo giorno di scuola", MB_OK or MB_ICONINFORMATION
                )

            if (x_giorno == 22) { /* Pagella */
                DialogBox(110, hInstance, ::MostraPagella)
            }
            if (x_giorno > 15)
                x_vacanza = 1
        }

        7, /* Luglio e */
        8 -> { /* Agosto   */
            x_vacanza = 1
            if ((Rapporti > 93) && (FigTipa > 95))
                current_tipa = 2 /* 6 Maggio 1999 - Tipa al mare...*/
        }

        9 -> { /* Settembre ------------------------------------------------------- */
            if (x_giorno < 15)
                x_vacanza = 1
            if (x_giorno == 15) {
                MessageBox(
                    hInstance,
                    "Questa mattina devi tornare a scuola...",
                    "Primo giorno di scuola", MB_OK or MB_ICONINFORMATION
                )
                Studio = 0 /* Azzera le materie...	*/
                for (i in 1 until 10)
                    MaterieMem[i].xxx = 0
            }
        }

        12 -> { /* Dicembre -------------------------------------------------------- */
            if (x_giorno > 22) {
                x_vacanza = 1 /* Vacanze di Natale */
                if (Rapporti > 0)
                    current_tipa = 1 /* 6 Maggio 1999 - Tipa vestita da Babbo Natale...*/
            }

            if (x_giorno == 25) {
                if ((current_pantaloni == 19) && (current_gibbotto == 19)) {
                    MessageBox(
                        hInstance,
                        "Con il tuo vestito da Babbo Natale riesci a stupire tutti...",
                        "Natale...", MB_OK or MB_ICONINFORMATION
                    )
                    Fama += 20
                    if (Fama > 100)
                        Fama = 100
                }
                // #ifdef VERAMENTE_INUTILE: Buon Natale dalla tipa (04/01/1999) - escluso
            }

            if ((x_giorno == 28) && ((current_pantaloni == 19) || (current_gibbotto == 19))) {
                MessageBox(
                    hInstance,
                    "Natale e' gia' passato... Togliti quel dannato vestito...",
                    "Natale...", MB_OK or MB_ICONINFORMATION
                )
                Fama -= 5
                if (Fama < 0)
                    Fama = 0
            }
        }
    }

    /* Domeniche e festivita' varie				VACANZE DI TIPO 2 */

    if (x_giornoset == 7)
        x_vacanza = 2 /* Domenica */

    if (natale2 == 0) {
        for (a in InfoVacanze.indices) {
            if (InfoVacanze[a].mese == x_mese)
                if (InfoVacanze[a].giorno == x_giorno) {
                    MessageBox(
                        hInstance,
                        InfoVacanze[a].descrizione,
                        InfoVacanze[a].nome, MB_OK or MB_ICONINFORMATION
                    )

                    x_vacanza = 2 /* 2 = sono chiusi anche i negozi... */
                }
        }
    }

    /* Mostra data e soldi */
    tmp = "giorno: ${InfoSettimana[x_giornoset - 1].nome} $x_giorno ${InfoMese[x_mese - 1].nome}, ${MostraSoldi(Soldi)}"
    writelog(tmp)
}

// ------------------------------------------------------------------------------------------
// Mostra la pagella...
// ------------------------------------------------------------------------------------------

suspend fun MostraPagella(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    var k: Int

    if (message == WM_INITDIALOG) {
        k = 0
        for (i in 1 until 10) {
            if (MaterieMem[i].xxx < 6)
                k++ /* k = materie insuff o grav. insuf. */
            if (MaterieMem[i].xxx < 4)
                k++ /* k = materie insuff o grav. insuf. */
            tmp = "${MaterieMem[i].xxx}"
            SetDlgItemText(hDlg, i + 119, tmp)
        }

        if (Fama > 75) // Condotta... + un e' figo, + sembra un bravo ragazzo...
            SetDlgItemText(hDlg, 129, "8")
        else
            SetDlgItemText(hDlg, 129, "9")

        if (k > 4) {
            if (sound_active != 0)
                TabbozPlaySound(401)
            tmp = "NON ammess$ao" /* bocciata/o */
            writelog("giorno: Pagella... Bocciato !!!")
        } else {
            tmp = "ammess$ao" /* promossa/o */
            Soldi += 200
            writelog("giorno: Pagella... Promosso...")
        }

        SetDlgItemText(hDlg, 119, tmp)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
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
