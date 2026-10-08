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

    // Semantic UI colors
    // Keep these centralized so every screen can adapt safely
    // between Light and Dark mode.

    fun buttonText(context: Context): Int {
        // Colored buttons use white text in both modes.
        return Color.WHITE
    }

    fun inputText(context: Context): Int {
        return primaryText(context)
    }

    fun placeholderText(context: Context): Int {
        return hintText(context)
    }

    fun disabledText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(90, 92, 104)
        } else {
            Color.rgb(155, 155, 165)
        }
    }

    fun iconColor(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(220, 220, 230)
        } else {
            Color.rgb(45, 45, 55)
        }
    }

    fun heroText(context: Context): Int {
        // Hero has a colored background, so white remains readable.
        return Color.WHITE
    }

    fun heroSecondaryText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(220, 220, 232)
        } else {
            Color.WHITE
        }
    }

    fun buttonSecondary(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.rgb(43, 44, 56)
        } else {
            Color.rgb(235, 235, 241)
        }
    }

    fun buttonSecondaryText(context: Context): Int {
        return if (isDarkMode(context)) {
            Color.WHITE
        } else {
            Color.rgb(35, 35, 45)
        }
    }

    // Home / feature semantic colors
    fun red(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(239, 83, 80)
        else Color.rgb(211, 47, 47)
    }

    fun saffron(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(255, 179, 0)
        else Color.rgb(199, 120, 0)
    }

    fun blue(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(66, 165, 245)
        else Color.rgb(21, 101, 192)
    }

    fun green(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(102, 187, 106)
        else Color.rgb(46, 125, 50)
    }

    fun cyan(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(41, 182, 246)
        else Color.rgb(2, 119, 189)
    }

    // Primary Home action:
    // Dark mode -> blue
    // Light mode -> red
    fun primaryAction(context: Context): Int {
        return if (isDarkMode(context)) blue(context)
        else red(context)
    }

    fun homeHeroStart(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(53, 36, 105)
        else Color.rgb(47, 85, 165)
    }

    fun homeHeroEnd(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(30, 65, 110)
        else Color.rgb(18, 125, 190)
    }

    fun navSelected(context: Context): Int {
        return if (isDarkMode(context)) Color.rgb(144, 202, 249)
        else Color.rgb(21, 101, 192)
    }

    fun navUnselected(context: Context): Int {
        return subtleText(context)
    }

}
