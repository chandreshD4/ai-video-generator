package com.aivideogenerator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun card(
        icon: String,
        title: String,
        subtitle: String
    ): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            setBackgroundColor(Color.rgb(25, 25, 34))
        }

        layout.addView(
            text(icon, 28f, Color.WHITE, false),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val titleView = text(title, 19f, Color.WHITE, true)
        val titleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        titleParams.topMargin = dp(10)
        layout.addView(titleView, titleParams)

        val subtitleView = text(subtitle, 14f, Color.LTGRAY)
        val subtitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        subtitleParams.topMargin = dp(5)
        layout.addView(subtitleView, subtitleParams)

        return layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val background = Color.rgb(11, 11, 16)
        val white = Color.WHITE
        val gray = Color.rgb(170, 170, 185)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(30))
            setBackgroundColor(background)
        }

        val title = text("AI Video Generator", 28f, white, true)
        content.addView(title)

        val subtitle = text(
            "Create videos with AI",
            15f,
            gray
        )
        val subtitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        subtitleParams.topMargin = dp(5)
        content.addView(subtitle, subtitleParams)

        val section = text("Create", 18f, white, true)
        val sectionParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        sectionParams.topMargin = dp(30)
        content.addView(section, sectionParams)

        val cards = listOf(
            card(
                "🎙️",
                "Voiceover Generator",
                "Turn your script into AI voice audio"
            ),
            card(
                "🎬",
                "Scene & AI Clip Generator",
                "Create scenes, images and short AI clips"
            ),
            card(
                "🎞️",
                "Final Video Render",
                "Combine audio, scenes and clips into a video"
            ),
            card(
                "✨",
                "Video Quality Enhancer",
                "Upscale and improve your generated videos"
            )
        )

        for (item in cards) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dp(14)
            content.addView(item, params)
        }

        val projects = text("Projects", 18f, white, true)
        val projectParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        projectParams.topMargin = dp(30)
        content.addView(projects, projectParams)

        val empty = text(
            "No projects yet",
            15f,
            gray
        )
        val emptyParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        emptyParams.topMargin = dp(12)
        content.addView(empty, emptyParams)

        val scrollView = ScrollView(this)
        scrollView.setBackgroundColor(background)
        scrollView.addView(content)

        setContentView(scrollView)
    }
}
