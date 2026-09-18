package com.tabboz.simulator.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.tabboz.simulator.win.BitmapLayer
import com.tabboz.simulator.win.CheckDlgButton
import com.tabboz.simulator.win.ControlTemplate
import com.tabboz.simulator.win.CustomControls
import com.tabboz.simulator.win.Dlg
import com.tabboz.simulator.win.MAKELPARAM
import com.tabboz.simulator.win.WM_COMMAND
import kotlin.math.roundToInt

// -----------------------------------------------------------------------------
// Rendering dei singoli controlli dei dialoghi (coordinate in pixel logici).
// -----------------------------------------------------------------------------

/** Disegna un'immagine a dimensione naturale (1 pixel immagine = 1 pixel logico). */
fun DrawScope.drawImageLp(img: ImageBitmap, xLp: Int = 0, yLp: Int = 0, wLp: Int = img.width, hLp: Int = img.height) {
    val d = 1.dp.toPx()
    drawImage(
        image = img,
        dstOffset = IntOffset((xLp * d).roundToInt(), (yLp * d).roundToInt()),
        dstSize = IntSize((wLp * d).roundToInt(), (hLp * d).roundToInt()),
        filterQuality = FilterQuality.None,
    )
}

/** Abilitazione di un controllo (i controlli con id -1 non sono indirizzabili: usano solo lo stile). */
fun ctlEnabled(dlg: Dlg, c: ControlTemplate): Boolean =
    if (c.id == -1) !c.has("WS_DISABLED") else dlg.isEnabled(c.id)

/** Testo corrente di un controllo (per id -1 quello del template). */
fun ctlText(dlg: Dlg, c: ControlTemplate): String = if (c.id == -1) c.text else (dlg.texts[c.id] ?: c.text)

/** Piazza e disegna un controllo del template. */
@Composable
fun ControlView(dlg: Dlg, c: ControlTemplate, interactive: Boolean) {
    val x = c.x * Win98.DLU
    val y = c.y * Win98.DLU
    val w = c.w * Win98.DLU
    val h = c.h * Win98.DLU
    val m = Modifier.absoluteOffset(x.dp, y.dp).requiredSize(w.dp, h.dp)
    val enabled = ctlEnabled(dlg, c) && interactive
    when (c.cls) {
        "BorShade" -> BorShadeView(c, m)
        "STATIC", "BorStatic" -> {
            if (c.has("SS_ICON")) IconView(c.text, m)
            else StaticTextView(dlg, c, m)
        }
        "BUTTON" -> when {
            c.has("BS_PUSHBUTTON") || c.has("BS_DEFPUSHBUTTON") -> PushButtonView(dlg, c, m, enabled)
            c.has("BS_AUTORADIOBUTTON") || c.has("BS_RADIOBUTTON") -> RadioView(dlg, c, m, enabled, bwcc = false)
            c.has("BS_AUTOCHECKBOX") || c.has("BS_CHECKBOX") -> CheckBoxView(dlg, c, m, enabled, bwcc = false)
            c.has("BS_GROUPBOX") -> GroupBoxView(c, m)
            else -> PushButtonView(dlg, c, m, enabled)
        }
        "BorRadio" -> RadioView(dlg, c, m, enabled, bwcc = true)
        "BorCheck" -> CheckBoxView(dlg, c, m, enabled, bwcc = true)
        "BorBtn" -> BorBtnView(dlg, c, m, enabled)
        "EDIT" -> EditView(dlg, c, m, enabled)
        "COMBOBOX" -> ComboView(dlg, c, m)
        "msctls_progress" -> Box(m.sunken1())
        else -> CustomView(dlg, c, m, interactive)
    }
}

// --- BorShade -----------------------------------------------------------------

@Composable
fun BorShadeView(c: ControlTemplate, m: Modifier) {
    val style = c.styleNum and 0xFF
    Box(
        m.drawBehind {
            val s = 1.dp.toPx()
            when (style) {
                2 -> { // BSS_HDIP: linea orizzontale incassata
                    drawRect(Win98.Shadow, Offset(0f, 0f), Size(size.width, s))
                    drawRect(Win98.Highlight, Offset(0f, s), Size(size.width, s))
                }
                3 -> { // BSS_VDIP: linea verticale incassata
                    drawRect(Win98.Shadow, Offset(0f, 0f), Size(s, size.height))
                    drawRect(Win98.Highlight, Offset(s, 0f), Size(s, size.height))
                }
                4 -> { // BSS_HBUMP: linea orizzontale rialzata
                    drawRect(Win98.Highlight, Offset(0f, 0f), Size(size.width, s))
                    drawRect(Win98.Shadow, Offset(0f, s), Size(size.width, s))
                }
                5 -> { // BSS_VBUMP: linea verticale rialzata
                    drawRect(Win98.Highlight, Offset(0f, 0f), Size(s, size.height))
                    drawRect(Win98.Shadow, Offset(s, 0f), Size(s, size.height))
                }
                else -> { // BSS_GROUP / BSS_RGROUP: pannello rialzato
                    bevelRect(Win98.Highlight, Win98.Shadow, 0f)
                }
            }
        }
    )
}

// --- STATIC / BorStatic --------------------------------------------------------

@Composable
fun StaticTextView(dlg: Dlg, c: ControlTemplate, m: Modifier) {
    val text = ctlText(dlg, c)
    val align = when {
        c.has("SS_CENTER") || (c.cls == "BorStatic" && (c.styleNum and 1) != 0) -> TextAlign.Center
        c.has("SS_RIGHT") -> TextAlign.End
        else -> TextAlign.Start
    }
    val bordered = c.has("WS_BORDER")
    val enabled = ctlEnabled(dlg, c)
    val style = win98TextStyle(color = if (enabled) Win98.Text else Win98.TextDisabled, align = align)
    if (bordered) {
        Box(m.sunken1(), contentAlignment = Alignment.CenterStart) {
            BasicText(
                text = text,
                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp).offset(y = 1.dp),
                style = style,
                overflow = TextOverflow.Visible,
            )
        }
    } else {
        BasicText(
            text = text,
            modifier = m.padding(top = 1.dp),
            style = style,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
fun IconView(name: String, m: Modifier) {
    val img = BitmapCache.icon(name)
    Canvas(m) {
        if (img != null) {
            val d = 1.dp.toPx()
            drawImage(
                image = img,
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                filterQuality = if (size.width >= img.width * d) FilterQuality.None else FilterQuality.Low,
            )
        }
    }
}

// --- Pulsanti -------------------------------------------------------------------

/** Pulsante standard Windows 98. */
@Composable
fun Win98Button(
    modifier: Modifier,
    text: String,
    enabled: Boolean,
    default: Boolean = false,
    onClick: () -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val base = modifier
        .then(if (enabled) Modifier.clickable(src, null, onClick = onClick) else Modifier)
        .background(Win98.Face)
        .drawBehind {
            val s = 1.dp.toPx()
            val inset = if (default) 1f else 0f
            if (default) drawRect(Win98.Dark, Offset.Zero, size, style = Stroke(s))
            if (pressed && enabled) {
                bevelRect(Win98.Dark, Win98.Highlight, inset)
                bevelRect(Win98.Shadow, Win98.Light, inset + 1f)
            } else {
                bevelRect(Win98.Highlight, Win98.Dark, inset)
                bevelRect(Win98.Light, Win98.Shadow, inset + 1f)
            }
        }
    Box(base, contentAlignment = Alignment.Center) {
        val off = if (pressed && enabled) 1.dp else 0.dp
        if (!enabled) {
            BasicText(
                text = text,
                modifier = Modifier.offset(1.dp, 1.dp),
                style = win98TextStyle(color = Win98.Highlight, align = TextAlign.Center),
                overflow = TextOverflow.Visible,
            )
        }
        BasicText(
            text = text,
            modifier = Modifier.offset(off, off),
            style = win98TextStyle(color = if (enabled) Win98.Text else Win98.TextDisabled, align = TextAlign.Center),
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
fun PushButtonView(dlg: Dlg, c: ControlTemplate, m: Modifier, enabled: Boolean) {
    Win98Button(
        modifier = m,
        text = ctlText(dlg, c),
        enabled = enabled,
        default = c.has("BS_DEFPUSHBUTTON"),
        onClick = { dlg.postMessage(WM_COMMAND, c.id, 0) },
    )
}

/** Pulsante bitmap Borland (BorBtn): OK/Cancel/Yes/No con bitmap di sistema, altrimenti bitmap 1000+id. */
@Composable
fun BorBtnView(dlg: Dlg, c: ControlTemplate, m: Modifier, enabled: Boolean) {
    val img = BitmapCache.bitmap(1000 + c.id)
    val imgPressed = BitmapCache.bitmap(3000 + c.id)
    val isStatic = (c.styleNum and 0x8000) != 0 // BBS_BITMAP
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val mod = m
        .then(if (enabled) Modifier.clickable(src, null) { dlg.postMessage(WM_COMMAND, c.id, 0) } else Modifier)
        .then(if (c.has("WS_BORDER")) Modifier.sunken1() else Modifier)
    Canvas(mod) {
        val im = if (pressed && !isStatic && imgPressed != null) imgPressed else img
        if (im != null) {
            val off = if (pressed && !isStatic && imgPressed == null) 1 else 0
            val inset = if (c.has("WS_BORDER")) 1 else 0
            drawImageLp(im, off + inset, off + inset)
        }
    }
}

// --- Radio / Check ----------------------------------------------------------------

@Composable
fun RadioView(dlg: Dlg, c: ControlTemplate, m: Modifier, enabled: Boolean, bwcc: Boolean) {
    val checked = dlg.isChecked(c.id)
    val text = ctlText(dlg, c)
    val ctlEnabled = ctlEnabled(dlg, c)
    val on = if (bwcc) BitmapCache.bitmap(131) else null
    val off = if (bwcc) BitmapCache.bitmap(130) else null
    Box(
        m.then(if (enabled) Modifier.clickable(remember { MutableInteractionSource() }, null) {
            CheckDlgButton(dlg, c.id, true)
            dlg.postMessage(WM_COMMAND, c.id, 0)
        } else Modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = 1.dp.toPx()
            val boxLp = 13
            val top = ((size.height / d - boxLp) / 2f).roundToInt()
            val im = if (checked) on else off
            if (im != null) {
                drawImageLp(im, 0, top)
            } else {
                // Radio standard: cerchio con effetto 3D
                val cx = 6.5f * d
                val cy = (top + 6.5f) * d
                val r = 6f * d
                drawCircle(Win98.Window, r, Offset(cx, cy))
                drawArc(Win98.Shadow, 135f, 180f, false, Offset(cx - r, cy - r), Size(2 * r, 2 * r), style = Stroke(d))
                drawArc(Win98.Highlight, -45f, 180f, false, Offset(cx - r, cy - r), Size(2 * r, 2 * r), style = Stroke(d))
                drawArc(Win98.Dark, 135f, 180f, false, Offset(cx - r + d, cy - r + d), Size(2 * (r - d), 2 * (r - d)), style = Stroke(d))
                if (checked) drawCircle(Win98.Text, 2.2f * d, Offset(cx, cy))
            }
        }
        BasicText(
            text = text,
            modifier = Modifier.padding(start = 17.dp).offset(y = 1.dp),
            style = win98TextStyle(color = if (ctlEnabled) Win98.Text else Win98.TextDisabled),
            overflow = TextOverflow.Visible,
            maxLines = 1,
        )
    }
}

@Composable
fun CheckBoxView(dlg: Dlg, c: ControlTemplate, m: Modifier, enabled: Boolean, bwcc: Boolean) {
    val checked = dlg.isChecked(c.id)
    val text = ctlText(dlg, c)
    val on = if (bwcc) BitmapCache.bitmap(111) else null
    val off = if (bwcc) BitmapCache.bitmap(110) else null
    val ctlEnabled = ctlEnabled(dlg, c)
    Box(
        m.then(if (enabled) Modifier.clickable(remember { MutableInteractionSource() }, null) {
            CheckDlgButton(dlg, c.id, !dlg.isChecked(c.id))
            dlg.postMessage(WM_COMMAND, c.id, 0)
        } else Modifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = 1.dp.toPx()
            val boxLp = 13
            val top = ((size.height / d - boxLp) / 2f).roundToInt()
            val im = if (checked) on else off
            if (im != null) {
                drawImageLp(im, 0, top)
            } else {
                // Checkbox standard: quadrato bianco incassato con segno di spunta
                val bw = boxLp * d
                val ty = top * d
                drawRect(if (ctlEnabled) Win98.Window else Win98.Face, Offset(0f, ty), Size(bw, bw))
                drawRect(Win98.Shadow, Offset(0f, ty), Size(bw, d))
                drawRect(Win98.Shadow, Offset(0f, ty), Size(d, bw))
                drawRect(Win98.Highlight, Offset(0f, ty + bw - d), Size(bw, d))
                drawRect(Win98.Highlight, Offset(bw - d, ty), Size(d, bw))
                drawRect(Win98.Dark, Offset(d, ty + d), Size(bw - 2 * d, d))
                drawRect(Win98.Dark, Offset(d, ty + d), Size(d, bw - 2 * d))
                drawRect(Win98.Light, Offset(d, ty + bw - 2 * d), Size(bw - 2 * d, d))
                drawRect(Win98.Light, Offset(bw - 2 * d, ty + d), Size(d, bw - 2 * d))
                if (checked) {
                    val p = Path()
                    p.moveTo(3 * d, ty + 6 * d)
                    p.lineTo(5.5f * d, ty + 8.5f * d)
                    p.lineTo(10 * d, ty + 4 * d)
                    drawPath(p, if (ctlEnabled) Win98.Text else Win98.TextDisabled, style = Stroke(2 * d))
                }
            }
        }
        BasicText(
            text = text,
            modifier = Modifier.padding(start = 17.dp).offset(y = 1.dp),
            style = win98TextStyle(color = if (ctlEnabled) Win98.Text else Win98.TextDisabled),
            overflow = TextOverflow.Visible,
            maxLines = 1,
        )
    }
}

// --- Group box --------------------------------------------------------------------

@Composable
fun GroupBoxView(c: ControlTemplate, m: Modifier) {
    Box(m) {
        Canvas(Modifier.fillMaxSize()) {
            val d = 1.dp.toPx()
            val top = 6 * d
            // linea incassata (grigio + bianco)
            drawRect(Win98.Shadow, Offset(0f, top), Size(size.width - d, d))
            drawRect(Win98.Shadow, Offset(0f, top), Size(d, size.height - top - d))
            drawRect(Win98.Shadow, Offset(0f, size.height - 2 * d), Size(size.width - d, d))
            drawRect(Win98.Shadow, Offset(size.width - 2 * d, top), Size(d, size.height - top - d))
            drawRect(Win98.Highlight, Offset(d, top + d), Size(size.width - 3 * d, d))
            drawRect(Win98.Highlight, Offset(d, top + d), Size(d, size.height - top - 3 * d))
            drawRect(Win98.Highlight, Offset(0f, size.height - d), Size(size.width, d))
            drawRect(Win98.Highlight, Offset(size.width - d, top), Size(d, size.height - top))
        }
        if (c.text.isNotEmpty()) {
            BasicText(
                text = c.text,
                modifier = Modifier.absoluteOffset(8.dp, 0.dp).background(Win98.Face).padding(horizontal = 2.dp),
                style = win98TextStyle(),
                overflow = TextOverflow.Visible,
                maxLines = 1,
            )
        }
    }
}

// --- Edit / Combo ------------------------------------------------------------------

@Composable
fun EditView(dlg: Dlg, c: ControlTemplate, m: Modifier, enabled: Boolean) {
    val ctlEnabled = ctlEnabled(dlg, c)
    Box(
        m.background(if (ctlEnabled) Win98.Window else Win98.Face).sunken2(),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = ctlText(dlg, c),
            onValueChange = { v ->
                dlg.texts[c.id] = v
                dlg.postMessage(WM_COMMAND, c.id, 0)
            },
            modifier = Modifier.fillMaxSize().padding(start = 3.dp, end = 3.dp, top = 2.dp),
            enabled = enabled && ctlEnabled,
            textStyle = win98TextStyle(),
            singleLine = true,
            cursorBrush = SolidColor(Win98.Text),
        )
    }
}

@Composable
fun ComboView(dlg: Dlg, c: ControlTemplate, m: Modifier) {
    val items = dlg.comboItems[c.id] ?: emptyList()
    val sel = dlg.comboSel[c.id] ?: -1
    val text = if (sel in items.indices) items[sel] else ""
    val h = 9 * Win98.DLU
    Box(
        Modifier.absoluteOffset((c.x * Win98.DLU).dp, (c.y * Win98.DLU).dp)
            .requiredSize((c.w * Win98.DLU).dp, h.dp)
            .background(Win98.Window).sunken2(),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(text, Modifier.padding(start = 3.dp), style = win98TextStyle(), maxLines = 1)
        Box(
            Modifier.align(Alignment.CenterEnd).padding(2.dp).requiredSize(16.dp, (h - 4).dp)
                .background(Win98.Face).raised2(),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.requiredSize(7.dp, 4.dp)) {
                val p = Path()
                p.moveTo(0f, 0f); p.lineTo(size.width, 0f); p.lineTo(size.width / 2f, size.height); p.close()
                drawPath(p, Win98.Text)
            }
        }
    }
}

// --- Controlli custom (BMPView, BMPTipa) -----------------------------------------------

@Composable
fun CustomView(dlg: Dlg, c: ControlTemplate, m: Modifier, interactive: Boolean) {
    @Suppress("UNUSED_VARIABLE")
    val repaint = dlg.repaint // lettura dello stato: forza la ricomposizione su InvalidateRect
    val painter = CustomControls.painters[c.cls]
    val layers: List<BitmapLayer> = painter?.invoke(dlg) ?: emptyList()
    val mod = if (interactive) m.pointerInput(dlg, c.id) {
        val d = 1.dp.toPx()
        detectTapGestures { pos ->
            dlg.postMessage(WM_COMMAND, c.id, MAKELPARAM((pos.x / d).toInt(), (pos.y / d).toInt()))
        }
    } else m
    Canvas(mod.background(Color.Transparent)) {
        for (l in layers) {
            val im = BitmapCache.bitmap(l.bitmapId) ?: continue
            drawImageLp(im, l.x, l.y)
        }
    }
}
