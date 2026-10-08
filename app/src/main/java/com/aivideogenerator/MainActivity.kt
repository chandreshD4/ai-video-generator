package com.aivideogenerator

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var store: ProjectStore
    private lateinit var content: LinearLayout

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun label(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView =
        TextView(this@MainActivity).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL

            if (bold) {
                typeface = Typeface.create(
                    "sans",
                    Typeface.BOLD
                )
            }
        }

    private fun roundedBackground(
        color: Int,
        radius: Float,
        strokeColor: Int = Color.TRANSPARENT,
        strokeWidth: Int = 0
    ): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()

            if (strokeWidth > 0) {
                setStroke(dp(strokeWidth), strokeColor)
            }
        }

    private fun gradientBackground(
        startColor: Int,
        endColor: Int,
        radius: Float
    ): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = dp(radius.toInt()).toFloat()
        }

    private fun featureCard(
        icon: String,
        title: String,
        subtitle: String,
        accent: Int,
        large: Boolean = false
    ): LinearLayout {

        val card = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(if (large) 22 else 18),
                dp(if (large) 20 else 17),
                dp(if (large) 22 else 18),
                dp(if (large) 20 else 17)
            )

            background = roundedBackground(
                ThemeManager.card(this@MainActivity),
                20f,
                ThemeManager.border(this@MainActivity),
                1
            )

            isClickable = true
            isFocusable = true
        }

        val iconBox = TextView(this@MainActivity).apply {
            text = icon
            textSize = if (large) 28f else 24f
            gravity = Gravity.CENTER
            setTextColor(ThemeManager.buttonText(this@MainActivity))
            background = roundedBackground(accent, 14f)
        }

        card.addView(
            iconBox,
            LinearLayout.LayoutParams(
                dp(if (large) 54 else 48),
                dp(if (large) 54 else 48)
            )
        )

        val titleView = label(
            title,
            if (large) 20f else 17f,
            ThemeManager.primaryText(this@MainActivity),
            true
        )

        card.addView(
            titleView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
            }
        )

        val subtitleView = label(
            subtitle,
            13f,
            ThemeManager.secondaryText(this@MainActivity)
        )

        card.addView(
            subtitleView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        val arrow = label(
            "›",
            28f,
            ThemeManager.subtleText(this@MainActivity)
        )

        arrow.gravity = Gravity.CENTER

        card.addView(
            arrow,
            LinearLayout.LayoutParams(
                dp(32),
                dp(32)
            ).apply {
                gravity = Gravity.END
                topMargin = dp(-34)
            }
        )

        return card
    }

    private fun sectionTitle(
        title: String,
        action: String? = null,
        actionClick: (() -> Unit)? = null
    ): LinearLayout {

        val row = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            label(title, 19f, ThemeManager.primaryText(this@MainActivity), true),
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
                ThemeManager.accent(this@MainActivity),
                true
            )

            actionView.setOnClickListener {
                actionClick?.invoke()
            }

            row.addView(actionView)
        }

        return row
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val previousCrash = CrashLogger.consume(this@MainActivity)

        if (!previousCrash.isNullOrBlank()) {
            showCrashReport(previousCrash)
            return
        }

        ThemeManager.apply(this@MainActivity)
        store = ProjectStore(this@MainActivity)
        applySystemBars()
        buildHome()
    }

    override fun onResume() {
        super.onResume()

        if (::content.isInitialized) {
            buildHome()
        }
    }

    private fun applySystemBars() {
        val dark = ThemeManager.isDarkMode(this@MainActivity)

        window.statusBarColor = ThemeManager.background(this@MainActivity)
        window.navigationBarColor = ThemeManager.background(this@MainActivity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = 0

            if (!dark) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }

            window.decorView.systemUiVisibility = flags
        }
    }

    private fun showCrashReport(error: String) {
        val scroll = ScrollView(this@MainActivity)

        val message = TextView(this@MainActivity).apply {
            text = error
            textSize = 12f
            setTextIsSelectable(true)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        scroll.addView(message)

        AlertDialog.Builder(this@MainActivity)
            .setTitle("Previous App Crash")
            .setMessage("App पिछली बार crash हुई थी। नीचे पूरा error है।")
            .setView(scroll)
            .setNegativeButton("Close", null)
            .setPositiveButton("Copy Error") { _, _ ->
                val clipboard =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "AI Video Generator Crash",
                        error
                    )
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun buildHome() {

        val backgroundColor = ThemeManager.background(this@MainActivity)

        val root = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val scroll = ScrollView(this@MainActivity).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor)
        }

        content = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                dp(24),
                dp(20),
                dp(28)
            )
        }

        // Header
        val header = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val headingArea = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
        }

        headingArea.addView(
            label(
                "AI CREATIVE STUDIO",
                11f,
                ThemeManager.accent(this@MainActivity),
                true
            )
        )

        headingArea.addView(
            label(
                "Create something\namazing.",
                29f,
                ThemeManager.primaryText(this@MainActivity),
                true
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        )

        header.addView(
            headingArea,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val settings = TextView(this@MainActivity).apply {
            text = "⚙"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(ThemeManager.iconColor(this@MainActivity))

            background = roundedBackground(
                ThemeManager.surfaceAlt(this@MainActivity),
                15f,
                ThemeManager.border(this@MainActivity),
                1
            )
        }

        header.addView(
            settings,
            LinearLayout.LayoutParams(
                dp(50),
                dp(50)
            )
        )

        content.addView(header)

        // Hero
        val hero = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(22),
                dp(22),
                dp(22),
                dp(22)
            )

            background = gradientBackground(
                ThemeManager.homeHeroStart(this@MainActivity),
                ThemeManager.homeHeroEnd(this@MainActivity),
                24f
            )
        }

        hero.addView(
            label(
                "Turn your ideas\ninto videos",
                23f,
                ThemeManager.heroText(this@MainActivity),
                true
            )
        )

        hero.addView(
            label(
                "Generate voice, scenes, AI clips and final videos from one place.",
                13f,
                ThemeManager.secondaryText(this@MainActivity)
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
            }
        )

        val startButton = label(
            "＋  New Project",
            14f,
            ThemeManager.buttonText(this@MainActivity),
            true
        ).apply {
            gravity = Gravity.CENTER

            background = roundedBackground(
                ThemeManager.primaryAction(this@MainActivity),
                14f
            )

            setOnClickListener {
                startActivity(
                    Intent(
                        this@MainActivity,
                        ProjectWorkspaceActivity::class.java
                    )
                )
            }
        }

        hero.addView(
            startButton,
            LinearLayout.LayoutParams(
                dp(150),
                dp(46)
            ).apply {
                topMargin = dp(18)
            }
        )

        content.addView(
            hero,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        // Create
        content.addView(
            sectionTitle("Create"),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(30)
            }
        )

        val voice = featureCard(
            "🎙",
            "Voiceover Generator",
            "Turn your script into natural AI voice",
            ThemeManager.blue(this@MainActivity),
            true
        )

        content.addView(
            voice,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
            }
        )

        val row = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val scenes = featureCard(
            "🎬",
            "AI Scenes",
            "Images & clips",
            ThemeManager.green(this@MainActivity)
        )

        val render = featureCard(
            "🎞",
            "Video Render",
            "Final MP4",
            ThemeManager.red(this@MainActivity)
        )

        row.addView(
            scenes,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                topMargin = dp(14)
                rightMargin = dp(7)
            }
        )

        row.addView(
            render,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                topMargin = dp(14)
                leftMargin = dp(7)
            }
        )

        content.addView(row)

        val enhance = featureCard(
            "✨",
            "Video Quality Enhancer",
            "Upscale and improve your generated videos",
            ThemeManager.saffron(this@MainActivity)
        )

        content.addView(
            enhance,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
            }
        )

        // Recent Projects
        content.addView(
            sectionTitle(
                "Recent Projects",
                "View All"
            ) {
                startActivity(
                    Intent(
                        this@MainActivity,
                        ProjectsActivity::class.java
                    )
                )
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(30)
            }
        )

        addRecentProjects()

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
        val bottom = LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            background = roundedBackground(
                ThemeManager.card(this@MainActivity),
                20f
            )
        }

        val homeNav = navItem(
            "⌂\nHome",
            true
        )

        val projectsNav = navItem(
            "▣\nProjects",
            false
        )

        val settingsNav = navItem(
            "⚙\nSettings",
            false
        )

        projectsNav.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ProjectsActivity::class.java
                )
            )
        }

        bottom.addView(
            homeNav,
            LinearLayout.LayoutParams(
                0,
                dp(50),
                1f
            )
        )

        bottom.addView(
            projectsNav,
            LinearLayout.LayoutParams(
                0,
                dp(50),
                1f
            )
        )

        settings.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        settingsNav.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        bottom.addView(
            settingsNav,
            LinearLayout.LayoutParams(
                0,
                dp(50),
                1f
            )
        )

        root.addView(
            bottom,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(66)
            )
        )

        setContentView(root)
    }

    private fun navItem(
        value: String,
        selected: Boolean
    ): TextView =
        label(
            value,
            11f,
            if (selected)
                ThemeManager.navSelected(this@MainActivity)
            else
                ThemeManager.navUnselected(this@MainActivity),
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(
                0,
                dp(7),
                0,
                dp(7)
            )
        }

    private fun addRecentProjects() {

        val projects = store.getProjects()
            .sortedByDescending { it.updatedAt }
            .take(3)

        if (projects.isEmpty()) {

            val emptyProject = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER

                setPadding(
                    dp(20),
                    dp(25),
                    dp(20),
                    dp(25)
                )

                background = roundedBackground(
                    ThemeManager.card(this@MainActivity),
                    18f,
                    ThemeManager.border(this@MainActivity),
                    1
                )
            }

            val folder = label(
                "▣",
                28f,
                ThemeManager.mutedText(this@MainActivity),
                true
            )
            folder.gravity = Gravity.CENTER

            emptyProject.addView(folder)

            val noProjects = label(
                "No projects yet",
                15f,
                ThemeManager.primaryText(this@MainActivity),
                true
            )
            noProjects.gravity = Gravity.CENTER

            emptyProject.addView(
                noProjects,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(8)
                }
            )

            val hint = label(
                "Your generated videos will appear here.",
                12f,
                ThemeManager.subtleText(this@MainActivity)
            )
            hint.gravity = Gravity.CENTER

            emptyProject.addView(
                hint,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                }
            )

            content.addView(
                emptyProject,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(12)
                }
            )

            return
        }

        projects.forEach { project ->

            val card = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    dp(15),
                    dp(13),
                    dp(15),
                    dp(13)
                )

                background = roundedBackground(
                    ThemeManager.card(this@MainActivity),
                    15f,
                    ThemeManager.border(this@MainActivity),
                    1
                )

                setOnClickListener {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            ProjectWorkspaceActivity::class.java
                        ).apply {
                            putExtra(
                                "project_id",
                                project.id
                            )
                        }
                    )
                }
            }

            val icon = label(
                "🎬",
                20f,
                ThemeManager.buttonText(this@MainActivity)
            )
            icon.gravity = Gravity.CENTER
            icon.background = roundedBackground(
                ThemeManager.blue(this@MainActivity),
                11f
            )

            card.addView(
                icon,
                LinearLayout.LayoutParams(
                    dp(44),
                    dp(44)
                )
            )

            val info = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }

            info.addView(
                label(
                    project.name,
                    15f,
                    ThemeManager.primaryText(this@MainActivity),
                    true
                )
            )

            val preview = project.script
                .replace("\n", " ")
                .trim()
                .let {
                    if (it.length > 55)
                        it.take(55) + "…"
                    else
                        it
                }

            info.addView(
                label(
                    if (preview.isEmpty())
                        "No script added"
                    else
                        preview,
                    11f,
                    ThemeManager.subtleText(this@MainActivity)
                ),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                }
            )

            card.addView(
                info,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    leftMargin = dp(12)
                }
            )

            card.addView(
                label(
                    "›",
                    26f,
                    ThemeManager.subtleText(this@MainActivity)
                ).apply {
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(
                    dp(30),
                    dp(44)
                )
            )

            content.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(10)
                }
            )
        }
    }
}
