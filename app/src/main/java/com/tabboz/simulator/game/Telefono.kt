@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1999 by Andrea Bonomi
// Iniziato il 31 Marzo 1999
// 31 Maggio 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di telefono.c (CellularData/CellularMem e AbbonamentData/AbbonamentMem
// sono in GameState.kt).

/** `static int scelta = 0` di CompraCellulare (nasconde la globale scelta). */
private var CompraCellulare_scelta = 0

/** `static int scelta = 0` di AbbonaCellulare (nasconde la globale scelta). */
private var AbbonaCellulare_scelta = 0

// ------------------------------------------------------------------------------------------
//  Controlla se e' un giorno di vacanza...
// ------------------------------------------------------------------------------------------

private suspend fun CellularVacanza(hDlg: Dlg): Int {
    if (x_vacanza != 2)
        return 0
    else {
        MessageBox(
            hDlg,
            "Stranamente, in un giorno di vacanza, il negozio e' chiuso...",
            "Telefonino", MB_OK or MB_ICONINFORMATION
        )
        return -1
    }
}

// ------------------------------------------------------------------------------------------
//  Compra Cellulare
// ------------------------------------------------------------------------------------------

suspend fun CompraCellulare(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean { /* 31 Marzo 1999 */
    var i: Int

    if (message == WM_INITDIALOG) {
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        i = 0
        while (i < 3) {
            SetDlgItemText(hDlg, 120 + i, MostraSoldi(CellularMem[i].prezzo.toLong()))
            i++
        }
        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {
            110, 111, 112 -> {
                CompraCellulare_scelta = wParam - 110
                return true
            }

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                if (Soldi < CellularMem[CompraCellulare_scelta].prezzo) { // Controlla se ha abbastanza soldi...
                    nomoney(hDlg, CELLULRABBONAM)
                    EndDialog(hDlg, 1)
                    return true
                }

                Soldi -= CellularMem[CompraCellulare_scelta].prezzo
                CellularData = CellularMem[CompraCellulare_scelta].copy()
                Fama += CellularMem[CompraCellulare_scelta].fama
                if (Fama > 100)
                    Fama = 100

                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

// ------------------------------------------------------------------------------------------
//  Abbonamento
// ------------------------------------------------------------------------------------------

suspend fun AbbonaCellulare(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean { /* 31 Marzo 1999 */
    var tmp: String
    var i: Int

    if (message == WM_INITDIALOG) {
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        if (AbbonamentData.creditorest > -1) {
            tmp = AbbonamentData.nome
            SetDlgItemText(hDlg, 105, tmp)
        }
        i = 0
        while (i < 9) {
            SetDlgItemText(hDlg, 110 + i, MostraSoldi(AbbonamentMem[i].prezzo.toLong()))
            i++
        }
        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {

            110, 111, 112, 113, 114, 115, 116, 117, 118 -> {
                AbbonaCellulare_scelta = wParam - 110
                return true
            }

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                if (Soldi < AbbonamentMem[AbbonaCellulare_scelta].prezzo) { // Controlla se ha abbastanza soldi...
                    nomoney(hDlg, CELLULRABBONAM)
                    EndDialog(hDlg, 1)
                    return true
                }

                if (AbbonamentMem[AbbonaCellulare_scelta].abbonamento == 1) { // Abbonamento, no problem...
                    Soldi -= AbbonamentMem[AbbonaCellulare_scelta].prezzo
                    AbbonamentData = AbbonamentMem[AbbonaCellulare_scelta].copy()
                    if ((sound_active != 0) && (CellularData.stato > -1))
                        TabbozPlaySound(602)

                    EndDialog(hDlg, 1)
                } else { // Ricarica...
                    if ((AbbonamentData.creditorest > -1) &&
                        (AbbonamentData.nome == AbbonamentMem[AbbonaCellulare_scelta].nome)
                    ) {
                        Soldi -= AbbonamentMem[AbbonaCellulare_scelta].prezzo
                        AbbonamentData.creditorest += AbbonamentMem[AbbonaCellulare_scelta].creditorest
                        if ((sound_active != 0) && (CellularData.stato > -1))
                            TabbozPlaySound(602)
                        EndDialog(hDlg, 1)
                    } else
                        MessageBox(
                            hDlg,
                            "Oh, che  te ne fai di una ricarica se non hai la sim ???",
                            "Telefonino", MB_OK or MB_ICONINFORMATION
                        )
                }
                return true
            }

            else -> return true
        }
    }

    return false
}

// ------------------------------------------------------------------------------------------
//  Cellulare
// ------------------------------------------------------------------------------------------

fun AggiornaCell(hDlg: Dlg) {
    var tmp: String
    SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))

    if (CellularData.stato > -1) {
        tmp = CellularData.nome
        SetDlgItemText(hDlg, 120, tmp)
    } else {
        SetDlgItemText(hDlg, 120, null)
    }

    if (AbbonamentData.creditorest > -1) {
        tmp = AbbonamentData.nome
        SetDlgItemText(hDlg, 121, tmp)                                     // Abbonamento
        SetDlgItemText(hDlg, 122, MostraSoldi(AbbonamentData.creditorest.toLong())) // Credito
    } else {
        SetDlgItemText(hDlg, 121, null)
        SetDlgItemText(hDlg, 122, null)
    }
}

suspend fun Cellular(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean { /* 31 Marzo 1999 */
    var tmp: String
    var offerta: Long
    var scelta: Int
    if (message == WM_INITDIALOG) {
        AggiornaCell(hDlg)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            110 -> {
                if (CellularVacanza(hDlg) == 0) {
                    DialogBox(COMPRACELLULAR, hDlg, ::CompraCellulare)
                    AggiornaCell(hDlg)
                }
                return true
            }

            111 -> {
                if (CellularVacanza(hDlg) == 0) {
                    if (CellularData.stato > -1) {
                        offerta = (CellularData.prezzo / 2 + 15).toLong()
                        tmp = "Ti posso dare ${MostraSoldi(offerta)} per il tuo telefonino... vuoi vendermelo ?"
                        scelta = MessageBox(
                            hDlg,
                            tmp,
                            "Telefonino", MB_YESNO or MB_ICONQUESTION
                        )
                        if (scelta == IDYES) {
                            CellularData.stato = -1
                            Soldi += offerta
                        } else
                            MessageBox(
                                hDlg,
                                "Allora vai a farti fottere, pirletta !",
                                "Telefonino", MB_OK or MB_ICONINFORMATION
                            )
                    } else {
                        MessageBox(
                            hDlg,
                            "Che telefonino vuoi vendere, pirletta ?",
                            "Telefonino", MB_OK or MB_ICONINFORMATION
                        )
                    }
                    AggiornaCell(hDlg)
                }
                return true
            }

            112 -> {
                if (CellularVacanza(hDlg) == 0) {
                    DialogBox(CELLULRABBONAM, hDlg, ::AbbonaCellulare)
                    AggiornaCell(hDlg)
                }
                return true
            }

            150, IDOK, IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}
