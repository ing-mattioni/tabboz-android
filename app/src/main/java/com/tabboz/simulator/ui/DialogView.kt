package com.tabboz.simulator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.tabboz.simulator.win.Dlg
import com.tabboz.simulator.win.Engine
import com.tabboz.simulator.win.MenuItem
import com.tabboz.simulator.win.SC_CLOSE
import com.tabboz.simulator.win.WM_COMMAND
import com.tabboz.simulator.win.WM_SYSCOMMAND
import kotlin.math.roundToInt

/** Dimensione logica (lp) della finestra di un dialogo. */
fun dialogWindowSize(dlg: Dlg): Pair<Float, Float> {
    val t = dlg.template ?: return 200f to 100f
    val hasTitle = t.has("WS_CAPTION")
    val hasMenu = t.menu != null
    val w = t.w * Win98.DLU + 2 * Win98.FRAME
    val h = t.h * Win98.DLU + 2 * Win98.FRAME + (if (hasTitle) Win98.TITLE_H else 0f) + (if (hasMenu) Win98.MENU_H else 0f)
    return w to h
}

/** Barra del titolo Windows 98. */
@Composable
fun TitleBar(title: String, iconName: String?, active: Boolean, showClose: Boolean, onClose: () -> Unit) {
    val brush = if (active) Brush.horizontalGradient(listOf(Win98.TitleActive1, Win98.TitleActive2))
    else Brush.horizontalGradient(listOf(Win98.TitleInactive1, Win98.TitleInactive2))
    Row(
        Modifier.fillMaxWidth().requiredHeight(Win98.TITLE_H.dp).background(brush).padding(start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconName != null) {
            val img = BitmapCache.icon(iconName)
            if (img != null) {
                Canvas(Modifier.requiredSize(16.dp).padding(end = 0.dp)) {
                    drawImage(img, dstOffset = IntOffset.Zero, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.Low)
                }
                Spacer(Modifier.width(3.dp))
            }
        }
        BasicText(
            text = title,
            modifier = Modifier.weight(1f).padding(start = 2.dp),
            style = win98TextStyle(bold = true, color = if (active) Win98.Highlight else Win98.Light),
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        if (showClose) {
            Box(
                Modifier.requiredSize(16.dp, 14.dp).background(Win98.Face).raised2()
                    .clickable(remember { MutableInteractionSource() }, null, onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.requiredSize(8.dp, 7.dp)) {
                    val s = 1.5f * 1.dp.toPx()
                    drawLine(Win98.Text, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = s)
                    drawLine(Win98.Text, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = s)
                }
            }
        }
    }
}

/** Una finestra di dialogo completa (cornice, titolo, menu, controlli). */
@Composable
fun DialogWindowView(dlg: Dlg, isTop: Boolean) {
    val t = dlg.template
    val (w, h) = dialogWindowSize(dlg)
    var openMenu by remember { mutableIntStateOf(-1) }
    val menuX = remember { mutableStateOf(FloatArray(0)) }
    val density = LocalDensity.current

    Box(
        Modifier.requiredSize(w.dp, h.dp).background(Win98.Face).windowFrame().padding(Win98.FRAME.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            if (t != null && t.has("WS_CAPTION")) {
                TitleBar(
                    title = t.caption,
                    iconName = if (t.menu != null) "1" else null,
                    active = isTop,
                    showClose = t.has("WS_SYSMENU"),
                    onClose = { if (isTop) dlg.postMessage(WM_SYSCOMMAND, SC_CLOSE, 0) },
                )
            }
            if (t?.menu != null) {
                MenuBar(dlg, Engine.mainMenu, openMenu, onOpen = { openMenu = it }, onPositions = { menuX.value = it })
            }
            Box(Modifier.fillMaxSize()) {
                if (t != null) {
                    // Il primo controllo del file .rc e' in cima allo z-order: disegniamo in ordine inverso.
                    for (c in t.controls.asReversed()) {
                        ControlView(dlg, c, isTop)
                    }
                }
            }
        }
        // Menu a tendina aperto (sopra tutto)
        if (openMenu >= 0 && t?.menu != null) {
            val menu = Engine.mainMenu
            Box(
                Modifier.fillMaxSize().zIndex(10f)
                    .clickable(remember { MutableInteractionSource() }, null) { openMenu = -1 }
            ) {
                val xs = menuX.value
                val x = if (openMenu < xs.size) with(density) { xs[openMenu].toDp() } else 0.dp
                if (openMenu < menu.size) {
                    DropDownMenu(
                        dlg = dlg,
                        items = menu[openMenu].items,
                        modifier = Modifier.absoluteOffset(x, (Win98.TITLE_H + Win98.MENU_H - 1).dp),
                        onSelect = { id ->
                            openMenu = -1
                            dlg.postMessage(WM_COMMAND, id, 0)
                        },
                    )
                }
            }
        }
    }
}

private fun menuLabel(dlg: Dlg, item: MenuItem): String {
    val raw = dlg.menuTexts[item.menuId] ?: item.label
    return raw.replace("&&", "\u0001").replace("&", "").replace("\u0001", "&")
}

/** Barra dei menu della finestra principale. */
@Composable
fun MenuBar(dlg: Dlg, menu: List<MenuItem>, openMenu: Int, onOpen: (Int) -> Unit, onPositions: (FloatArray) -> Unit) {
    val positions = remember(menu.size) { FloatArray(menu.size) }
    Row(
        Modifier.fillMaxWidth().requiredHeight(Win98.MENU_H.dp).background(Win98.Face).zIndex(5f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        menu.forEachIndexed { i, item ->
            val isHelp = item.label.replace("&", "").equals("Help", ignoreCase = true)
            if (isHelp) Spacer(Modifier.weight(1f))
            val open = openMenu == i
            Box(
                Modifier.fillMaxHeight()
                    .onGloballyPositioned { positions[i] = it.positionInParent().x; onPositions(positions.copyOf()) }
                    .then(if (open) Modifier.sunken1() else Modifier)
                    .clickable(remember { MutableInteractionSource() }, null) { onOpen(if (open) -1 else i) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(menuLabel(dlg, item), style = win98TextStyle(), maxLines = 1)
            }
        }
    }
}

@Composable
fun DropDownMenu(dlg: Dlg, items: List<MenuItem>, modifier: Modifier, onSelect: (Int) -> Unit) {
    Column(
        modifier.width(IntrinsicSize.Max).background(Win98.Face).raised2().padding(3.dp)
    ) {
        for (item in items) {
            if (item.kind == "separator") {
                Box(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 3.dp).requiredHeight(2.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val s = 1.dp.toPx()
                        drawRect(Win98.Shadow, Offset.Zero, androidx.compose.ui.geometry.Size(size.width, s))
                        drawRect(Win98.Highlight, Offset(0f, s), androidx.compose.ui.geometry.Size(size.width, s))
                    }
                }
            } else {
                val disabled = dlg.menuDisabled[item.menuId] == true
                var hover by remember { mutableStateOf(false) }
                Box(
                    Modifier.fillMaxWidth().requiredHeight(17.dp)
                        .background(if (hover) Win98.MenuSelected else Color.Transparent)
                        .then(if (!disabled) Modifier.clickable(remember { MutableInteractionSource() }, null) {
                            hover = true
                            onSelect(item.menuId)
                        } else Modifier)
                        .padding(start = 20.dp, end = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicText(
                        text = menuLabel(dlg, item),
                        style = win98TextStyle(color = if (disabled) Win98.TextDisabled else if (hover) Win98.Highlight else Win98.Text),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Cornice etched usata per riquadri: non usata direttamente ma utile per debug. */
@Suppress("unused")
@Composable
fun DebugOutline(modifier: Modifier) {
    Canvas(modifier) { drawRect(Color.Red, style = Stroke(1f)) }
}
