package com.aivideogenerator

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class VoiceoverActivity : Activity() {

    private lateinit var store: ProjectStore
    private lateinit var project: VideoProject

    private lateinit var scriptInput: EditText
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var generateButton: TextView
    private lateinit var playButton: TextView

    private var selectedVoice = "nova"
    private var selectedModel = "openai/tts-1"
    private var mediaPlayer: MediaPlayer? = null

    private val femaleVoices = listOf(
        "nova",
        "shimmer",
        "coral",
        "rachel",
        "domi",
        "bella",
        "elli",
        "charlotte",
        "dorothy",
        "sarah",
        "emily",
        "lily",
        "matilda"
    )

    private val maleVoices = listOf(
        "alloy",
        "echo",
        "fable",
        "onyx",
        "sage",
        "verse",
        "adam",
        "antoni",
        "arnold",
        "josh",
        "sam",
        "daniel",
        "charlie",
        "james",
        "fin",
        "callum",
        "liam",
        "george",
        "brian",
        "bill"
    )

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
            setOnClickListener { finish() }
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

        content.addView(
            text(project.name, 24f, Color.WHITE, true)
        )

        content.addView(
            text(
                "Turn your script into natural AI narration.",
                14f,
                Color.rgb(155, 157, 170)
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        content.addView(
            text(
                "SCRIPT",
                11f,
                Color.rgb(150, 120, 245),
                true
            ),
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
            setPadding(dp(16), dp(16), dp(16), dp(16))
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

        content.addView(
            text(
                "VOICE",
                11f,
                Color.rgb(150, 120, 245),
                true
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(22)
            }
        )

        val voiceContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = background(
                Color.rgb(18, 19, 26),
                15,
                Color.rgb(42, 43, 54)
            )
        }

        addVoiceSection(
            voiceContainer,
            "Female Voices",
            femaleVoices
        )

        addVoiceSection(
            voiceContainer,
            "Male Voices",
            maleVoices
        )

        content.addView(
            voiceContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            text(
                "VOICE MODEL",
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

        val modelContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = background(
                Color.rgb(18, 19, 26),
                15,
                Color.rgb(42, 43, 54)
            )
        }

        addModelOption(
            modelContainer,
            "openai/tts-1",
            "Standard quality"
        )

        addModelOption(
            modelContainer,
            "openai/tts-1-hd",
            "Higher quality"
        )

        content.addView(
            modelContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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

        statusCard.addView(
            text(
                "Generation Status",
                14f,
                Color.WHITE,
                true
            )
        )

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

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
            visibility = View.GONE
        }

        statusCard.addView(
            progressBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(8)
            ).apply {
                topMargin = dp(14)
            }
        )

        progressText = text(
            "",
            12f,
            Color.rgb(145, 147, 160)
        )

        statusCard.addView(
            progressText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
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

            visibility =
                if (project.voiceoverPath.isNotBlank()) {
                    View.VISIBLE
                } else {
                    View.GONE
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

        content.addView(
            text(
                "API key stays outside the APK. Voice generation is handled through the secure bridge during testing.",
                12f,
                Color.rgb(115, 117, 130)
            ).apply {
                setPadding(dp(4), dp(14), dp(4), 0)
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

    private fun addVoiceSection(
        parent: LinearLayout,
        title: String,
        voices: List<String>
    ) {

        parent.addView(
            text(
                title,
                13f,
                Color.WHITE,
                true
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
                bottomMargin = dp(4)
            }
        )

        voices.forEach { voice ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(4), dp(2), dp(4), dp(2))
                background = background(
                    Color.TRANSPARENT,
                    10
                )
            }

            val radio = RadioButton(this).apply {
                isChecked = voice == selectedVoice
                buttonTintList = null
            }

            val name = text(
                voice.replaceFirstChar {
                    it.uppercase()
                },
                15f,
                Color.WHITE
            )

            row.addView(
                radio,
                LinearLayout.LayoutParams(
                    dp(48),
                    dp(48)
                )
            )

            row.addView(
                name,
                LinearLayout.LayoutParams(
                    0,
                    dp(48),
                    1f
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            )

            val click = {
                selectedVoice = voice

                updateAllVoiceRadioButtons(
                    voice
                )
            }

            radio.setOnClickListener {
                click()
            }

            row.setOnClickListener {
                click()
            }

            parent.addView(row)
        }
    }

    private fun updateAllVoiceRadioButtons(
        voice: String
    ) {
        selectedVoice = voice

        val root = window.decorView

        updateRadioButtons(
            root,
            voice
        )
    }

    private fun updateRadioButtons(
        view: View,
        voice: String
    ) {

        if (view is LinearLayout) {

            for (i in 0 until view.childCount) {

                val child = view.getChildAt(i)

                if (child is LinearLayout &&
                    child.childCount > 0
                ) {

                    val radio =
                        child.getChildAt(0)

                    val nameView =
                        if (child.childCount > 1)
                            child.getChildAt(1)
                        else null

                    if (radio is RadioButton &&
                        nameView is TextView
                    ) {

                        val rowVoice =
                            nameView.text.toString()

                        radio.isChecked =
                            rowVoice.equals(
                                voice,
                                ignoreCase = true
                            )
                    }
                }

                updateRadioButtons(
                    child,
                    voice
                )
            }
        }
    }

    private fun addModelOption(
        parent: LinearLayout,
        model: String,
        description: String
    ) {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(2), dp(4), dp(2))
        }

        val radio = RadioButton(this).apply {
            isChecked = model == selectedModel
        }

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        labels.addView(
            text(
                model,
                14f,
                Color.WHITE,
                true
            )
        )

        labels.addView(
            text(
                description,
                12f,
                Color.rgb(135, 137, 150)
            )
        )

        row.addView(
            radio,
            LinearLayout.LayoutParams(
                dp(48),
                dp(58)
            )
        )

        row.addView(
            labels,
            LinearLayout.LayoutParams(
                0,
                dp(58),
                1f
            )
        )

        val select = {
            selectedModel = model

            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)

                if (child is LinearLayout &&
                    child.childCount > 0
                ) {
                    val r = child.getChildAt(0)

                    if (r is RadioButton) {
                        val selected =
                            child.getTag() == model

                        r.isChecked = selected
                    }
                }
            }
        }

        row.tag = model

        radio.setOnClickListener {
            select()
        }

        row.setOnClickListener {
            select()
        }

        parent.addView(row)
    }

    private fun generateVoiceover() {

        val script = scriptInput.text.toString().trim()

        if (script.isEmpty()) {
            scriptInput.error = "Enter your video script"
            return
        }

        if (script.length > 12000) {
            scriptInput.error =
                "Maximum 12000 characters"
            return
        }

        project = project.copy(
            script = script,
            updatedAt = System.currentTimeMillis(),
            voiceoverStatus = "Generating..."
        )

        store.saveProject(project)

        statusText.text =
            "Connecting to voice generation service..."

        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true
        progressText.text =
            "Generating audio on the server..."

        generateButton.text = "Generating..."
        generateButton.isEnabled = false
        playButton.visibility = View.GONE

        Thread {

            try {

                val requestBody =
                    JSONObject().apply {
                        put("project_id", project.id)
                        put("voice", selectedVoice)
                        put("model", selectedModel)
                        put("text", script)
                    }.toString()

                val connection =
                    URL(
                        "http://127.0.0.1:8765/voiceover"
                    ).openConnection()
                            as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 600000
                connection.doOutput = true

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.outputStream.use {
                    it.write(
                        requestBody.toByteArray(
                            Charsets.UTF_8
                        )
                    )
                }

                val responseCode =
                    connection.responseCode

                val stream =
                    if (responseCode in 200..299)
                        connection.inputStream
                    else
                        connection.errorStream

                val response =
                    stream
                        ?.bufferedReader()
                        ?.use { it.readText() }
                        ?: ""

                connection.disconnect()

                if (responseCode !in 200..299) {
                    throw Exception(
                        JSONObject(response)
                            .optString(
                                "error",
                                "Voiceover generation failed"
                            )
                    )
                }

                val result =
                    JSONObject(response)

                if (!result.optBoolean(
                        "ok",
                        false
                    )
                ) {
                    throw Exception(
                        result.optString(
                            "error",
                            "Generation failed"
                        )
                    )
                }

                val filename =
                    result.getString(
                        "filename"
                    )

                runOnUiThread {

                    statusText.text =
                        "Audio generated. Downloading..."

                    progressBar.isIndeterminate =
                        false

                    progressBar.progress = 0

                    progressText.text =
                        "Downloading audio... 0%"
                }

                val encodedFilename =
                    URLEncoder.encode(
                        filename,
                        "UTF-8"
                    )

                val audioConnection =
                    URL(
                        "http://127.0.0.1:8765/audio/$encodedFilename"
                    ).openConnection()
                            as HttpURLConnection

                audioConnection.requestMethod =
                    "GET"

                audioConnection.connectTimeout =
                    15000

                audioConnection.readTimeout =
                    120000

                if (audioConnection.responseCode !in 200..299) {
                    throw Exception(
                        "Generated audio could not be downloaded"
                    )
                }

                val total =
                    audioConnection
                        .contentLengthLong

                val voiceoverDir =
                    File(
                        filesDir,
                        "voiceovers"
                    )

                if (!voiceoverDir.exists()) {
                    voiceoverDir.mkdirs()
                }

                val localFile =
                    File(
                        voiceoverDir,
                        "${project.id}.mp3"
                    )

                var downloaded = 0L

                audioConnection.inputStream.use { input ->

                    FileOutputStream(
                        localFile
                    ).use { output ->

                        val buffer =
                            ByteArray(8192)

                        while (true) {

                            val count =
                                input.read(buffer)

                            if (count == -1) {
                                break
                            }

                            output.write(
                                buffer,
                                0,
                                count
                            )

                            downloaded += count

                            if (total > 0) {

                                val percent =
                                    (
                                        downloaded *
                                            100 /
                                            total
                                    ).toInt()

                                runOnUiThread {

                                    progressBar.progress =
                                        percent

                                    progressText.text =
                                        "Downloading audio... $percent%"
                                }
                            }
                        }
                    }
                }

                audioConnection.disconnect()

                project = project.copy(
                    voiceoverPath =
                        localFile.absolutePath,
                    voiceoverStatus =
                        "Generated",
                    updatedAt =
                        System.currentTimeMillis()
                )

                store.saveProject(project)

                runOnUiThread {

                    progressBar.progress = 100

                    statusText.text =
                        "Voiceover generated successfully."

                    progressText.text =
                        "Completed • ${
                            localFile.length() / 1024
                        } KB"

                    generateButton.text =
                        "Generate Again"

                    generateButton.isEnabled =
                        true

                    playButton.visibility =
                        View.VISIBLE

                    Toast.makeText(
                        this,
                        "Voiceover generated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (error: Exception) {

                project = project.copy(
                    voiceoverStatus =
                        "Generation failed",
                    updatedAt =
                        System.currentTimeMillis()
                )

                store.saveProject(project)

                runOnUiThread {

                    progressBar.visibility =
                        View.GONE

                    progressText.text = ""

                    statusText.text =
                        "Error: ${
                            error.message
                                ?: "Unknown error"
                        }"

                    generateButton.text =
                        "Try Again"

                    generateButton.isEnabled =
                        true

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

        val path =
            project.voiceoverPath

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

            mediaPlayer =
                MediaPlayer().apply {

                    setDataSource(
                        file.absolutePath
                    )

                    setOnPreparedListener {
                        start()

                        playButton.text =
                            "⏸  Playing Voiceover"
                    }

                    setOnCompletionListener {
                        playButton.text =
                            "▶  Play Voiceover"
                    }

                    prepareAsync()
                }

        } catch (error: Exception) {

            Toast.makeText(
                this,
                "Playback failed: ${
                    error.message
                }",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
