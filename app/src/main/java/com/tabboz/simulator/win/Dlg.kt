package com.tabboz.simulator.win

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel

/** Messaggio in coda per una finestra (equivalente di MSG). */
data class Msg(val message: Int, val wParam: Int, val lParam: Int)

/**
 * Handle di una finestra di dialogo (equivalente di HWND per i dialoghi del gioco).
 * Lo stato dei controlli e' osservabile da Compose.
 */
class Dlg internal constructor(
    val templateId: Int,
    val template: DialogTemplate?,
    val parent: Dlg?,
) {
    /** Testo corrente dei controlli (override rispetto al template). */
    val texts = mutableStateMapOf<Int, String>()
    /** Stato check di radio/checkbox. */
    val checks = mutableStateMapOf<Int, Boolean>()
    /** Abilitazione dei controlli (override rispetto al template). */
    val enabledMap = mutableStateMapOf<Int, Boolean>()
    /** Voci delle combo box. */
    val comboItems = mutableStateMapOf<Int, List<String>>()
    val comboSel = mutableStateMapOf<Int, Int>()
    /** Etichette di menu modificate a runtime (id voce -> testo). */
    val menuTexts = mutableStateMapOf<Int, String>()
    /** Voci di menu disabilitate. */
    val menuDisabled = mutableStateMapOf<Int, Boolean>()
    /** Contatore incrementato per forzare il ridisegno dei controlli custom (BMPView, BMPTipa). */
    var repaint by mutableIntStateOf(0)
        internal set
    /** Finestra visibile (ShowWindow). */
    var visible by mutableStateOf(true)
        internal set

    @Volatile var ended = false
        internal set
    var result = 0
        internal set

    internal val queue = Channel<Msg>(Channel.UNLIMITED)
    internal var timerJob: Job? = null

    /** Accoda un messaggio alla finestra (PostMessage). Usato dalla UI. */
    fun postMessage(message: Int, wParam: Int = 0, lParam: Int = 0) {
        if (!ended) queue.trySend(Msg(message, wParam, lParam))
    }

    /** Testo attuale di un controllo (override o testo del template). */
    fun textOf(id: Int): String = texts[id] ?: template?.control(id)?.text ?: ""

    fun isChecked(id: Int): Boolean = checks[id] ?: false

    fun isEnabled(id: Int): Boolean = enabledMap[id] ?: (template?.control(id)?.has("WS_DISABLED") != true)

    override fun toString() = "Dlg(#$templateId '${template?.caption}')"
}
