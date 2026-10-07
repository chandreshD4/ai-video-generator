package com.aivideogenerator

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class SettingsActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun text(
        value: String,
        size: Float,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            if (bold) {
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
        }
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            setBackgroundColor(Color.rgb(25, 25, 32))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.apply(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            setBackgroundColor(Color.rgb(11, 11, 16))
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 38f
            setTextColor(Color.WHITE)
            setPadding(0, 0, dp(18), 0)
            setOnClickListener { finish() }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(48), dp(60))
        )

        header.addView(
            text("Settings", 25f, true),
            LinearLayout.LayoutParams(
                0,
                dp(60),
                1f
            )
        )

        root.addView(header)

        val appearanceCard = card()

        appearanceCard.addView(
            text("Appearance", 19f, true)
        )

        val description = text(
            "Choose how the AI Video Generator looks.",
            14f
        )
        description.setTextColor(Color.LTGRAY)

        appearanceCard.addView(
            description,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
                bottomMargin = dp(14)
            }
        )

        val darkRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val darkText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        darkText.addView(
            text("Dark Mode", 17f, true)
        )

        val modeDescription = text(
            "Use the dark interface throughout the app.",
            13f
        )
        modeDescription.setTextColor(Color.LTGRAY)

        darkText.addView(modeDescription)

        darkRow.addView(
            darkText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val darkSwitch = Switch(this).apply {
            isChecked = ThemeManager.isDarkMode(this@SettingsActivity)
            setOnCheckedChangeListener { _, checked ->
                ThemeManager.setDarkMode(
                    this@SettingsActivity,
                    checked
                )
                recreate()
            }
        }

        darkRow.addView(
            darkSwitch,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        appearanceCard.addView(darkRow)

        root.addView(
            appearanceCard,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )

        val infoCard = card()

        infoCard.addView(
            text("AI Video Generator", 17f, true)
        )

        val info = text(
            "Video creation workspace • Voiceover • Scenes • AI Clips",
            13f
        )
        info.setTextColor(Color.LTGRAY)

        infoCard.addView(
            info,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(7)
            }
        )

        root.addView(
            infoCard,
            LinearLayout.LayoutParams(
                -1,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        )

        setContentView(root)
    }
}
