package com.aivideogenerator

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ProjectsActivity : Activity() {

    private lateinit var store: ProjectStore
    private lateinit var projectList: LinearLayout

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun background(
        color: Int,
        radius: Int = 16,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
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
        super.onCreate(savedInstanceState)

        store = ProjectStore(this)
        buildScreen()
    }

    override fun onResume() {
        super.onResume()

        if (::projectList.isInitialized) {
            loadProjects()
        }
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

        val back = text("‹", 34f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setOnClickListener {
                finish()
            }
        }

        header.addView(
            back,
            LinearLayout.LayoutParams(dp(45), dp(48))
        )

        val title = text(
            "Projects",
            22f,
            Color.WHITE,
            true
        )

        val titleParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        titleParams.leftMargin = dp(8)

        header.addView(title, titleParams)

        val newButton = text(
            "+ New",
            14f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = background(Color.rgb(105, 70, 205), 12)

            setOnClickListener {
                startActivity(
                    Intent(
                        this@ProjectsActivity,
                        ProjectWorkspaceActivity::class.java
                    )
                )
            }
        }

        header.addView(
            newButton,
            LinearLayout.LayoutParams(dp(76), dp(42))
        )

        root.addView(header)

        val scroll = ScrollView(this)

        projectList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(30))
        }

        scroll.addView(projectList)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        loadProjects()
    }

    private fun loadProjects() {

        projectList.removeAllViews()

        val projects = store.getProjects()
            .sortedByDescending { it.updatedAt }

        if (projects.isEmpty()) {
            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(70), dp(20), dp(70))
                background = background(
                    Color.rgb(18, 19, 26),
                    18,
                    Color.rgb(38, 39, 50)
                )
            }

            val icon = text("▣", 34f, Color.rgb(105, 106, 120), true)
            icon.gravity = Gravity.CENTER

            empty.addView(icon)

            val title = text(
                "No projects yet",
                17f,
                Color.WHITE,
                true
            )
            title.gravity = Gravity.CENTER

            val titleParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            titleParams.topMargin = dp(10)

            empty.addView(title, titleParams)

            val hint = text(
                "Create your first AI video project.",
                13f,
                Color.rgb(130, 132, 145)
            )
            hint.gravity = Gravity.CENTER

            val hintParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            hintParams.topMargin = dp(5)

            empty.addView(hint, hintParams)

            projectList.addView(
                empty,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            return
        }

        projects.forEach { project ->
            addProjectCard(project)
        }
    }

    private fun addProjectCard(project: VideoProject) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17), dp(16), dp(17), dp(16))
            background = background(
                Color.rgb(22, 23, 31),
                17,
                Color.rgb(42, 43, 55)
            )

            setOnClickListener {
                openProject(project.id)
            }
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = text("🎬", 22f, Color.WHITE)
        icon.gravity = Gravity.CENTER
        icon.background = background(Color.rgb(53, 36, 105), 12)

        top.addView(
            icon,
            LinearLayout.LayoutParams(dp(46), dp(46))
        )

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val name = text(
            project.name,
            16f,
            Color.WHITE,
            true
        )

        val scriptPreview = project.script
            .replace("\n", " ")
            .trim()
            .let {
                if (it.length > 70) it.take(70) + "…" else it
            }

        val preview = text(
            if (scriptPreview.isEmpty())
                "No script added"
            else
                scriptPreview,
            12f,
            Color.rgb(145, 147, 160)
        )

        info.addView(name)
        info.addView(preview)

        val infoParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        infoParams.leftMargin = dp(12)

        top.addView(info, infoParams)

        val arrow = text("›", 27f, Color.rgb(125, 127, 140))
        arrow.gravity = Gravity.CENTER

        top.addView(
            arrow,
            LinearLayout.LayoutParams(dp(30), dp(46))
        )

        card.addView(top)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val edit = actionButton(
            "Open",
            Color.rgb(105, 70, 205)
        )

        edit.setOnClickListener {
            openProject(project.id)
        }

        val delete = actionButton(
            "Delete",
            Color.rgb(48, 49, 60)
        )

        delete.setOnClickListener {
            confirmDelete(project)
        }

        actions.addView(
            edit,
            LinearLayout.LayoutParams(
                0,
                dp(40),
                1f
            )
        )

        val deleteParams = LinearLayout.LayoutParams(
            0,
            dp(40),
            1f
        )
        deleteParams.leftMargin = dp(8)

        actions.addView(delete, deleteParams)

        val actionsParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(40)
        )
        actionsParams.topMargin = dp(14)

        card.addView(actions, actionsParams)

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = dp(11)

        projectList.addView(card, cardParams)
    }

    private fun actionButton(
        title: String,
        color: Int
    ): TextView =
        text(title, 13f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            background = background(color, 11)
        }

    private fun openProject(id: String) {
        startActivity(
            Intent(
                this,
                ProjectWorkspaceActivity::class.java
            ).apply {
                putExtra("project_id", id)
            }
        )
    }

    private fun confirmDelete(project: VideoProject) {

        AlertDialog.Builder(this)
            .setTitle("Delete Project?")
            .setMessage(
                "“${project.name}” will be removed from this device."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                store.deleteProject(project.id)

                loadProjects()
            }
            .show()
    }
}
