package com.aivideogenerator

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.util.UUID

class ProjectWorkspaceActivity : Activity() {

    private lateinit var projectStore: ProjectStore
    private lateinit var project: VideoProject
    private lateinit var nameInput: EditText
    private lateinit var scriptInput: EditText

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun background(
        color: Int,
        radius: Int = 16,
        stroke: Int = Color.TRANSPARENT
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != Color.TRANSPARENT) {
            setStroke(dp(1), stroke)
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.apply(this)
        super.onCreate(savedInstanceState)

        projectStore = ProjectStore(this)

        loadProject()
        buildScreen()
    }

    override fun onResume() {
        super.onResume()

        if (::projectStore.isInitialized &&
            intent.getStringExtra("project_id") != null
        ) {
            loadProject()
            buildScreen()
        }
    }

    private fun loadProject() {

        val projectId =
            intent.getStringExtra("project_id")

        project = if (projectId != null) {
            projectStore.getProject(projectId)
                ?: createEmptyProject()
        } else {
            createEmptyProject()
        }
    }

    private fun createEmptyProject(): VideoProject {

        val now = System.currentTimeMillis()

        return VideoProject(
            id = UUID.randomUUID().toString(),
            name = "Untitled Project",
            script = "",
            createdAt = now,
            updatedAt = now
        )
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(10, 10, 15))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(12)
            )
        }

        val back = text("‹", 34f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(
                dp(45),
                dp(48)
            )
        )

        header.addView(
            text(
                "Project Workspace",
                20f,
                Color.WHITE,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dp(8)
            }
        )

        val save = text(
            "Save",
            14f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = background(
                Color.rgb(105, 70, 205),
                12
            )
            setOnClickListener {
                saveProject()

                Toast.makeText(
                    this@ProjectWorkspaceActivity,
                    "Project saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        header.addView(
            save,
            LinearLayout.LayoutParams(
                dp(72),
                dp(42)
            )
        )

        root.addView(header)

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(20),
                dp(8),
                dp(20),
                dp(35)
            )
        }

        content.addView(
            text(
                project.name,
                24f,
                Color.WHITE,
                true
            )
        )

        content.addView(
            text(
                "Build your video step by step.",
                14f,
                Color.rgb(155, 157, 170)
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        )

        content.addView(
            text(
                "PROJECT",
                11f,
                Color.rgb(150, 120, 245),
                true
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        nameInput = EditText(this).apply {
            setText(project.name)
            textSize = 17f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(110, 112, 125))
            hint = "Project name"
            setSingleLine(true)
            setPadding(
                dp(16),
                0,
                dp(16),
                0
            )
            background = background(
                Color.rgb(23, 24, 32),
                14
            )
        }

        content.addView(
            nameInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            text(
                "VIDEO SCRIPT",
                11f,
                Color.rgb(150, 120, 245),
                true
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(20)
            }
        )

        scriptInput = EditText(this).apply {
            setText(project.script)
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(105, 107, 120))
            hint = "Write or paste your video script here..."
            gravity = Gravity.TOP
            minLines = 8
            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )
            background = background(
                Color.rgb(23, 24, 32),
                16
            )
        }

        content.addView(
            scriptInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(210)
            ).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            text(
                "CREATION WORKSPACE",
                11f,
                Color.rgb(150, 120, 245),
                true
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(28)
            }
        )

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 3
            useDefaultMargins = false
        }

        addWorkspaceCard(
            grid,
            "🎙",
            "Voiceover",
            project.voiceoverStatus,
            true
        ) {
            saveProject()

            startActivity(
                Intent(
                    this,
                    VoiceoverActivity::class.java
                ).apply {
                    putExtra(
                        "project_id",
                        project.id
                    )
                }
            )
        }

        addWorkspaceCard(
            grid,
            "🖼",
            "Scenes & Images",
            "Not generated",
            false
        )

        addWorkspaceCard(
            grid,
            "🎬",
            "AI Clips",
            "Not generated",
            false
        )

        addWorkspaceCard(
            grid,
            "🎞",
            "Audio",
            "Not generated",
            false
        )

        addWorkspaceCard(
            grid,
            "🎥",
            "Final Video",
            "Not rendered",
            false
        )

        addWorkspaceCard(
            grid,
            "✨",
            "Enhancement",
            "Not processed",
            false
        )

        content.addView(
            grid,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        val generate = text(
            "Start Generation",
            16f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = background(
                Color.rgb(105, 70, 205),
                16
            )

            setOnClickListener {
                saveProject()

                Toast.makeText(
                    this@ProjectWorkspaceActivity,
                    "Project saved. Generation pipeline ready.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        content.addView(
            generate,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(24)
            }
        )

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun addWorkspaceCard(
        grid: GridLayout,
        icon: String,
        title: String,
        status: String,
        enabled: Boolean,
        action: (() -> Unit)? = null
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
            )

            background = background(
                if (enabled)
                    Color.rgb(25, 24, 36)
                else
                    Color.rgb(18, 19, 26),
                17,
                if (enabled)
                    Color.rgb(75, 60, 110)
                else
                    Color.rgb(38, 39, 49)
            )

            alpha =
                if (enabled) 1f else 0.82f

            setOnClickListener {
                action?.invoke()
            }
        }

        card.addView(
            text(
                icon,
                27f,
                Color.WHITE
            ).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )

        card.addView(
            text(
                title,
                15f,
                Color.WHITE,
                true
            ).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(7)
            }
        )

        card.addView(
            text(
                status,
                11f,
                Color.rgb(135, 137, 150)
            ).apply {
                gravity = Gravity.CENTER
                maxLines = 2
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            ).apply {
                topMargin = dp(3)
            }
        )

        val params =
            GridLayout.LayoutParams().apply {

                width = 0
                height = dp(145)

                columnSpec =
                    GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                    )

                rowSpec =
                    GridLayout.spec(
                        GridLayout.UNDEFINED,
                        1f
                    )

                setMargins(
                    dp(4),
                    dp(4),
                    dp(4),
                    dp(4)
                )
            }

        grid.addView(card, params)
    }

    private fun saveProject() {

        val name =
            nameInput.text.toString().trim()

        val script =
            scriptInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error =
                "Enter project name"
            return
        }

        project = project.copy(
            name = name,
            script = script,
            updatedAt =
                System.currentTimeMillis()
        )

        projectStore.saveProject(project)
    }
}
