@file:Suppress("FunctionName", "unused")

package com.tabboz.simulator.win

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// -----------------------------------------------------------------------------
// API "Win32-like" usata dalla logica di gioco. I nomi ricalcano quelli originali
// per rendere meccanica la traduzione del codice C.
// -----------------------------------------------------------------------------

/**
 * Crea e mostra un dialogo modale (DialogBox). Sospende finche' la procedura non chiama [EndDialog]
 * (o l'utente chiude la finestra). Restituisce il valore passato a EndDialog.
 */
suspend fun DialogBox(templateId: Int, parent: Dlg?, proc: DlgProc): Int {
    val template = Engine.templates[templateId]
    if (template == null) Engine.log("DialogBox: template $templateId non trovato")
    val dlg = Dlg(templateId, template, parent)
    // Stato iniziale dei controlli
    template?.controls?.forEach { c ->
        if (c.has("WS_DISABLED")) dlg.enabledMap[c.id] = false
    }
    val window = Win98Window.DialogWindow(dlg)
    Engine.push(window)
    try {
        proc(dlg, WM_INITDIALOG, 0, 0)
        while (!dlg.ended) {
            val msg = dlg.queue.receive()
            if (dlg.ended) break
            when (msg.message) {
                WM_SYSCOMMAND -> {
                    if (msg.wParam == SC_CLOSE) {
                        // Come in Windows: se la procedura non gestisce SC_CLOSE, il dialogo riceve IDCANCEL.
                        if (!proc(dlg, WM_SYSCOMMAND, SC_CLOSE, 0) && !dlg.ended) {
                            proc(dlg, WM_COMMAND, IDCANCEL, 0)
                        }
                    } else {
                        proc(dlg, WM_SYSCOMMAND, msg.wParam, msg.lParam)
                    }
                }
                else -> proc(dlg, msg.message, msg.wParam, msg.lParam)
            }
        }
    } finally {
        KillTimer(dlg)
        dlg.ended = true
        Engine.pop(window)
    }
    return dlg.result
}

/** Termina un dialogo modale (EndDialog). */
fun EndDialog(hDlg: Dlg, result: Int = 1) {
    hDlg.result = result
    hDlg.ended = true
    // Sblocca il loop dei messaggi se e' in attesa.
    hDlg.queue.trySend(Msg(WM_CLOSE, 0, 0))
}

/**
 * Mostra un message box modale e sospende fino alla pressione di un pulsante.
 * Restituisce IDOK, IDCANCEL, IDYES o IDNO.
 */
suspend fun MessageBox(parent: Dlg?, text: String, caption: String, type: Int = MB_OK): Int {
    val mb = Win98Window.MessageBoxWindow(text, caption, type, parent)
    Engine.push(mb)
    try {
        return mb.deferred.await()
    } finally {
        Engine.pop(mb)
    }
}

/** Imposta il testo di un controllo (SetDlgItemText). `text == null` equivale a stringa vuota. */
fun SetDlgItemText(hDlg: Dlg, id: Int, text: String?) {
    hDlg.texts[id] = text ?: ""
}

/** Legge il testo di un controllo (GetDlgItemText). */
fun GetDlgItemText(hDlg: Dlg, id: Int): String = hDlg.textOf(id)

private fun ControlTemplate.isRadio(): Boolean =
    cls == "BorRadio" || (cls == "BUTTON" && (has("BS_AUTORADIOBUTTON") || has("BS_RADIOBUTTON")))

/** Imposta lo stato di un radio button / check box (BM_SETCHECK). Per i radio, deseleziona gli altri del dialogo. */
fun CheckDlgButton(hDlg: Dlg, id: Int, checked: Boolean) {
    val ctl = hDlg.template?.control(id)
    if (checked && ctl != null && ctl.isRadio()) {
        hDlg.template.controls.forEach { c ->
            if (c.isRadio() && c.id != id) hDlg.checks[c.id] = false
        }
    }
    hDlg.checks[id] = checked
}

/** Legge lo stato di un radio button / check box (BM_GETCHECK). */
fun IsDlgButtonChecked(hDlg: Dlg, id: Int): Boolean = hDlg.isChecked(id)

/** Abilita/disabilita un controllo (EnableWindow(GetDlgItem(...))). */
fun EnableWindow(hDlg: Dlg, id: Int, enabled: Boolean) {
    hDlg.enabledMap[id] = enabled
}

/** Mostra/nasconde una finestra (ShowWindow). */
fun ShowWindow(hDlg: Dlg?, show: Boolean) {
    hDlg?.visible = show
}

/** Aggiunge una voce ad una combo box (CB_ADDSTRING). */
fun ComboAddString(hDlg: Dlg, id: Int, s: String) {
    hDlg.comboItems[id] = (hDlg.comboItems[id] ?: emptyList()) + s
}

/** Seleziona una voce di una combo box (CB_SETCURSEL). */
fun ComboSetCurSel(hDlg: Dlg, id: Int, index: Int) {
    hDlg.comboSel[id] = index
}

/** Cambia l'etichetta di una voce del menu della finestra principale (sostituisce DeleteMenu/AppendMenu). */
fun SetMenuItemText(hDlg: Dlg, menuId: Int, text: String) {
    hDlg.menuTexts[menuId] = text
}

/** Disabilita/abilita una voce del menu. */
fun EnableMenuItem(hDlg: Dlg, menuId: Int, enabled: Boolean) {
    hDlg.menuDisabled[menuId] = !enabled
}

/** Forza il ridisegno dei controlli custom (InvalidateRect/UpdateWindow). */
fun InvalidateRect(hDlg: Dlg?) {
    if (hDlg != null) hDlg.repaint++
}

/** Avvia un timer periodico che invia WM_TIMER alla finestra (SetTimer). */
fun SetTimer(hDlg: Dlg, ms: Long) {
    KillTimer(hDlg)
    hDlg.timerJob = Engine.scope.launch {
        while (!hDlg.ended) {
            delay(ms)
            if (!hDlg.ended) hDlg.postMessage(WM_TIMER, 0, 0)
        }
    }
}

/** Ferma il timer (KillTimer). */
fun KillTimer(hDlg: Dlg) {
    hDlg.timerJob?.cancel()
    hDlg.timerJob = null
}

/** Carica una stringa dalle risorse (LoadString). Restituisce "" se non esiste. */
fun LoadString(id: Int): String = Engine.strings[id] ?: ""

/** Riproduce il suono tabsNNNN.wav (TabbozPlaySound). */
fun TabbozPlaySound(number: Int) {
    if (Engine.initialized) Engine.sounds.play(number)
}

/** Ferma tutti i suoni (SpegniISuoni). */
fun SpegniISuoni() {
    if (Engine.initialized) Engine.sounds.stopAll()
}

/** Equivalente di random(n) del Borland C: intero casuale in [0, n). Per n <= 0 restituisce 0. */
fun random(n: Int): Int = if (n <= 0) 0 else Random.nextInt(n)

/** randomize(): non necessario in Kotlin. */
fun randomize() {}

/** Scrive una riga nel log (writelog). */
fun writelog(s: String) = Engine.log(s)

// ----------------------------------------------------------------------------
// Persistenza (equivalente del registro di configurazione: readkey.c)
// ----------------------------------------------------------------------------

/** Salva una stringa (TabbozAddKey). */
fun TabbozAddKey(key: String, value: String) {
    Engine.prefs.edit().putString(key, value).apply()
}

/** Legge una stringa; null se la chiave non esiste (TabbozReadKey). */
fun TabbozReadKey(key: String): String? = Engine.prefs.getString(key, null)

/** Legge una stringa; "" se la chiave non esiste (RRKey). */
fun RRKey(key: String): String = TabbozReadKey(key) ?: ""

/** Cancella tutte le chiavi salvate. */
fun TabbozClearKeys() {
    Engine.prefs.edit().clear().apply()
}

/** atoi/atol tolleranti come in C: prefisso numerico, 0 se non valido. */
fun atoi(s: String?): Int = atol(s).toInt()

fun atol(s: String?): Long {
    if (s == null) return 0
    val t = s.trim()
    var i = 0
    var neg = false
    if (i < t.length && (t[i] == '-' || t[i] == '+')) { neg = t[i] == '-'; i++ }
    var v = 0L
    var any = false
    while (i < t.length && t[i].isDigit()) { v = v * 10 + (t[i] - '0'); i++; any = true }
    if (!any) return 0
    return if (neg) -v else v
}
