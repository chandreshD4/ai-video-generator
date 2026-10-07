package com.aivideogenerator

import android.app.Application

class AIVideoGeneratorApp : Application() {

    override fun onCreate() {
        super.onCreate()

        CrashLogger.install(this)
    }
}
