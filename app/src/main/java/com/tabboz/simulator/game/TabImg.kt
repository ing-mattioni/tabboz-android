@file:Suppress("FunctionName", "LocalVariableName", "unused", "UNUSED_PARAMETER", "NAME_SHADOWING")

package com.tabboz.simulator.game

import com.tabboz.simulator.win.*

//      Tabboz Simulator
//      Copyright (c) 1999 Andrea Bonomi
//
/* Iniziato il 26 Febbraio 1999         */
/* In questo file ci sono le funzioni x */
/* la visualizzazione delle immagini.   */
/* 1 Giugno 1999 -                      */
/* Conversione Tabbozzo -> Tabbozza     */
//
// Traduzione di tabimg.c: su Android il disegno e' delegato alla UI, quindi WMCreate/WMPaint
// diventano la lista dei livelli bitmap registrata in CustomControls (stesse risorse, stesso
// ordine e stesse coordinate dell' originale). WM_CREATE/WM_DESTROY/WM_PAINT non servono.

private const val IMG_SIZEX = 143
private const val IMG_SIZEY = 275
private const val IMG_X_INC = 10
private const val IMG_Y_INC = 0

/*********************************************************************/
/* Parte per (cercare) di disegnare immagini nelle dialogs			   */
/*********************************************************************/
// Registra la Classe BMPView
// (WMCreate: sceglie le bitmap secondo ImgSelector / sesso;
//  WMPaint: le disegna, mantenendo l' ordine !!!)

fun RegisterBMPViewClass() {
    CustomControls.register("BMPView") { _ ->
        // (nell' originale i valori sono le HBITMAP caricate con LoadBitmap: qui gli id delle risorse)
        var hbmp_testa = 0
        var hbmp_giubbotto = 0
        var hbmp_pantaloni = 0
        var hbmp_scarpe = 0
        var hbmp_sfondo = 0

        when (ImgSelector) {
            0 -> {
                if (sesso == 'M') {
                    hbmp_pantaloni = current_pantaloni + 1520
                    hbmp_testa = current_testa + 1390
                    hbmp_sfondo = 1560
                    hbmp_giubbotto = current_gibbotto + 1500
                    hbmp_scarpe = current_scarpe + 1540
                } else {
                    hbmp_pantaloni = current_pantaloni + 1525
                    hbmp_testa = current_testa + 1370
                    hbmp_sfondo = 1561
                    hbmp_giubbotto = current_gibbotto + 1507
                    hbmp_scarpe = current_scarpe + 1540
                }
            }
        }

        // Mantenere l' ordine !!!
        val layers = ArrayList<BitmapLayer>()

        layers.add(BitmapLayer(hbmp_sfondo, 0, 0))

        if (sesso == 'M')
            layers.add(BitmapLayer(hbmp_pantaloni, 18 + IMG_X_INC, 93 + IMG_Y_INC))
        else
            layers.add(BitmapLayer(hbmp_pantaloni, 23 + IMG_X_INC, 120 + IMG_Y_INC))

        layers.add(BitmapLayer(hbmp_scarpe, -16 + IMG_X_INC, 205 + IMG_Y_INC))

        if (sesso == 'M') {
            layers.add(BitmapLayer(hbmp_giubbotto, -30 + IMG_X_INC, 18 + IMG_Y_INC))
            layers.add(BitmapLayer(hbmp_testa, 32 + IMG_X_INC, 3 + IMG_Y_INC))
        } else {
            layers.add(BitmapLayer(hbmp_giubbotto, -27 + IMG_X_INC, 39 + IMG_Y_INC))
            layers.add(BitmapLayer(hbmp_testa, 30 + IMG_X_INC, 1 + IMG_Y_INC))
        }

        layers
    }
}

/*********************************************************************/
// Classe BMPView

suspend fun BMPViewWndProc(hWnd: Dlg, msg: Int, wParam: Int, lParam: Int): Int {
    when (msg) {
        WM_LBUTTONDOWN -> {
            /* Display Personal Information box. */
            DialogBox(PERSONALINFO, hWnd, ::PersonalInfo)
            AggiornaPrincipale(hWndMain!!)
            return 0
        }
    }
    return 0
}

/*********************************************************************/
/*********************************************************************/

/*********************************************************************/
// Questa parte si okkupa del disegno della tipa... 6 Maggio 1999

/*********************************************************************/
// Registra la Classe BMPTipa
// (WMTipaCreate/WMTipaPaint: current_tipa + 1204 disegnata in 0,0)

fun RegisterBMPTipaClass() {
    CustomControls.register("BMPTipa") { listOf(BitmapLayer(current_tipa + 1204, 0, 0)) }
}

/*********************************************************************/
// Classe BMPTipa

suspend fun BMPTipaWndProc(hWnd: Dlg, msg: Int, wParam: Int, lParam: Int): Int {
    when (msg) {
        WM_LBUTTONDOWN -> {
            //								sprintf(tmp,"%d %d",LOWORD(lParam),HIWORD(lParam));
            //								MessageBox( 0, tmp, "BMPTipa", MB_OK );
            if ((((LOWORD(lParam) >= 138) && (LOWORD(lParam) <= 170)) && // Coordinate Tette
                    ((HIWORD(lParam) >= 50) && (HIWORD(lParam) <= 65))) ||
                (((LOWORD(lParam) >= 104) && (LOWORD(lParam) <= 136)) && // Coordinate Culo
                    ((HIWORD(lParam) >= 78) && (HIWORD(lParam) <= 166)))
            ) {
                // Palpatina...
                if (Rapporti < 0) {
                    //			MessageBox( hWnd, "Brutto porco, che cazzo tocchi ?", "Palpatina...", MB_OK | MB_ICONSTOP);
                    //			if (sound_active) TabbozPlaySound(604);
                    //			AggiornaTipa(tipahDlg);
                } else if (Rapporti < (20 + (FigTipa / 2))) { // + e' figa, - te la da' (perla di saggezza)
                    if (sound_active != 0)
                        TabbozPlaySound(604)
                    MessageBox(hWnd, "Brutto porco, che cazzo tocchi ?", "Palpatina...", MB_OK)
                    if (Rapporti > 5)
                        Rapporti -= 3
                    tipahDlg?.let { AggiornaTipa(it) }
                } else if (Rapporti < (30 + (FigTipa / 2))) {
                    MessageBox(hWnd, "Dai, smettila... Voi uomini pensato solo a quello...", "Palpatina...", MB_OK or MB_ICONQUESTION)
                } else {
                    MessageBox(hWnd, "Mmhhhhhhhh.........", "Palpatina...", MB_OK or MB_ICONINFORMATION)
                    Giorno(hWnd)
                    tipahDlg?.let { AggiornaTipa(it) }
                }
            }
            return 0
        }
    }
    return 0
}
