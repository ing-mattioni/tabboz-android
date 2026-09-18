package com.tabboz.simulator.win

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject

/** Una finestra sullo "schermo" (stack di finestre modali). */
sealed class Win98Window {
    class DialogWindow(val dlg: Dlg) : Win98Window()

    class MessageBoxWindow(val text: String, val caption: String, val type: Int, val parent: Dlg?) : Win98Window() {
        val deferred = CompletableDeferred<Int>()
        fun complete(result: Int) { if (!deferred.isCompleted) deferred.complete(result) }

        /** Risultato "annulla" (tasto back / ESC). */
        val cancelResult: Int
            get() = when {
                (type and MB_YESNO) != 0 -> IDNO
                (type and MB_OKCANCEL) != 0 -> IDCANCEL
                else -> IDOK
            }
    }

    /** Schermata "It's now safe to turn off your computer". */
    object ShutdownScreen : Win98Window()
}

/**
 * Runtime dell'applicazione: stack delle finestre, risorse (template dialoghi, stringhe, suoni), preferenze.
 * Tutta la logica di gioco gira in una coroutine sul main thread ([scope]).
 */
object Engine {
    const val TAG = "Tabboz"

    lateinit var appContext: Context
        private set
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Finestre aperte, dalla piu' in basso alla piu' in alto. Osservabile da Compose. */
    val windows = mutableStateListOf<Win98Window>()

    var templates: Map<Int, DialogTemplate> = emptyMap()
        private set
    var strings: Map<Int, String> = emptyMap()
        private set
    var mainMenu: List<MenuItem> = emptyList()
        private set
    lateinit var sounds: SoundManager
        private set
    lateinit var prefs: SharedPreferences
        private set

    @Volatile var initialized = false
        private set

    /** Callback invocata quando il gioco termina (uscita normale o "spegni il computer"). */
    var onExit: ((shutdown: Boolean) -> Unit)? = null

    fun init(context: Context) {
        if (initialized) return
        appContext = context.applicationContext
        templates = DialogTemplates.load(appContext)
        strings = loadStrings(appContext)
        mainMenu = try { Menus.load(appContext, "ZARROSIM") } catch (e: Exception) { emptyList() }
        sounds = SoundManager(appContext)
        prefs = appContext.getSharedPreferences("tabboz", Context.MODE_PRIVATE)
        initialized = true
    }

    private fun loadStrings(context: Context): Map<Int, String> {
        val json = context.assets.open("strings/strings.json").bufferedReader().use { it.readText() }
        val obj = JSONObject(json)
        val map = HashMap<Int, String>()
        for (k in obj.keys()) {
            k.toIntOrNull()?.let { map[it] = obj.getString(k) }
        }
        return map
    }

    fun push(w: Win98Window) { windows.add(w) }
    fun pop(w: Win98Window) { windows.remove(w) }
    val top: Win98Window? get() = windows.lastOrNull()

    /** Tasto "indietro" di Android: equivale a chiudere la finestra in cima (ESC / pulsante X). */
    fun onBackPressed(): Boolean {
        return when (val t = top) {
            is Win98Window.MessageBoxWindow -> { t.complete(t.cancelResult); true }
            is Win98Window.DialogWindow -> { t.dlg.postMessage(WM_SYSCOMMAND, SC_CLOSE, 0); true }
            else -> false
        }
    }

    fun log(s: String) = Log.d(TAG, s)
}
