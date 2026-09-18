package com.tabboz.simulator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tabboz.simulator.game.SalvaTutto
import com.tabboz.simulator.game.WinMain
import com.tabboz.simulator.game.hWndMain
import com.tabboz.simulator.ui.TabbozScreen
import com.tabboz.simulator.win.Engine
import com.tabboz.simulator.win.MB_ICONSTOP
import com.tabboz.simulator.win.MB_OK
import com.tabboz.simulator.win.MessageBox
import com.tabboz.simulator.win.Win98Window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

/** Avvia e controlla la coroutine principale del gioco (equivalente di WinMain). */
object GameRunner {
    private var job: Job? = null

    fun startIfNeeded() {
        if (job?.isActive == true) return
        job = Engine.scope.launch {
            var shutdown = 0
            try {
                shutdown = WinMain()
            } catch (e: Throwable) {
                Log.e(Engine.TAG, "Errore fatale nel gioco", e)
                try {
                    MessageBox(null, "Il Tabboz Simulator e' crashato:\n$e", "Errore irrecuperabile", MB_OK or MB_ICONSTOP)
                } catch (_: Throwable) {
                }
            }
            if (shutdown == 2) {
                Engine.push(Win98Window.ShutdownScreen)
                delay(3000)
            }
            exitProcess(0)
        }
    }

    /**
     * Salva la partita quando l'app va in background, ma solo se e' aperta la sola finestra principale:
     * durante alcuni dialoghi (es. acquisto scooter) le variabili globali contengono valori temporanei.
     */
    fun saveIfLoaded() {
        val main = hWndMain
        val onlyMain = main != null && Engine.windows.size == 1 &&
            (Engine.windows[0] as? Win98Window.DialogWindow)?.dlg === main
        if (onlyMain) {
            try {
                SalvaTutto()
            } catch (e: Throwable) {
                Log.w(Engine.TAG, "Salvataggio fallito", e)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Engine.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            BackHandler(enabled = true) {
                if (!Engine.onBackPressed()) finish()
            }
            TabbozScreen()
        }
        GameRunner.startIfNeeded()
    }

    override fun onStop() {
        super.onStop()
        GameRunner.saveIfLoaded()
    }
}
