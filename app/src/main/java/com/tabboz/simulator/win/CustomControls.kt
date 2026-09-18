package com.tabboz.simulator.win

/** Un livello bitmap da disegnare in un controllo custom: bitmap `bitmapId` alle coordinate (x, y) in pixel logici. */
data class BitmapLayer(val bitmapId: Int, val x: Int, val y: Int)

/**
 * Registro dei "window class" custom (BMPView, BMPTipa): per ogni classe una funzione che, dato il dialogo,
 * restituisce i livelli bitmap da disegnare. La UI la richiama ad ogni ridisegno (vedi [Dlg.repaint]).
 */
object CustomControls {
    val painters = HashMap<String, (Dlg) -> List<BitmapLayer>>()

    fun register(className: String, painter: (Dlg) -> List<BitmapLayer>) {
        painters[className] = painter
    }
}
