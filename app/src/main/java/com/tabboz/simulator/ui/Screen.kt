package com.tabboz.simulator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.tabboz.simulator.win.Engine
import com.tabboz.simulator.win.IDCANCEL
import com.tabboz.simulator.win.IDNO
import com.tabboz.simulator.win.IDOK
import com.tabboz.simulator.win.IDYES
import com.tabboz.simulator.win.MB_OKCANCEL
import com.tabboz.simulator.win.MB_YESNO
import com.tabboz.simulator.win.Win98Window
import kotlin.math.min
import kotlin.math.roundToInt

const val MAX_SCALE = 1.8f
const val MSGBOX_W = 320f

/**
 * Ospita una finestra di dimensione logica (w x h lp) scalandola uniformemente per farla entrare nello schermo.
 * Se `h` e' null l'altezza e' quella naturale del contenuto (message box).
 */
@Composable
fun ScaledHost(w: Float, h: Float?, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val availW = maxWidth.value - 8f
        val availH = maxHeight.value - 8f
        var scale = availW / w
        if (h != null) scale = min(scale, availH / h)
        scale = min(scale, MAX_SCALE)
        val m = if (h != null) Modifier.requiredSize(w.dp, h.dp) else Modifier.requiredWidth(w.dp).wrapContentHeight()
        Box(m.graphicsLayer { scaleX = scale; scaleY = scale }) {
            content()
        }
    }
}

/** Lo "schermo": sfondo teal e stack delle finestre. */
@Composable
fun TabbozScreen() {
    val windows = Engine.windows
    Box(Modifier.fillMaxSize().background(Win98.Desktop).safeDrawingPadding()) {
        val last = windows.lastIndex
        windows.forEachIndexed { i, win ->
            val isTop = i == last
            when (win) {
                is Win98Window.DialogWindow -> {
                    if (win.dlg.visible || isTop) {
                        key(win.dlg) {
                            val (w, h) = dialogWindowSize(win.dlg)
                            ScaledHost(w, h) { DialogWindowView(win.dlg, isTop) }
                        }
                    }
                }
                is Win98Window.MessageBoxWindow -> {
                    key(win) {
                        ScaledHost(MSGBOX_W, null) { MessageBoxView(win, isTop) }
                    }
                }
                Win98Window.ShutdownScreen -> ShutdownView()
            }
        }
    }
}

/** Message box in stile Windows 98. */
@Composable
fun MessageBoxView(mb: Win98Window.MessageBoxWindow, isTop: Boolean) {
    val iconName = when (mb.type and 0x70) {
        0x10 -> "mb_103" // MB_ICONSTOP / MB_ICONHAND
        0x20 -> "mb_102" // MB_ICONQUESTION
        0x30 -> "mb_101" // MB_ICONEXCLAMATION
        0x40 -> "mb_104" // MB_ICONINFORMATION
        else -> null
    }
    Column(
        Modifier.requiredWidth(MSGBOX_W.dp).background(Win98.Face).windowFrame().padding(Win98.FRAME.dp)
    ) {
        TitleBar(
            title = mb.caption,
            iconName = null,
            active = isTop,
            showClose = true,
            onClose = { if (isTop) mb.complete(mb.cancelResult) },
        )
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 4.dp)) {
            if (iconName != null) {
                val img = BitmapCache.icon(iconName)
                if (img != null) {
                    Canvas(Modifier.requiredSize(32.dp)) {
                        drawImage(img, dstOffset = IntOffset.Zero, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.Low)
                    }
                    Spacer(Modifier.width(14.dp))
                }
            }
            BasicText(
                text = mb.text,
                modifier = Modifier.weight(1f).padding(top = 4.dp),
                style = win98TextStyle(),
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            val buttons: List<Pair<String, Int>> = when {
                (mb.type and MB_YESNO) != 0 -> listOf("Si'" to IDYES, "No" to IDNO)
                (mb.type and MB_OKCANCEL) != 0 -> listOf("OK" to IDOK, "Annulla" to IDCANCEL)
                else -> listOf("OK" to IDOK)
            }
            buttons.forEachIndexed { i, (label, id) ->
                if (i > 0) Spacer(Modifier.width(6.dp))
                Win98Button(
                    modifier = Modifier.requiredSize(75.dp, 23.dp),
                    text = label,
                    enabled = isTop,
                    default = i == 0,
                    onClick = { mb.complete(id) },
                )
            }
        }
    }
}

/** "It's now safe to turn off your computer." */
@Composable
fun ShutdownView() {
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        BasicText(
            text = "It's now safe to turn off\nyour computer.",
            style = win98TextStyle(bold = true, color = Color(0xFFFFA500), align = TextAlign.Center, sizeLp = 22f),
        )
    }
}
