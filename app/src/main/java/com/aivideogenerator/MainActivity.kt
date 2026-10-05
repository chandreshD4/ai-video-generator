package com.aivideogenerator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun label(
        text: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL

            if (bold) {
                typeface = Typeface.create("sans", Typeface.BOLD)
            }
        }
    }

    private fun roundedBackground(
        color: Int,
        radius: Float,
        strokeColor: Int = Color.TRANSPARENT,
        strokeWidth: Int = 0
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()

            if (strokeWidth > 0) {
                setStroke(dp(strokeWidth), strokeColor)
            }
        }
    }

    private fun gradientBackground(
        startColor: Int,
        endColor: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = dp(radius.toInt()).toFloat()
        }
    }

    private fun featureCard(
        icon: String,
        title: String,
        subtitle: String,
        accent: Int,
        large: Boolean = false
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(if (large) 22 else 18),
                dp(if (large) 20 else 17),
                dp(if (large) 22 else 18),
                dp(if (large) 20 else 17)
            )

            background = roundedBackground(
                Color.rgb(23, 24, 32),
                20f,
                Color.rgb(45, 46, 58),
                1
            )

            isClickable = true
            isFocusable = true
        }

        val iconBox = TextView(this).apply {
            text = icon
            textSize = if (large) 28f else 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedBackground(accent, 14f)

            layoutParams = LinearLayout.LayoutParams(
                dp(if (large) 54 else 48),
                dp(if (large) 54 else 48)
            )
        }

        card.addView(iconBox)

        val titleView = label(
            title,
            if (large) 20f else 17f,
            Color.WHITE,
            true
        )

        val titleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        titleParams.topMargin = dp(14)
        card.addView(titleView, titleParams)

        val subtitleView = label(
            subtitle,
            13f,
            Color.rgb(166, 168, 180)
        )

        val subtitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        subtitleParams.topMargin = dp(6)
        card.addView(subtitleView, subtitleParams)

        val arrow = label("›", 28f, Color.rgb(130, 132, 145))
        arrow.gravity = Gravity.CENTER

        val arrowParams = LinearLayout.LayoutParams(
            dp(32),
            dp(32)
        )
        arrowParams.gravity = Gravity.END
        arrowParams.topMargin = dp(-34)
        card.addView(arrow, arrowParams)

        return card
    }

    private fun sectionTitle(
        title: String,
        action: String? = null
    ): LinearLayout {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = label(title, 19f, Color.WHITE, true)

        row.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        if (action != null) {
            val actionView = label(
                action,
                13f,
                Color.rgb(155, 120, 255),
                true
            )
            row.addView(actionView)
        }

        return row
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(10, 10, 15)
        window.navigationBarColor = Color.rgb(10, 10, 15)

        val backgroundColor = Color.rgb(10, 10, 15)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor)
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val headingArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val smallTitle = label(
            "AI CREATIVE STUDIO",
            11f,
            Color.rgb(151, 120, 255),
            true
        )
        headingArea.addView(smallTitle)

        val title = label(
            "Create something\namazing.",
            29f,
            Color.WHITE,
            true
        )

        val titleParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        titleParams.topMargin = dp(5)

        header.addView(headingArea, titleParams)

        val settings = TextView(this).apply {
            text = "⚙"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = roundedBackground(
                Color.rgb(25, 26, 35),
                15f,
                Color.rgb(48, 49, 61),
                1
            )
        }

        header.addView(
            settings,
            LinearLayout.LayoutParams(dp(50), dp(50))
        )

        content.addView(header)

        // Hero
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(22))

            background = gradientBackground(
                Color.rgb(53, 36, 105),
                Color.rgb(30, 65, 110),
                24f
            )
        }

        val heroTitle = label(
            "Turn your ideas\ninto videos",
            23f,
            Color.WHITE,
            true
        )
        hero.addView(heroTitle)

        val heroSubtitle = label(
            "Generate voice, scenes, AI clips and final videos from one place.",
            13f,
            Color.rgb(220, 220, 232)
        )

        val heroSubtitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        heroSubtitleParams.topMargin = dp(9)
        hero.addView(heroSubtitle, heroSubtitleParams)

        val startButton = TextView(this).apply {
            text = "＋  New Project"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = roundedBackground(
                Color.rgb(115, 75, 220),
                14f
            )
        }

        val buttonParams = LinearLayout.LayoutParams(
            dp(150),
            dp(46)
        )
        buttonParams.topMargin = dp(18)
        hero.addView(startButton, buttonParams)

        val heroParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        heroParams.topMargin = dp(24)
        content.addView(hero, heroParams)

        // Create section
        val createTitle = sectionTitle("Create")
        val createParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        createParams.topMargin = dp(30)
        content.addView(createTitle, createParams)

        // Voiceover
        val voice = featureCard(
            "🎙",
            "Voiceover Generator",
            "Turn your script into natural AI voice",
            Color.rgb(105, 75, 190),
            true
        )

        val voiceParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        voiceParams.topMargin = dp(14)
        content.addView(voice, voiceParams)

        // Two cards row
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val scenes = featureCard(
            "🎬",
            "AI Scenes",
            "Images & clips",
            Color.rgb(35, 105, 150)
        )

        val render = featureCard(
            "🎞",
            "Video Render",
            "Final MP4",
            Color.rgb(155, 75, 115)
        )

        val leftParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        leftParams.topMargin = dp(14)
        leftParams.rightMargin = dp(7)

        val rightParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        rightParams.topMargin = dp(14)
        rightParams.leftMargin = dp(7)

        row.addView(scenes, leftParams)
        row.addView(render, rightParams)

        content.addView(row)

        // Enhancer
        val enhance = featureCard(
            "✨",
            "Video Quality Enhancer",
            "Upscale and improve your generated videos",
            Color.rgb(155, 110, 55)
        )

        val enhanceParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        enhanceParams.topMargin = dp(14)
        content.addView(enhance, enhanceParams)

        // Projects
        val projectsTitle = sectionTitle(
            "Recent Projects",
            "View All"
        )

        val projectsParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        projectsParams.topMargin = dp(30)
        content.addView(projectsTitle, projectsParams)

        val emptyProject = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(25), dp(20), dp(25))

            background = roundedBackground(
                Color.rgb(18, 19, 26),
                18f,
                Color.rgb(38, 39, 50),
                1
            )
        }

        val folder = label("▣", 28f, Color.rgb(105, 106, 120), true)
        folder.gravity = Gravity.CENTER
        emptyProject.addView(folder)

        val noProjects = label(
            "No projects yet",
            15f,
            Color.WHITE,
            true
        )
        noProjects.gravity = Gravity.CENTER

        val noProjectsParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        noProjectsParams.topMargin = dp(8)
        emptyProject.addView(noProjects, noProjectsParams)

        val projectHint = label(
            "Your generated videos will appear here.",
            12f,
            Color.rgb(130, 132, 145)
        )
        projectHint.gravity = Gravity.CENTER

        val hintParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        hintParams.topMargin = dp(4)
        emptyProject.addView(projectHint, hintParams)

        val emptyParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        emptyParams.topMargin = dp(12)
        content.addView(emptyProject, emptyParams)

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Bottom navigation
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = roundedBackground(
                Color.rgb(18, 19, 25),
                20f
            )
        }

        val navItems = listOf(
            "⌂\nHome",
            "▣\nProjects",
            "⚙\nSettings"
        )

        for ((index, item) in navItems.withIndex()) {
            val nav = TextView(this).apply {
                text = item
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(
                    if (index == 0)
                        Color.rgb(174, 135, 255)
                    else
                        Color.rgb(125, 127, 140)
                )
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, dp(7), 0, dp(7))
            }

            bottom.addView(
                nav,
                LinearLayout.LayoutParams(
                    0,
                    dp(50),
                    1f
                )
            )
        }

        root.addView(
            bottom,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(66)
            )
        )

        setContentView(root)
    }
}
