package com.tabboz.simulator

import android.app.Application
import com.tabboz.simulator.win.Engine

class TabbozApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Engine.init(this)
    }
}
