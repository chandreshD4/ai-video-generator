package com.aivideogenerator

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
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
        TextView(this).apply {
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
            Color.WHITE,
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
            Color.rgb(166, 168, 180)
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
            Color.rgb(130, 132, 145)
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

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(
            label(title, 19f, Color.WHITE, true),
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

            actionView.setOnClickListener {
                actionClick?.invoke()
            }

            row.addView(actionView)
        }

        return row
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        super.onCreate(savedInstanceState)

        store = ProjectStore(this)

        window.statusBarColor = Color.rgb(10, 10, 15)
        window.navigationBarColor = Color.rgb(10, 10, 15)

        buildHome()
    }

    override fun onResume() {
        super.onResume()

        if (::content.isInitialized) {
            buildHome()
        }
    }

    private fun buildHome() {

        val backgroundColor = Color.rgb(10, 10, 15)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(backgroundColor)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                dp(24),
                dp(20),
                dp(28)
            )
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val headingArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        headingArea.addView(
            label(
                "AI CREATIVE STUDIO",
                11f,
                Color.rgb(151, 120, 255),
                true
            )
        )

        headingArea.addView(
            label(
                "Create something\namazing.",
                29f,
                Color.WHITE,
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
            LinearLayout.LayoutParams(
                dp(50),
                dp(50)
            )
        )

        content.addView(header)

        // Hero
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(22),
                dp(22),
                dp(22),
                dp(22)
            )

            background = gradientBackground(
                Color.rgb(53, 36, 105),
                Color.rgb(30, 65, 110),
                24f
            )
        }

        hero.addView(
            label(
                "Turn your ideas\ninto videos",
                23f,
                Color.WHITE,
                true
            )
        )

        hero.addView(
            label(
                "Generate voice, scenes, AI clips and final videos from one place.",
                13f,
                Color.rgb(220, 220, 232)
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
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER

            background = roundedBackground(
                Color.rgb(115, 75, 220),
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
            Color.rgb(105, 75, 190),
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
            Color.rgb(155, 110, 55)
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
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
            )

            background = roundedBackground(
                Color.rgb(18, 19, 25),
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
                Color.rgb(174, 135, 255)
            else
                Color.rgb(125, 127, 140),
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

            val emptyProject = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER

                setPadding(
                    dp(20),
                    dp(25),
                    dp(20),
                    dp(25)
                )

                background = roundedBackground(
                    Color.rgb(18, 19, 26),
                    18f,
                    Color.rgb(38, 39, 50),
                    1
                )
            }

            val folder = label(
                "▣",
                28f,
                Color.rgb(105, 106, 120),
                true
            )
            folder.gravity = Gravity.CENTER

            emptyProject.addView(folder)

            val noProjects = label(
                "No projects yet",
                15f,
                Color.WHITE,
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
                Color.rgb(130, 132, 145)
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

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    dp(15),
                    dp(13),
                    dp(15),
                    dp(13)
                )

                background = roundedBackground(
                    Color.rgb(20, 21, 28),
                    15f,
                    Color.rgb(38, 39, 50),
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
                Color.WHITE
            )
            icon.gravity = Gravity.CENTER
            icon.background = roundedBackground(
                Color.rgb(53, 36, 105),
                11f
            )

            card.addView(
                icon,
                LinearLayout.LayoutParams(
                    dp(44),
                    dp(44)
                )
            )

            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            info.addView(
                label(
                    project.name,
                    15f,
                    Color.WHITE,
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
                    Color.rgb(130, 132, 145)
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
                    Color.rgb(125, 127, 140)
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
