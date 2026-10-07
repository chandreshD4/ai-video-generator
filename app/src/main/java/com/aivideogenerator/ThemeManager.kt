package com.aivideogenerator

import android.content.Context
import android.content.res.Configuration

object ThemeManager {

    private const val PREFS = "app_settings"
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDarkMode(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, true)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, enabled)
            .apply()

        apply(context)
    }

    fun apply(context: Context) {
        val dark = isDarkMode(context)

        val configuration = Configuration(
            context.resources.configuration
        )

        configuration.uiMode =
            (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            if (dark) {
                Configuration.UI_MODE_NIGHT_YES
            } else {
                Configuration.UI_MODE_NIGHT_NO
            }

        context.resources.updateConfiguration(
            configuration,
            context.resources.displayMetrics
        )
    }
}
