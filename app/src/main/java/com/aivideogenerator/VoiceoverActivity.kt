package com.aivideogenerator

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class VoiceoverActivity : Activity() {

    private lateinit var store: ProjectStore
    private lateinit var project: VideoProject

    private lateinit var scriptInput: EditText
    private lateinit var voiceSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private lateinit var statusText: TextView
    private lateinit var generateButton: TextView
    private lateinit var playButton: TextView

    private var mediaPlayer: MediaPlayer? = null

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

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
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
            "openai/tts-1",
            "openai/tts-1-hd"
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
                generateVoiceover()
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

        playButton = text(
            "▶  Play Voiceover",
            15f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = background(
                Color.rgb(35, 36, 46),
                16,
                Color.rgb(65, 66, 80)
            )
            visibility = if (project.voiceoverPath.isNotBlank()) {
                TextView.VISIBLE
            } else {
                TextView.GONE
            }

            setOnClickListener {
                playVoiceover()
            }
        }

        content.addView(
            playButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(12)
            }
        )

        val info = text(
            "The Pollinations API key stays outside the APK. This Android build only talks to the local secure bridge during testing.",
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

    private fun generateVoiceover() {

        val script = scriptInput.text.toString().trim()

        if (script.isEmpty()) {
            scriptInput.error = "Enter your video script"
            return
        }

        if (script.length > 12000) {
            scriptInput.error = "Maximum 12000 characters"
            return
        }

        val voice = voiceSpinner.selectedItem?.toString() ?: "nova"
        val model = modelSpinner.selectedItem?.toString() ?: "openai/tts-1"

        project = project.copy(
            script = script,
            updatedAt = System.currentTimeMillis(),
            voiceoverStatus = "Generating..."
        )

        store.saveProject(project)

        statusText.text = "Generating voiceover..."
        generateButton.text = "Generating..."
        generateButton.isEnabled = false
        playButton.visibility = TextView.GONE

        Thread {
            try {
                val requestBody = JSONObject().apply {
                    put("project_id", project.id)
                    put("voice", voice)
                    put("model", model)
                    put("text", script)
                }.toString()

                val connection = URL(
                    "http://127.0.0.1:8765/voiceover"
                ).openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 600000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.outputStream.use {
                    it.write(requestBody.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode

                val responseStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val response = responseStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: ""

                connection.disconnect()

                if (responseCode !in 200..299) {
                    throw Exception(
                        JSONObject(response)
                            .optString("error", "Voiceover generation failed")
                    )
                }

                val result = JSONObject(response)

                if (!result.optBoolean("ok", false)) {
                    throw Exception(
                        result.optString(
                            "error",
                            "Voiceover generation failed"
                        )
                    )
                }

                val filename = result.getString("filename")

                val encodedFilename = URLEncoder.encode(
                    filename,
                    "UTF-8"
                )

                val audioConnection = URL(
                    "http://127.0.0.1:8765/audio/$encodedFilename"
                ).openConnection() as HttpURLConnection

                audioConnection.requestMethod = "GET"
                audioConnection.connectTimeout = 15000
                audioConnection.readTimeout = 120000

                if (audioConnection.responseCode !in 200..299) {
                    throw Exception(
                        "Generated audio could not be downloaded"
                    )
                }

                val voiceoverDir = File(
                    filesDir,
                    "voiceovers"
                )

                if (!voiceoverDir.exists()) {
                    voiceoverDir.mkdirs()
                }

                val localFile = File(
                    voiceoverDir,
                    "${project.id}.mp3"
                )

                audioConnection.inputStream.use { input ->
                    FileOutputStream(localFile).use { output ->
                        input.copyTo(output)
                    }
                }

                audioConnection.disconnect()

                project = project.copy(
                    voiceoverPath = localFile.absolutePath,
                    voiceoverStatus = "Generated",
                    updatedAt = System.currentTimeMillis()
                )

                store.saveProject(project)

                runOnUiThread {
                    statusText.text =
                        "Generated successfully • ${localFile.length() / 1024} KB"

                    generateButton.text = "Generate Again"
                    generateButton.isEnabled = true
                    playButton.visibility = TextView.VISIBLE

                    Toast.makeText(
                        this,
                        "Voiceover generated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (error: Exception) {

                project = project.copy(
                    voiceoverStatus = "Generation failed",
                    updatedAt = System.currentTimeMillis()
                )

                store.saveProject(project)

                runOnUiThread {
                    statusText.text =
                        "Error: ${error.message ?: "Unknown error"}"

                    generateButton.text = "Try Again"
                    generateButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Voiceover generation failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun playVoiceover() {

        val path = project.voiceoverPath

        if (path.isBlank()) {
            Toast.makeText(
                this,
                "No voiceover available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val file = File(path)

        if (!file.exists()) {
            Toast.makeText(
                this,
                "Voiceover file not found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)

                setOnPreparedListener {
                    start()
                    playButton.text = "⏸  Playing Voiceover"
                }

                setOnCompletionListener {
                    playButton.text = "▶  Play Voiceover"
                }

                prepareAsync()
            }

        } catch (error: Exception) {
            Toast.makeText(
                this,
                "Playback failed: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
