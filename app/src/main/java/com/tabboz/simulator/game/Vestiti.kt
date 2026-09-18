@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

// Tabboz Simulator
// (C) Copyright 1997-2000 by Andrea Bonomi
// 31 Maggio 1999 - Conversione Tabbozzo -> Tabbozza
//
// Traduzione di vestiti.c (VestitiMem, PalestraMem e SizeMem sono in GameState.kt).

/* Vestito da Babbo Natale... 11 Marzo 1999 */
private const val COSTO_VESTITO_NATALIZIO = 58

/********************************************************************/
/* Vestiti...                                                       */
/********************************************************************/

suspend fun Vestiti(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String

    if (message == WM_INITDIALOG) {
        SetDlgItemText(hDlg, 120, MostraSoldi(Soldi))

        if ((x_mese == 12) && (Soldi >= COSTO_VESTITO_NATALIZIO))
            if ((x_giorno > 14) && (x_giorno < 25) && (current_gibbotto != 19) && (current_pantaloni != 19)) {
                var scelta: Int
                tmp = "Vuoi comperare, per ${MostraSoldi(COSTO_VESTITO_NATALIZIO.toLong())}, un meraviglioso vestito da Babbo Natale ?"
                scelta = MessageBox(
                    hDlg,
                    tmp,
                    "Offerte Natalizie...", MB_YESNO or MB_ICONQUESTION
                )
                if (scelta == IDYES) {
                    current_gibbotto = 19
                    current_pantaloni = 19
                    TabbozRedraw = 1 // E' necessario ridisegnare l' immagine del Tabbozzo...
                    Soldi -= COSTO_VESTITO_NATALIZIO
                }
            }

        return true
    } else if (message == WM_COMMAND) {

        when (wParam) {

            101, 102, 103, 104, 105 -> { // MAKEINTRESOURCE 80 / 81 / 82 / 83 / 84
                RunVestiti(hDlg, (wParam - 21))
                SetDlgItemText(hDlg, 120, MostraSoldi(Soldi))
                return true
            }

            110 -> { // Tabaccaio
                RunTabacchi(hDlg)
                SetDlgItemText(hDlg, 120, MostraSoldi(Soldi))
                return true
            }

            111 -> { // Palestra
                RunPalestra(hDlg)
                SetDlgItemText(hDlg, 120, MostraSoldi(Soldi))
                return true
            }

            112 -> { // Cellulare
                DialogBox(CELLULAR, hDlg, ::Cellular)

                SetDlgItemText(hDlg, 120, MostraSoldi(Soldi))
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

//*******************************************************************
// Routine per il pagamento di qualunque cosa...
//*******************************************************************

suspend fun PagaQualcosa(hInstance: Dlg?) {
    var tmp: String

    if (scelta != 0) {
        if (VestitiMem[scelta].prezzo > Soldi) {
            nomoney(hInstance, VESTITI)
        } else {
            Soldi -= VestitiMem[scelta].prezzo

            when (scelta) { /* 25 Febbraio 1999 */
                1, 2, 3, 4, 5, 6 -> current_gibbotto = scelta
                7, 8, 9, 10 -> current_pantaloni = scelta - 6
                11, 12, 13, 14, 15, 16, 17 -> current_scarpe = scelta - 10
            }

            TabbozRedraw = 1 // E' necessario ridisegnare l' immagine del Tabbozzo...

            tmp = "vestiti: Paga ${MostraSoldi(VestitiMem[scelta].prezzo.toLong())}"
            writelog(tmp)

            Fama += VestitiMem[scelta].fama
            if (Fama > 100)
                Fama = 100
        }
        Evento(hInstance)
    }
}

//*******************************************************************
// Routine di acquisto generica
//*******************************************************************

suspend fun CompraQualcosa(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    var i: Int

    if (message == WM_INITDIALOG) {
        scelta = 0
        SetDlgItemText(hDlg, 120, MostraSoldi(Soldi))
        tmp = "$Fama/100"
        SetDlgItemText(hDlg, 121, tmp)

        i = 101
        while (i < 120) {
            // NOTA: il C legge anche VestitiMem[18] e VestitiMem[19], che sono oltre la fine
            // della tabella (in Kotlin darebbe ArrayIndexOutOfBoundsException). I controlli
            // 118 e 119 non esistono in questi dialoghi, quindi il testo non verrebbe usato.
            if ((i - 100) < VestitiMem.size)
                SetDlgItemText(hDlg, i, MostraSoldi(VestitiMem[i - 100].prezzo.toLong()))
            i++
        }
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
            111, 112, 113, 114, 115, 116, 117, 118, 119 -> {
                scelta = wParam - 100
                return true
            }

            IDCANCEL -> {
                scelta = 0
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                PagaQualcosa(hDlg)
                EndDialog(hDlg, 1)
                return true
            }

            else -> return true
        }
    }

    return false
}

//*******************************************************************
// Tabaccaio ! (che centra tra i vestiti ??? Come procedura e' simile...)
//*******************************************************************

suspend fun Tabaccaio(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp_descrizione: String
    var tmp: String
    var nico_quot: Int
    var nico_rem: Int
    var i: Int

    if (message == WM_INITDIALOG) {
        if (sound_active != 0)
            TabbozPlaySound(203)
        scelta = -1 // Fino alla 0.8.51pr c'era un bug che non faceva comprare le Barclay...
        SetDlgItemText(hDlg, 104, MostraSoldi(Soldi))
        tmp = "$sizze"
        SetDlgItemText(hDlg, 105, tmp)
        tmp = "Che sigarette vuoi, ragazz$ao ?"
        SetDlgItemText(hDlg, 106, tmp)
        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411,
            412, 413, 414, 415, 416, 417, 418, 419, 420, 421, 422, 423 -> {
                scelta = wParam - 400
                tmp = LoadString(wParam + 1000)

                if (SizeMem[scelta].cc == 0) {
                    /* Se i valori sono impostati a 0, non li scrive */
                    tmp_descrizione = "${SizeMem[scelta].nome}\n$tmp"
                } else {
                    nico_quot = SizeMem[scelta].cc / 10
                    nico_rem = SizeMem[scelta].cc % 10
                    tmp_descrizione = "${SizeMem[scelta].nome}\n${tmp}Condensato: ${SizeMem[scelta].speed} Nicotina: $nico_quot.$nico_rem"
                }

                SetDlgItemText(hDlg, 106, tmp_descrizione)
                return true
            }

            IDCANCEL -> {
                scelta = -1
                EndDialog(hDlg, 1)
                return true
            }

            IDOK -> {
                if (scelta != -1) {
                    if (SizeMem[scelta].prezzo > Soldi) {
                        nomoney(hDlg, TABACCAIO)
                    } else {
                        Soldi -= SizeMem[scelta].prezzo
                        tmp = "tabaccaio: Paga ${MostraSoldi(SizeMem[scelta].prezzo.toLong())}"
                        writelog(tmp)
                        Fama += SizeMem[scelta].fama
                        if (Fama > 100)
                            Fama = 100
                        sizze += 20
                    }
                    i = random(8) + 600
                    tmp = LoadString(i) // 600 -> 607
                    MessageBox(
                        hDlg,
                        tmp,
                        "ART. 46 L. 29/12/1990 n. 428", MB_OK or MB_ICONINFORMATION
                    )

                    Evento(hDlg)
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
// Palestra ! (che centra tra i vestiti ??? Come procedura e' simile...)
//*******************************************************************

private fun AggiornaPalestra(parent: Dlg) {
    var tmp: String

    SetDlgItemText(parent, 104, MostraSoldi(Soldi))
    tmp = "$Fama/100"
    SetDlgItemText(parent, 105, tmp)

    if (scad_pal_giorno < 1) {
        tmp = "Nessun Abbonamento"
        SetDlgItemText(parent, 106, tmp)
    } else {
        tmp = "Scadenza abbonamento: $scad_pal_giorno ${InfoMese[scad_pal_mese - 1].nome}"
        SetDlgItemText(parent, 106, tmp)
    }

    // Scrive il grado di abbronzatura... 4 Marzo 1999
    when (current_testa) {
        1 -> tmp = "Abbronzatura Lieve"
        2 -> tmp = "Abbronzatura Media"
        3 -> tmp = "Abbronzatura Pesante"
        4 -> tmp = "Carbonizzat$ao..."
        else -> tmp = "Non abbronzat$ao"
    }
    SetDlgItemText(parent, 107, tmp)
}

//*******************************************************************

// Costi... (migliaia di lire)
private const val UN_MESE = 50
private const val SEI_MESI = 270
private const val UN_ANNO = 500

suspend fun Palestra(hDlg: Dlg, message: Int, wParam: Int, lParam: Int): Boolean {
    var tmp: String
    var i: Int

    if (message == WM_INITDIALOG) {
        AggiornaPalestra(hDlg)
        i = 0
        while (i < 4) {
            SetDlgItemText(hDlg, 120 + i, MostraSoldi(PalestraMem[i].prezzo.toLong()))
            i++
        }

        return true
    } else if (message == WM_COMMAND) {
        when (wParam) {

            110 -> { // Vai in palestra
                if (scad_pal_giorno < 1) {
                    MessageBox(
                        hDlg,
                        "Prima di poter venire in palestra devi fare un abbonamento !",
                        "Palestra", MB_OK or MB_ICONINFORMATION
                    )
                } else {
                    if (sound_active != 0)
                        TabbozPlaySound(201)
                    if (Fama < 82)
                        Fama++
                    EventiPalestra(hDlg)
                    AggiornaPalestra(hDlg)
                    /* Evento(hDlg); */
                }
                return true
            }

            111 -> { // Lampada
                if (PalestraMem[3].prezzo > Soldi) {
                    nomoney(hDlg, PALESTRA)
                } else {
                    if (current_testa < 3) {
                        current_testa++ // Grado di abbronzatura
                        if (Fama < 20)
                            Fama++ // Da 0 a 3 punti in piu' di fama
                        if (Fama < 45)
                            Fama++ // ( secondo quanta se ne ha gia')
                        if (Fama < 96)
                            Fama++
                    } else {
                        current_testa = 4 // Carbonizzato...
                        if (Fama > 8)
                            Fama -= 8
                        if (Reputazione > 5)
                            Reputazione -= 5
                        MessageBox(
                            hDlg, "L' eccessiva esposizione del tuo corpo ai raggi ultravioletti," +
                                " provoca un avanzato grado di carbonizzazione e pure qualche piccola mutazione genetica...",
                            "Lampada", MB_OK or MB_ICONSTOP
                        )
                    }
                    TabbozRedraw = 1 // E' necessario ridisegnare l' immagine del Tabbozzo...

                    if (sound_active != 0)
                        TabbozPlaySound(202)
                    Soldi -= PalestraMem[3].prezzo
                    tmp = "lampada: Paga ${MostraSoldi(PalestraMem[3].prezzo.toLong())}"
                    writelog(tmp)
                }
                i = random(5 + Fortuna)
                if (i == 0)
                    Evento(hDlg)
                AggiornaPalestra(hDlg)
                return true
            }

            115, 116, 117 -> { // Abbonamenti
                if (scad_pal_giorno > 0) {
                    MessageBox(
                        hDlg,
                        "Hai gia' un abbonamento, perche' te ne serve un altro ???",
                        "Palestra", MB_OK or MB_ICONINFORMATION
                    )
                    return true
                }

                if (PalestraMem[wParam - 115].prezzo > Soldi) {
                    nomoney(hDlg, PALESTRA)
                    return true
                } else {
                    Soldi -= PalestraMem[wParam - 115].prezzo
                    tmp = "palestra: Paga ${MostraSoldi(PalestraMem[wParam - 115].prezzo.toLong())}"
                    writelog(tmp)
                }

                when (wParam) {
                    115 -> {
                        scad_pal_mese = x_mese + 1 // UN MESE
                        scad_pal_giorno = x_giorno
                        if (scad_pal_mese > 12)
                            scad_pal_mese -= 12
                        // Quello che segue evita che la palestra scada un giorno tipo il 31 Febbraio
                        if (scad_pal_giorno > InfoMese[scad_pal_mese - 1].num_giorni)
                            scad_pal_giorno = InfoMese[scad_pal_mese - 1].num_giorni
                    }

                    116 -> {
                        scad_pal_mese = x_mese + 6 // SEI MESI
                        scad_pal_giorno = x_giorno
                        if (scad_pal_mese > 12)
                            scad_pal_mese -= 12
                        // Quello che segue evita che la palestra scada un giorno tipo il 31 Febbraio
                        if (scad_pal_giorno > InfoMese[scad_pal_mese - 1].num_giorni)
                            scad_pal_giorno = InfoMese[scad_pal_mese - 1].num_giorni
                    }

                    117 -> {
                        scad_pal_mese = x_mese // UN ANNO ( meno un giorno...)
                        scad_pal_giorno = x_giorno - 1
                        if (scad_pal_giorno < 1) {
                            scad_pal_mese--
                            if (scad_pal_mese < 1)
                                scad_pal_mese += 12
                            scad_pal_giorno = InfoMese[scad_pal_mese - 1].num_giorni
                        }
                    }
                }

                Evento(hDlg)
                AggiornaPalestra(hDlg)
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

suspend fun RunTabacchi(hDlg: Dlg) {
    if (x_vacanza != 2) { // 19 Mar 98 - Tabaccaio
        DialogBox(TABACCAIO, hDlg, ::Tabaccaio)
    } else {
        MessageBox(
            hDlg,
            "Rimani fisso a guardare la saracinesca del tabaccaio inrimediabilmente chiusa...",
            "Bar Tabacchi", MB_OK or MB_ICONINFORMATION
        )
    }
}

suspend fun RunPalestra(hDlg: Dlg) {
    if (x_vacanza != 2) { // 20 Mar 98 - Palestra
        DialogBox(PALESTRA, hDlg, ::Palestra)
    } else {
        MessageBox(
            hDlg,
            "Il tuo fisico da atleta dovra' aspettare... visto che oggi la palestra e' chiusa...",
            "Palestra", MB_OK or MB_ICONINFORMATION
        )
    }
}

suspend fun RunVestiti(hDlg: Dlg, numero: Int) {
    var numero = numero
    var tmp: String

    // Versione femminile di "Bau House" e "Blue Rider"
    if ((numero == 80) && (sesso == 'F'))
        numero = 85
    if ((numero == 84) && (sesso == 'F'))
        numero = 86

    if (x_vacanza != 2) { // 26 Feb 98... finalmente i negozi sono chiusi durante le vacanze...
        if (sound_active != 0) {
            if (numero < 82)
                TabbozPlaySound(204)
            else
                TabbozPlaySound(205)
        }

        DialogBox(numero, hDlg, ::CompraQualcosa) // La funzione e' uguale x tutti...
    } else {
        tmp = "Oh, tip$ao... i negozi sono chiusi di festa..."
        MessageBox(hDlg, tmp, "Vestiti", MB_OK or MB_ICONINFORMATION)
    }
}

/********************************************************************/
/* EVENTI PALESTRA - 14 Luglio 1998                                 */
/********************************************************************/

suspend fun EventiPalestra(hInstance: Dlg?) {
    var i: Int
    var messaggio: String

    i = random(29 + (Fortuna / 2))

    if (i > 9)
        return /* eventi: 0 - 10) */

    messaggio = LoadString(1100 + i)

    MessageBox(
        hInstance,
        messaggio,
        "Palestra...", MB_OK or MB_ICONSTOP
    )

    if (Reputazione > 10)
        Reputazione -= 4

    writelog("eventi: Evento riguardante la palestra")
}
