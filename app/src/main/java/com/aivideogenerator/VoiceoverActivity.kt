package com.aivideogenerator

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class VoiceoverActivity : Activity() {

    private lateinit var store: ProjectStore
    private lateinit var project: VideoProject

    private lateinit var scriptInput: EditText
    private lateinit var voiceSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private lateinit var statusText: TextView
    private lateinit var generateButton: TextView

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

        if (bold) {
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        store = ProjectStore(this)

        val projectId = intent.getStringExtra("project_id")

        if (projectId == null) {
            finish()
            return
        }

        project = store.getProject(projectId) ?: run {
            finish()
            return
        }

        buildScreen()
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
            "Voiceover Generator",
            20f,
            Color.WHITE,
            true
        )

        header.addView(
            title,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dp(8)
            }
        )

        root.addView(header)

        val scroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(35))
        }

        val projectTitle = text(
            project.name,
            24f,
            Color.WHITE,
            true
        )

        content.addView(projectTitle)

        val subtitle = text(
            "Turn your script into natural AI narration.",
            14f,
            Color.rgb(155, 157, 170)
        )

        content.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        val scriptLabel = text(
            "SCRIPT",
            11f,
            Color.rgb(150, 120, 245),
            true
        )

        content.addView(
            scriptLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(25)
            }
        )

        scriptInput = EditText(this).apply {
            setText(project.script)
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(105, 107, 120))
            hint = "Your video script..."
            gravity = Gravity.TOP
            minLines = 12
            setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
            )
            background = background(
                Color.rgb(23, 24, 32),
                16,
                Color.rgb(43, 44, 56)
            )
        }

        content.addView(
            scriptInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(290)
            ).apply {
                topMargin = dp(8)
            }
        )

        val voiceLabel = text(
            "VOICE",
            11f,
            Color.rgb(150, 120, 245),
            true
        )

        content.addView(
            voiceLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(22)
            }
        )

        voiceSpinner = Spinner(this)

        val voices = listOf(
            "nova",
            "alloy",
            "echo",
            "fable",
            "onyx",
            "shimmer",
            "coral",
            "sage",
            "verse"
        )

        voiceSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            voices
        )

        content.addView(
            voiceSpinner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(8)
            }
        )

        val modelLabel = text(
            "VOICE MODEL",
            11f,
            Color.rgb(150, 120, 245),
            true
        )

        content.addView(
            modelLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(20)
            }
        )

        modelSpinner = Spinner(this)

        val models = listOf(
            "elevenlabs/eleven-v3",
            "elevenlabs/eleven-flash-v2.5",
            "elevenlabs/eleven-multilingual-v2",
            "google/gemini-3.8-flash-tts",
            "qwen/qwen3-tts-flash",
            "hexgrad/kokoro-82m"
        )

        modelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            models
        )

        content.addView(
            modelSpinner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(8)
            }
        )

        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = background(
                Color.rgb(20, 21, 28),
                15
            )
        }

        val statusTitle = text(
            "Generation Status",
            14f,
            Color.WHITE,
            true
        )

        statusCard.addView(statusTitle)

        statusText = text(
            project.voiceoverStatus,
            13f,
            Color.rgb(140, 142, 155)
        )

        statusCard.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        )

        content.addView(
            statusCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(22)
            }
        )

        generateButton = text(
            "Generate Voiceover",
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
                prepareGeneration()
            }
        }

        content.addView(
            generateButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(22)
            }
        )

        val info = text(
            "Your API connection will be handled securely outside the APK. No secret API key is stored in this app.",
            12f,
            Color.rgb(115, 117, 130)
        )

        info.setPadding(
            dp(4),
            dp(14),
            dp(4),
            0
        )

        content.addView(info)

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

    private fun prepareGeneration() {

        val script = scriptInput.text.toString().trim()

        if (script.isEmpty()) {
            scriptInput.error = "Enter your video script"
            return
        }

        project = project.copy(
            script = script,
            updatedAt = System.currentTimeMillis(),
            voiceoverStatus = "Ready to generate"
        )

        store.saveProject(project)

        statusText.text =
            "Voiceover configuration saved. Secure API connection will be used for generation."

        generateButton.text = "Generation Ready"

        Toast.makeText(
            this,
            "Voiceover settings saved",
            Toast.LENGTH_SHORT
        ).show()
    }
}
