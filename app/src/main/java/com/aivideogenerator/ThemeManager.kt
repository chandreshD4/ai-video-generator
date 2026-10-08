package com.aivideogenerator

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

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

        val configuration = Configuration(context.resources.configuration)
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

    fun background(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(11, 11, 16)
        } else {
            Color.rgb(248, 248, 250)
        }
    }

    fun card(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(25, 25, 32)
        } else {
            Color.WHITE
        }
    }

    fun primaryText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.WHITE
        } else {
            Color.rgb(20, 20, 24)
        }
    }

    fun secondaryText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.LTGRAY
        } else {
            Color.rgb(90, 90, 98)
        }
    }


    fun hintText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(105, 107, 120)
        } else {
            Color.rgb(120, 120, 130)
        }
    }

    fun inputBackground(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(23, 24, 32)
        } else {
            Color.rgb(242, 242, 246)
        }
    }

    fun inputStroke(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(43, 44, 56)
        } else {
            Color.rgb(210, 210, 218)
        }
    }

    fun accent(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(150, 120, 245)
        } else {
            Color.rgb(105, 70, 205)
        }
    }

    fun accentStrong(context: Context): Int {
        return Color.rgb(105, 70, 205)
    }

    fun mutedText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(145, 147, 160)
        } else {
            Color.rgb(90, 90, 98)
        }
    }

    fun subtleText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(120, 122, 135)
        } else {
            Color.rgb(105, 107, 120)
        }
    }

    fun surface(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(18, 19, 26)
        } else {
            Color.WHITE
        }
    }

    fun surfaceAlt(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(25, 24, 36)
        } else {
            Color.rgb(242, 242, 246)
        }
    }

    fun border(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(43, 44, 56)
        } else {
            Color.rgb(210, 210, 218)
        }
    }

    fun sectionBackground(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(20, 21, 29)
        } else {
            Color.rgb(245, 245, 248)
        }
    }
}
