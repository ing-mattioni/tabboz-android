@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997-1999 by Andrea Bonomi
// 6 Aprile 1999 - Inizio implementazione lettore CDROM (escluso: #define CDROM)
// 30 Maggio 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di disco.c. La tabella DiscoMem e' definita in GameState.kt.

/** `static int numdisco;` di disco.c */
private var numdisco = 0

//********************************************************************
// Routine per il pagamento della Discoteca.
//********************************************************************

private suspend fun PagaDisco(hInstance: Dlg?) {
    var tmp: String
    val Prezzo: Long

    if (numdisco != 0) {
        if (DiscoMem[numdisco].speed == 1) {
            if (ScooterData.stato == -1) {
                MessageBox(
                    hInstance,
                    "Senza lo scooter non puoi andare nelle discoteche fuori porta...",
                    "Discoteca fuori porta", MB_OK or MB_ICONINFORMATION
                )
                Evento(hInstance)
                return
            }
        }

        if (DiscoMem[numdisco].mass == x_giornoset) { /* [24 Marzo 1998] versione 0.6.2a */
            MessageBox(
                hInstance,
                "Un cartello recita che oggi e' il giorno di chiusura settimanale...",
                "Giorno di chiusura", MB_OK or MB_ICONINFORMATION
            )
            return
        }

        Prezzo = if (sesso == 'M')
            DiscoMem[numdisco].prezzo.toLong()
        else
            (DiscoMem[numdisco].prezzo - 10).toLong()

        if (Prezzo > Soldi) { /* check costo */
            nomoney(hInstance, DISCO)
        } else {
            if ((DiscoMem[numdisco].cc > Fama) && (sesso == 'M')) { /* check selezione all'ingresso */
                if (sound_active != 0)
                    TabbozPlaySound(302)
                MessageBox(
                    hInstance,
                    "Mi dispiace signore, conciato cosi', qui non puo' entrare...\nVenga vestito meglio la prossima volta, signore.",
                    "Selezione all' ingresso", MB_OK or MB_ICONINFORMATION
                )
                if (Reputazione > 2)
                    Reputazione -= 1
                if (Fama > 2)
                    Fama -= 1
            } else {
                if (sound_active != 0)
                    TabbozPlaySound(303 + random(3)) // suoni: 0303 -> 0305
                Soldi -= Prezzo
                tmp = "discoteca: Paga ${MostraSoldi(DiscoMem[numdisco].prezzo.toLong())}"
                writelog(tmp)
                Fama += DiscoMem[numdisco].fama
                Reputazione += DiscoMem[numdisco].xxx
                if (Fama > 100)
                    Fama = 100
                if (Reputazione > 100)
                    Reputazione = 100
            }
        }
        Evento(hInstance)
    }
}

//******************************************************************
// Disco...
//******************************************************************

suspend fun Disco(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var buf: String
    var tmp: String

    if (message == WM_INITDIALOG) {
        numdisco = 0
        buf = "O tip$ao, in che disco andiamo ?"
        SetDlgItemText(hDlg, 120, buf)

        /* [24 Marzo 1998] -  Perche' Discoteca era fino ad */
        /* oggi l'unica finestra che non mostrava i soldi   */
        /* che il tabbozzo ha ??? 			    */
        SetDlgItemText(hDlg, 110, MostraSoldi(Soldi))
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101, 102, 103, 104, 105, 106, 107, 108 -> {
                numdisco = wParam - 100
                buf = LoadString(wParam)

                tmp = if (sesso == 'M') // Le donne pagano meno...
                    String.format(buf, MostraSoldi(DiscoMem[numdisco].prezzo.toLong()))
                else
                    String.format(buf, MostraSoldi((DiscoMem[numdisco].prezzo - 10).toLong()))

                SetDlgItemText(hDlg, 120, tmp)
                return true
            }

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                PagaDisco(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}
