package com.aivideogenerator

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import android.content.Intent

class ProjectWorkspaceActivity : Activity() {

    private lateinit var projectStore: ProjectStore
    private lateinit var project: VideoProject
    private lateinit var nameInput: EditText
    private lateinit var scriptInput: EditText

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun background(color: Int, radius: Int = 16): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
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
        super.onCreate(savedInstanceState)

        projectStore = ProjectStore(this)

        val projectId = intent.getStringExtra("project_id")

        project = if (projectId != null) {
            projectStore.getProject(projectId)
                ?: createEmptyProject()
        } else {
            createEmptyProject()
        }

        buildScreen()
    }

    private fun createEmptyProject(): VideoProject {
        val now = System.currentTimeMillis()

        return VideoProject(
            id = java.util.UUID.randomUUID().toString(),
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
            setPadding(dp(18), dp(18), dp(18), dp(14))
        }

        val back = TextView(this).apply {
            text = "‹"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(45), dp(48))
        )

        val headerTitle = text(
            "Project Workspace",
            20f,
            Color.WHITE,
            true
        )

        val headerParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        headerParams.leftMargin = dp(8)

        header.addView(headerTitle, headerParams)

        val save = TextView(this).apply {
            text = "Save"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = background(Color.rgb(105, 70, 205), 12)
        }

        header.addView(
            save,
            LinearLayout.LayoutParams(dp(72), dp(42))
        )

        root.addView(header)

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(30))
        }

        val projectLabel = text(
            "PROJECT NAME",
            11f,
            Color.rgb(150, 120, 245),
            true
        )
        content.addView(projectLabel)

        nameInput = EditText(this).apply {
            setText(project.name)
            textSize = 18f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(110, 112, 125))
            hint = "Project name"
            singleLine = true
            setPadding(dp(16), 0, dp(16), 0)
            background = background(Color.rgb(23, 24, 32), 14)
        }

        val nameParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(54)
        )
        nameParams.topMargin = dp(8)
        content.addView(nameInput, nameParams)

        val scriptLabel = text(
            "VIDEO SCRIPT",
            11f,
            Color.rgb(150, 120, 245),
            true
        )

        val scriptLabelParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        scriptLabelParams.topMargin = dp(24)

        content.addView(scriptLabel, scriptLabelParams)

        scriptInput = EditText(this).apply {
            setText(project.script)
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(105, 107, 120))
            hint = "Write or paste your video script here..."
            gravity = Gravity.TOP
            minLines = 10
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = background(Color.rgb(23, 24, 32), 16)
        }

        val scriptParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(250)
        )
        scriptParams.topMargin = dp(8)
        content.addView(scriptInput, scriptParams)

        val statusTitle = text(
            "Generation Pipeline",
            19f,
            Color.WHITE,
            true
        )

        val statusTitleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        statusTitleParams.topMargin = dp(28)

        content.addView(statusTitle, statusTitleParams)

        addStatusCard(
            content,
            "🎙",
            "Voiceover",
            "Not generated"
        )

        addStatusCard(
            content,
            "🎬",
            "Scenes & Images",
            "Not generated"
        )

        addStatusCard(
            content,
            "🎞",
            "AI Clips",
            "Not generated"
        )

        addStatusCard(
            content,
            "🎥",
            "Final Video",
            "Not rendered"
        )

        addStatusCard(
            content,
            "✨",
            "Video Enhancement",
            "Not processed"
        )

        val generate = TextView(this).apply {
            text = "Start Generation"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = background(Color.rgb(105, 70, 205), 16)

            setOnClickListener {
                saveProject()
                Toast.makeText(
                    this@ProjectWorkspaceActivity,
                    "Project saved. Generation will be added next.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val generateParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(54)
        )
        generateParams.topMargin = dp(26)

        content.addView(generate, generateParams)

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        save.setOnClickListener {
            saveProject()
            Toast.makeText(
                this,
                "Project saved",
                Toast.LENGTH_SHORT
            ).show()
        }

        setContentView(root)
    }

    private fun addStatusCard(
        parent: LinearLayout,
        icon: String,
        title: String,
        status: String
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = background(
                Color.rgb(20, 21, 28),
                14
            )
        }

        val iconView = text(
            icon,
            22f,
            Color.WHITE
        )
        iconView.gravity = Gravity.CENTER

        card.addView(
            iconView,
            LinearLayout.LayoutParams(dp(42), dp(42))
        )

        val information = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleView = text(
            title,
            15f,
            Color.WHITE,
            true
        )

        val statusView = text(
            status,
            12f,
            Color.rgb(125, 127, 140)
        )

        information.addView(titleView)
        information.addView(statusView)

        val infoParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        infoParams.leftMargin = dp(12)

        card.addView(information, infoParams)

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(70)
            ).apply {
                topMargin = dp(9)
            }
        )
    }

    private fun saveProject() {

        val name = nameInput.text.toString().trim()
        val script = scriptInput.text.toString().trim()

        if (name.isEmpty()) {
            nameInput.error = "Enter project name"
            return
        }

        project = project.copy(
            name = name,
            script = script,
            updatedAt = System.currentTimeMillis()
        )

        projectStore.saveProject(project)
    }
}
