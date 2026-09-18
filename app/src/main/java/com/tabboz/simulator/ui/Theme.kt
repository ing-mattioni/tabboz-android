package com.tabboz.simulator.ui

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.tabboz.simulator.R
import com.tabboz.simulator.win.Engine

/** Palette e metriche dell'aspetto Windows 98 / Borland. Le misure sono in "pixel logici" (1 lp = 1 dp prima dello zoom). */
object Win98 {
    val Face = Color(0xFFC0C0C0)
    val Highlight = Color(0xFFFFFFFF)
    val Light = Color(0xFFDFDFDF)
    val Shadow = Color(0xFF808080)
    val Dark = Color(0xFF0A0A0A)
    val TitleActive1 = Color(0xFF000080)
    val TitleActive2 = Color(0xFF1084D0)
    val TitleInactive1 = Color(0xFF808080)
    val TitleInactive2 = Color(0xFFB5B5B5)
    val Desktop = Color(0xFF008080)
    val Text = Color(0xFF000000)
    val TextDisabled = Color(0xFF808080)
    val Window = Color(0xFFFFFFFF)
    val MenuSelected = Color(0xFF000080)

    /** Pixel logici per dialog unit. */
    const val DLU = 2
    const val TITLE_H = 18f
    const val MENU_H = 18f
    const val FRAME = 3f
    const val FONT_PX = 11f
    const val LINE_H = 13f
}

val TabbozFont = FontFamily(
    Font(R.font.ms_sans_serif, FontWeight.Normal),
    Font(R.font.ms_sans_serif_bold, FontWeight.Bold),
)

/** Converte pixel logici (dp) in sp indipendenti dalla scala dei caratteri di sistema. */
@Composable
fun lpSp(v: Float): TextUnit = with(LocalDensity.current) { v.dp.toSp() }

@Composable
fun win98TextStyle(
    bold: Boolean = false,
    color: Color = Win98.Text,
    align: TextAlign = TextAlign.Start,
    sizeLp: Float = Win98.FONT_PX,
): TextStyle = TextStyle(
    fontFamily = TabbozFont,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    fontSize = lpSp(sizeLp),
    lineHeight = lpSp(Win98.LINE_H * sizeLp / Win98.FONT_PX),
    color = color,
    textAlign = align,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

/** Disegna una cornice 3D di 1 lp: colore `tl` in alto/sinistra, `br` in basso/destra, rientrata di `inset` lp. */
fun DrawScope.bevelRect(tl: Color, br: Color, inset: Float = 0f) {
    val s = 1.dp.toPx()
    val i = inset * s
    val w = size.width
    val h = size.height
    drawRect(tl, Offset(i, i), Size(w - 2 * i, s))
    drawRect(tl, Offset(i, i), Size(s, h - 2 * i))
    drawRect(br, Offset(i, h - i - s), Size(w - 2 * i, s))
    drawRect(br, Offset(w - i - s, i), Size(s, h - 2 * i))
}

fun Modifier.bevel(tl: Color, br: Color, inset: Float = 0f): Modifier = drawBehind { bevelRect(tl, br, inset) }

/** Bordo rialzato a 2 livelli (pulsanti). */
fun Modifier.raised2(): Modifier = drawBehind {
    bevelRect(Win98.Highlight, Win98.Dark, 0f)
    bevelRect(Win98.Light, Win98.Shadow, 1f)
}

/** Bordo premuto a 2 livelli (pulsanti premuti). */
fun Modifier.pressed2(): Modifier = drawBehind {
    bevelRect(Win98.Dark, Win98.Highlight, 0f)
    bevelRect(Win98.Shadow, Win98.Light, 1f)
}

/** Bordo incassato a 2 livelli (campi di testo). */
fun Modifier.sunken2(): Modifier = drawBehind {
    bevelRect(Win98.Shadow, Win98.Highlight, 0f)
    bevelRect(Win98.Dark, Win98.Light, 1f)
}

/** Bordo incassato ad 1 livello. */
fun Modifier.sunken1(): Modifier = bevel(Win98.Shadow, Win98.Highlight)

/** Bordo rialzato ad 1 livello. */
fun Modifier.raised1(): Modifier = bevel(Win98.Highlight, Win98.Shadow)

/** Cornice della finestra (98.css). */
fun Modifier.windowFrame(): Modifier = drawBehind {
    bevelRect(Win98.Light, Win98.Dark, 0f)
    bevelRect(Win98.Highlight, Win98.Shadow, 1f)
}

/** Cache delle immagini caricate dagli asset. */
object BitmapCache {
    private val cache = HashMap<String, ImageBitmap?>()

    fun bitmap(id: Int): ImageBitmap? = load("bitmaps/$id.png")
    fun icon(name: String): ImageBitmap? = load("icons/$name.png")

    fun load(path: String): ImageBitmap? = cache.getOrPut(path) {
        try {
            Engine.appContext.assets.open(path).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        } catch (e: Exception) {
            null
        }
    }
}

fun Dp.lp(): Float = value
