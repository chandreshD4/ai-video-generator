package com.aivideogenerator

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileInputStream

class SceneGalleryActivity : Activity() {

    private val purple = Color.rgb(166, 104, 255)
    private val dark = Color.rgb(18, 16, 28)
    private val cardColor = Color.rgb(35, 30, 49)
    private lateinit var project: VideoProject

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectId = intent.getStringExtra("project_id")
        val loadedProject = projectId?.let { ProjectStore(this).getProject(it) }
        if (loadedProject == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        project = loadedProject

        renderGallery()
    }

    private fun renderGallery() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(16))
            setBackgroundColor(dark)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = Button(this).apply {
            text = "‹ Back"
            setTextColor(Color.WHITE)
            setBackgroundColor(cardColor)
            setOnClickListener { finish() }
        }
        header.addView(back)

        val heading = TextView(this).apply {
            text = "  Scene Gallery"
            textSize = 23f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        header.addView(
            heading,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )
        root.addView(header)

        val subtitle = TextView(this).apply {
            text = "${project.name}  •  ${project.scenes.size} scenes"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(10), 0, dp(14))
        }
        root.addView(subtitle)

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val generated = project.scenes.filter {
            it.imagePath.isNotBlank() && File(it.imagePath).isFile
        }

        if (generated.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "No generated images yet.\nReturn to the project and generate scene images first."
                textSize = 16f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(50), dp(20), dp(50))
            })
        }

        project.scenes.forEach { scene ->
            val file = File(scene.imagePath)
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                setBackgroundColor(cardColor)
            }

            val title = TextView(this).apply {
                text = "Image ${scene.sceneNumber}  •  ${scene.title}"
                textSize = 17f
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            card.addView(title)

            if (scene.imagePath.isNotBlank() && file.isFile) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val preview = ImageView(this).apply {
                        setImageBitmap(bitmap)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        adjustViewBounds = true
                        contentDescription = "Preview of image ${scene.sceneNumber}"
                        setOnClickListener { showPreview(file) }
                    }
                    card.addView(
                        preview,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            dp(230)
                        ).apply { topMargin = dp(10) }
                    )

                    val actions = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val previewButton = Button(this).apply {
                        text = "Preview"
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.DKGRAY)
                        setOnClickListener { showPreview(file) }
                    }
                    actions.addView(
                        previewButton,
                        LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                            marginEnd = dp(6)
                        }
                    )

                    val downloadButton = Button(this).apply {
                        text = "Download"
                        setTextColor(Color.WHITE)
                        setBackgroundColor(purple)
                        setOnClickListener { saveToGallery(file, scene.sceneNumber) }
                    }
                    actions.addView(
                        downloadButton,
                        LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                            marginStart = dp(6)
                        }
                    )
                    card.addView(
                        actions,
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { topMargin = dp(8) }
                    )
                } else {
                    addStatus(card, "Image file could not be opened")
                }
            } else {
                addStatus(card, "Not generated yet")
            }

            list.addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(14) }
            )
        }

        scroll.addView(list)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        setContentView(root)
    }

    private fun addStatus(card: LinearLayout, message: String) {
        card.addView(TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(12), 0, dp(8))
        })
    }

    private fun showPreview(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap == null) {
            Toast.makeText(this, "Unable to preview image", Toast.LENGTH_SHORT).show()
            return
        }

        val image = ImageView(this).apply {
            setImageBitmap(bitmap)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        AlertDialog.Builder(this)
            .setTitle("Image Preview")
            .setView(image)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun saveToGallery(file: File, sceneNumber: Int) {
        if (!file.isFile) {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val values = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "AI_Scene_${sceneNumber}_${System.currentTimeMillis()}.jpg"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/AIVideoGenerator"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: throw IllegalStateException("Could not create gallery item")

            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(file).use { input ->
                        input.copyTo(output)
                    }
                } ?: throw IllegalStateException("Could not open output stream")

                val done = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                contentResolver.update(uri, done, null, null)
                Toast.makeText(
                    this,
                    "Saved to Pictures/AIVideoGenerator",
                    Toast.LENGTH_LONG
                ).show()
            } catch (error: Exception) {
                contentResolver.delete(uri, null, null)
                throw error
            }
        } catch (error: Exception) {
            Toast.makeText(
                this,
                "Download failed: ${error.message ?: "unknown error"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
