package com.aivideogenerator

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

object CrashLogger {

    private const val PREFS = "crash_logger"
    private const val KEY_CRASH = "last_crash"

    fun install(context: Context) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            try {
                val writer = StringWriter()
                throwable.printStackTrace(PrintWriter(writer))

                context.getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                ).edit()
                    .putString(KEY_CRASH, writer.toString())
                    .apply()
            } catch (_: Exception) {
            }

            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    fun consume(context: Context): String? {
        val prefs = context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )

        val crash = prefs.getString(KEY_CRASH, null)

        if (!crash.isNullOrBlank()) {
            prefs.edit()
                .remove(KEY_CRASH)
                .apply()
        }

        return crash
    }
}
