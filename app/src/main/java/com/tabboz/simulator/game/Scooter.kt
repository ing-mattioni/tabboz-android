@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997,1998 by Andrea Bonomi
// 30 Maggio 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di scooter.c
// Le tabelle (ScooterMem, PezziMem, tabella, n_*, benzina, antifurto, showscooter,
// ScooterData) sono in GameState.kt.

/** `static NEWSTSCOOTER ScooterTemp` di AcquistaScooter. */
private var ScooterTemp = NEWSTSCOOTER()

/** `static long offerta` di VendiScooter (importante lo static !!!). */
private var offerta: Long = 0

/** `static long costo` di RiparaScooter (Importante lo static !!!). */
private var costo: Long = 0

// ******************************************************************
// Calcola la velocita' massima dello scooter, sencodo il tipo di marmitta,
// carburatore, etc...

suspend fun CalcolaVelocita(hDlg: Dlg?) {
    /* 28 Novembre 1998 0.81pr Bug ! Se lo scooter era ingrippato, cambiando il filtro
    dell' aria o la marmitta la velocita' diventava un numero negativo... */

    var buf: String

    ScooterData.speed = (ScooterData.marmitta * 5) + (ScooterData.filtro * 5) + tabella[ScooterData.cc + (ScooterData.carburatore * 6)]

    ScooterData.attivita = 1 /* Sano...    */

    /* 26 Novembre 1998 0.81pr  Arggg!!! Un bug !!! */
    /* Se lo scooter sta' per essere comprato, non fa' questi noiosi check... */

    if (showscooter == 0) {
        if (ScooterData.speed <= -500)
            ScooterData.attivita = 3 /* Invasato   */
        else if (ScooterData.speed <= -1)
            ScooterData.attivita = 2 /* Ingrippato */

        if (benzina < 1)
            ScooterData.attivita = 6 /* A secco    */

        if (ScooterData.attivita != 1) {
            buf = "Il tuo scooter e' ${n_attivita[ScooterData.attivita]}."
            MessageBox(hDlg, buf, "Attenzione", MB_OK or MB_ICONINFORMATION)
            return
        }
    }
}

/********************************************************************/
/* Scooter...                                                       */
/********************************************************************/

suspend fun Scooter(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var buf: String
    var tmp: String

    if (message == WM_INITDIALOG) {
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))

        tmp = "Parcheggia scooter"
        SetDlgItemText(hDlg, 105, tmp) /* 7 Maggio 1998 */

        if (ScooterData.stato != -1) {
            AggiornaScooter(hDlg)
            if (ScooterData.attivita == 4) {
                tmp = "Usa scooter"
                SetDlgItemText(hDlg, 105, tmp)
            }
        }

        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101 -> {
                if (x_vacanza == 2) {
                    tmp = "Oh, tip$ao... oggi il concessionario e' chiuso..."
                    MessageBox(hDlg, tmp, "Concessionario", MB_OK or MB_ICONINFORMATION)
                    return true
                }

                DialogBox(ACQUISTASCOOTER, hDlg, ::Concessionario)
                Evento(hDlg)
                AggiornaScooter(hDlg)
                return true
            }

            102 -> { /* Trucca */
                if (ScooterData.stato != -1) {
                    if (x_vacanza != 2) {
                        /* 28 Aprile 1998 La procedura per truccare gli scooter cambia completamente... */
                        DialogBox(73, hDlg, ::TruccaScooter)
                        Evento(hDlg)
                        AggiornaScooter(hDlg)

                        return true
                    } else {
                        tmp = "Oh, tip$ao... oggi il meccanico e' chiuso..."
                        MessageBox(hDlg, tmp, "Trucca lo scooter", MB_OK or MB_ICONINFORMATION)
                    }
                } else
                    MessageBox(
                        hDlg,
                        "Scusa, ma quale scooter avresti intenzione di truccare visto che non ne hai neanche uno ???",
                        "Trucca lo scooter", MB_OK or MB_ICONQUESTION
                    )

                tmp = "Parcheggia scooter" /* 7 Maggio 1998 */
                SetDlgItemText(hDlg, 105, tmp)

                if (ScooterData.attivita == 4) {
                    tmp = "Usa scooter"
                    SetDlgItemText(hDlg, 105, tmp)
                }

                Evento(hDlg)
                return true
            }

            103 -> { /* Ripara */
                if (ScooterData.stato != -1) {
                    if (ScooterData.stato == 100)
                        MessageBox(
                            hDlg,
                            "Che motivi hai per voleer riparare il tuo scooter\nvisto che e' al 100% di efficienza ???",
                            "Ripara lo scooter", MB_OK or MB_ICONQUESTION
                        )
                    else {
                        if (x_vacanza != 2) {
                            DialogBox(RIPARASCOOTER, hDlg, ::RiparaScooter)
                            AggiornaScooter(hDlg)
                        } else {
                            tmp = "Oh, tip$ao... oggi il meccanico e' chiuso..."
                            MessageBox(hDlg, tmp, "Ripara lo scooter", MB_OK or MB_ICONINFORMATION)
                        }
                    }
                    return true
                } else
                    MessageBox(
                        hDlg,
                        "Mi spieghi come fai a farti riparare lo scooter se non lo hai ???",
                        "Ripara lo scooter", MB_OK or MB_ICONQUESTION
                    )
                Evento(hDlg)
                return true
            }

            105 -> { /* Parcheggia / Usa Scooter	7 Maggio 1998 */
                if (ScooterData.stato < 0) {
                    MessageBox(
                        hDlg,
                        "Mi spieghi come fai a parcheggiare lo scooter se non lo hai ???",
                        "Parcheggia lo scooter", MB_OK or MB_ICONQUESTION
                    )
                    return true
                }

                when (ScooterData.attivita) {
                    1 -> {
                        ScooterData.attivita = 4
                        tmp = "Usa scooter"
                        SetDlgItemText(hDlg, 105, tmp)
                    }
                    4 -> {
                        ScooterData.attivita = 1
                        tmp = "Parcheggia scooter"
                        SetDlgItemText(hDlg, 105, tmp)
                    }
                    else -> {
                        buf = "Mi spieghi come fai a parcheggiare lo scooter visto che e' ${n_attivita[ScooterData.attivita]} ???"
                        MessageBox(hDlg, buf, "Parcheggia lo scooter", MB_OK or MB_ICONQUESTION)
                    }
                }

                AggiornaScooter(hDlg)
                return true
            }

            106 -> { /* Fai Benzina	8 Maggio 1998 */
                if (ScooterData.stato < 0) {
                    MessageBox(
                        hDlg,
                        "Mi spieghi come fai a far benzina allo scooter se non lo hai ???",
                        "Fai benza", MB_OK or MB_ICONQUESTION
                    )
                    return true
                }

                when (ScooterData.attivita) {
                    1, 2, 3, 6 -> {
                        if (Soldi < 10) {
                            buf = "Al distributore automatico puoi fare un minimo di ${MostraSoldi(10)} di benzina..."
                            MessageBox(hDlg, buf, "Fai benza", MB_OK or MB_ICONQUESTION)
                        } else {
                            Soldi -= 10
                            tmp = "scooter: Paga benzina (${MostraSoldi(10)})"
                            writelog(tmp)

                            benzina = 50 /* 5 litri, il massimo che puo' contenere... */

                            if (ScooterData.cc == 5)
                                benzina = 850 /* 85 litri, x la macchinina un po' figa... */
                            showscooter = 0
                            CalcolaVelocita(hDlg)

                            buf = "Fai ${MostraSoldi(10)} di benzina e riempi lo scooter..."
                            MessageBox(hDlg, buf, "Fai benza", MB_OK or MB_ICONINFORMATION)
                        }
                    }

                    else -> {
                        buf = "Mi spieghi come fai a far benzina allo scooter visto che e' ${n_attivita[ScooterData.attivita]} ???"
                        MessageBox(hDlg, buf, "Fai benza", MB_OK or MB_ICONQUESTION)
                    }
                }

                AggiornaScooter(hDlg)
                Evento(hDlg)
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

/********************************************************************/
/* Acquista Scooter                                                 */
/********************************************************************/

suspend fun AcquistaScooter(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var num_moto: Int

    if (message == WM_INITDIALOG) {
        scelta = -1
        ScooterTemp = ScooterData.copy()
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            121, 122, 123, 124, 125, 126 -> {
                num_moto = wParam - 120
                scelta = num_moto
                ScooterData = ScooterMem[num_moto].copy()
                showscooter = 1
                CalcolaVelocita(hDlg)
                showscooter = 0

                AggiornaScooter(hDlg)
                ScooterData = ScooterMem[0].copy()
                return true
            }

            IDCANCEL -> {
                scelta = -1
                ScooterData = ScooterTemp.copy()
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                ScooterData = ScooterTemp.copy()
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Vendi Scooter                                                    */
/********************************************************************/

suspend fun VendiScooter(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String /* Arghhhh ! fino alla 0.8.0 qui c'era un 30 che faceva crashiare tutto !!!! */

    if (message == WM_INITDIALOG) {
        if ((ScooterData.attivita == 1) || (ScooterData.attivita == 4)) /* 0.8.1pr 28 Novembre 1998 - Se lo scooter e' sputtanato, vale meno... */
            offerta = ((ScooterData.prezzo / 100) * (ScooterData.stato - 10 - random(10))).toLong()
        else
            offerta = ((ScooterData.prezzo / 100) * (ScooterData.stato - 50 - random(10))).toLong()

        if (offerta < 50)
            offerta = 50 /* se vale meno di 50.000 nessuno lo vuole... */
        /* 0.8.1pr 28 Novembre 1998 - se vale meno di 50.000, viene pagato 50.000      */

        AggiornaScooter(hDlg)

        SetDlgItemText(hDlg, 118, MostraSoldi(offerta))
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                ScooterData = ScooterMem[0].copy() /* nessuno scooter			*/
                benzina = 0                        /* serbatoio vuoto	7 Maggio 1998	*/
                ScooterData.stato = -1

                Soldi = Soldi + offerta

                tmp = "scooter: Vendi lo scooter per ${MostraSoldi(offerta)}"
                writelog(tmp)

                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

/********************************************************************/
/* Ripara Scooter                                                   */
/********************************************************************/

suspend fun RiparaScooter(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        // Calcola il costo della riparazione dello scooter...
        costo = ((ScooterData.prezzo / 100 * (100 - ScooterData.stato)) + 10).toLong()

        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        SetDlgItemText(hDlg, 105, MostraSoldi(costo))

        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {

            IDCANCEL -> {
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                if (costo > Soldi)
                    nomoney(hDlg, SCOOTER)
                else {
                    tmp = "scooter: Paga riparazione (${MostraSoldi(costo)})"
                    writelog(tmp)

                    // Per questa cagata, crascia il tabboz all' uscita...
                    //					if (sound_active) TabbozPlaySound(102);

                    ScooterData.stato = 100
                    Soldi -= costo
                    CalcolaVelocita(hDlg)
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
/* Trucca Scooter                                                   */
/* 28 Aprile 1998 La procedura per truccare gli scooter cambia completamente... */
/********************************************************************/

suspend fun TruccaScooter(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    if (message == WM_INITDIALOG) {
        if (sound_active != 0)
            TabbozPlaySound(101)
        AggiornaScooter(hDlg)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            121, 122, 123, 124 -> {
                DialogBox(wParam - 121 + 74, hDlg, ::CompraUnPezzo)
                /* CalcolaVelocita(hDlg); */
                AggiornaScooter(hDlg)
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

fun AggiornaScooter(hDlg: Dlg) {
    var tmp: String
    SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))

    if (ScooterData.stato != -1) {
        tmp = ScooterData.nome
        SetDlgItemText(hDlg, 116, tmp)
        tmp = "${benzina / 10}.${benzina % 10}l"
        SetDlgItemText(hDlg, 107, tmp)

        SetDlgItemText(hDlg, 110, MostraSpeed())
        SetDlgItemText(hDlg, 111, n_marmitta[ScooterData.marmitta])
        SetDlgItemText(hDlg, 112, n_carburatore[ScooterData.carburatore])
        SetDlgItemText(hDlg, 113, n_cc[ScooterData.cc])
        SetDlgItemText(hDlg, 114, n_filtro[ScooterData.filtro])
        tmp = "${ScooterData.stato}%"
        SetDlgItemText(hDlg, 115, tmp)

        SetDlgItemText(hDlg, 117, MostraSoldi(ScooterData.prezzo.toLong()))
    } else {
        SetDlgItemText(hDlg, 107, "")
        SetDlgItemText(hDlg, 110, "")
        SetDlgItemText(hDlg, 111, "")
        SetDlgItemText(hDlg, 112, "")
        SetDlgItemText(hDlg, 113, "")
        SetDlgItemText(hDlg, 114, "")
        SetDlgItemText(hDlg, 115, "")
        SetDlgItemText(hDlg, 116, "")
        SetDlgItemText(hDlg, 117, "")
    }
}

// -----------------------------------------------------------------------
// Routine di acquisto generika di un pezzo di motorino
// -----------------------------------------------------------------------

suspend fun CompraUnPezzo(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    var i: Int

    if (message == WM_INITDIALOG) {
        tmp = ScooterData.nome
        SetDlgItemText(hDlg, 109, tmp)
        SetDlgItemText(hDlg, 105, n_carburatore[ScooterData.carburatore])
        SetDlgItemText(hDlg, 106, n_marmitta[ScooterData.marmitta])
        SetDlgItemText(hDlg, 107, n_cc[ScooterData.cc])
        SetDlgItemText(hDlg, 108, n_filtro[ScooterData.filtro])

        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))

        i = 110
        while (i < 125) {
            SetDlgItemText(hDlg, i, MostraSoldi(PezziMem[i - 110].toLong()))
            i++
        }

        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            130, 131, 132 -> { /* marmitte ----------------------------------------------------------- */
                if (Soldi < PezziMem[wParam - 130]) {
                    nomoney(hDlg, SCOOTER)
                    return true
                }
                Soldi -= PezziMem[wParam - 130]
                tmp = "scooter: Paga marmitta (${MostraSoldi(PezziMem[wParam - 130].toLong())})"
                writelog(tmp)

                ScooterData.marmitta = (wParam - 129) /* (1 - 3 ) */
                showscooter = 0
                CalcolaVelocita(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            133, 134, 135, 136 -> { /* carburatore -------------------------------------------------------- */
                if (Soldi < PezziMem[wParam - 130]) {
                    nomoney(hDlg, SCOOTER)
                    return true
                }
                Soldi -= PezziMem[wParam - 130]
                tmp = "scooter: Paga carburatore (${MostraSoldi(PezziMem[wParam - 130].toLong())})"
                writelog(tmp)

                ScooterData.carburatore = (wParam - 132) /* ( 1 - 4 ) */
                showscooter = 0
                CalcolaVelocita(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            137, 138, 139, 140 -> { /* cc ----------------------------------------------------------------- */
                if (Soldi < PezziMem[wParam - 130]) {
                    nomoney(hDlg, SCOOTER)
                    return true
                }
                Soldi -= PezziMem[wParam - 130]
                tmp = "scooter: Paga cilindro e pistone (${MostraSoldi(PezziMem[wParam - 130].toLong())})"
                writelog(tmp)

                /* Piccolo bug della versione 0.6.91, qui c'era scritto ScooterData.marmitta */
                /* al posto di ScooterData.cc :-) */
                ScooterData.cc = (wParam - 136) /* ( 1 - 4 ) */
                showscooter = 0
                CalcolaVelocita(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            141, 142, 143, 144 -> { /* filtro dell' aria -------------------------------------------------- */
                if (Soldi < PezziMem[wParam - 130]) {
                    nomoney(hDlg, SCOOTER)
                    return true
                }
                Soldi -= PezziMem[wParam - 130]
                tmp = "scooter: Paga filtro dell' aria (${MostraSoldi(PezziMem[wParam - 130].toLong())})"
                writelog(tmp)

                ScooterData.filtro = (wParam - 140) /* (1 - 4) */
                showscooter = 0
                CalcolaVelocita(hDlg)
                EndDialog(hDlg, 1)
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

// -----------------------------------------------------------------------

fun MostraSpeed(): String {
    var tmp: String

    when (ScooterData.attivita) {
        1 -> tmp = "${ScooterData.speed}Km/h"
        2, 3, 4, 5, 6 -> tmp = "(${n_attivita[ScooterData.attivita]})"
        else -> tmp = ""
    }

    return tmp
}

// -----------------------------------------------------------------------
// Concessionario...  7 Maggio 1998
// -----------------------------------------------------------------------

suspend fun Concessionario(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101, 102 -> { /* Compra Scooter Malagutty / di altre marche */
                scelta = -1
                DialogBox(wParam - 101 + 78, hDlg, ::AcquistaScooter)
                if (scelta != -1) {
                    if (ScooterData.stato != -1) {
                        tmp = "Per il tuo vecchio scooter da rottamare ti diamo ${MostraSoldi(1000)} di supervalutazione..."
                        MessageBox(hDlg, tmp, "Incentivi", MB_OK or MB_ICONINFORMATION)
                        Soldi += 1000
                        tmp = "scooter: Imcentivo rottamazione ${MostraSoldi(1000)}"
                        writelog(tmp)

                        ScooterData = ScooterMem[0].copy() /* nessuno scooter			*/
                        benzina = 0                        /* serbatoio vuoto	7 Maggio 1998	*/
                        ScooterData.stato = -1
                    }
                    if (ScooterMem[scelta].prezzo > Soldi) {
                        MessageBox(
                            hDlg,
                            "Ti piacerebbe comprare lo scooter, vero ?\nPurtroppo, non hai abbastanza soldi...",
                            "Non hai abbastanza soldi", MB_OK or MB_ICONSTOP
                        )
                        if (Reputazione > 3)
                            Reputazione -= 1
                    } else {
                        Soldi -= ScooterMem[scelta].prezzo
                        tmp = "scooter: Acquista uno scooter per ${MostraSoldi(ScooterMem[scelta].prezzo.toLong())}"
                        writelog(tmp)

                        ScooterData = ScooterMem[scelta].copy()
                        benzina = 20
                        MessageBox(
                            hDlg,
                            "Fai un giro del quartiere per farti vedere con lo scooter nuovo...",
                            "Lo scooter nuovo", MB_OK or MB_ICONINFORMATION
                        )
                        Reputazione += 4
                        if (Reputazione > 100)
                            Reputazione = 100
                    }
                    Evento(hDlg)
                }
                SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
                return true
            }

            103 -> {
                if (ScooterData.stato != -1) {
                    DialogBox(VENDISCOOTER, hDlg, ::VendiScooter)
                } else
                    MessageBox(
                        hDlg,
                        "Scusa, ma quale scooter avresti intenzione di vendere visto che non ne hai neanche uno ???",
                        "Vendi lo scooter", MB_OK or MB_ICONQUESTION
                    )

                SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
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
