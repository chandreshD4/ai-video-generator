package com.aivideogenerator

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class VoiceSample(
    val path: String,
    val voice: String,
    val model: String,
    val createdAt: Long
)

class VoiceoverActivity : Activity() {

    private lateinit var store: ProjectStore
    private lateinit var project: VideoProject

    private lateinit var scriptInput: EditText
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var generateButton: TextView
    private lateinit var selectedVoiceText: TextView
    private lateinit var selectedModelText: TextView
    private lateinit var samplesContainer: LinearLayout

    private var selectedVoice = "nova"
    private var selectedModel = "openai/tts-1"

    private var mediaPlayer: MediaPlayer? = null
    private var playingPath: String? = null

    private val femaleVoices = listOf(
        "nova", "shimmer", "coral", "rachel", "domi",
        "bella", "elli", "charlotte", "dorothy", "sarah",
        "emily", "lily", "matilda"
    )

    private val maleVoices = listOf(
        "alloy", "echo", "fable", "onyx", "sage",
        "verse", "adam", "antoni", "arnold", "josh",
        "sam", "daniel", "charlie", "james", "fin",
        "callum", "liam", "george", "brian", "bill"
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun bg(
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

    private fun tv(
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

        store = ProjectStore(this)

        val projectId = intent.getStringExtra("project_id") ?: run {
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
            setPadding(dp(16), dp(16), dp(16), dp(12))
        }

        val back = tv("‹", 34f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
        }

        header.addView(back, LinearLayout.LayoutParams(dp(45), dp(48)))

        header.addView(
            tv("Voiceover Generator", 20f, Color.WHITE, true),
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dp(8)
            }
        )

        root.addView(header)

        val outerScroll = ScrollView(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(40))
        }

        content.addView(tv(project.name, 24f, Color.WHITE, true))

        content.addView(
            tv(
                "Create natural AI narration and compare different voices.",
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
            tv("SCRIPT", 11f, Color.rgb(150, 120, 245), true),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        scriptInput = EditText(this).apply {
            setText(project.script)
            textSize = 15f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(105, 107, 120))
            hint = "Write or paste your video script..."
            gravity = Gravity.TOP
            minLines = 12
            maxLines = 12
            setSingleLine(false)
            setHorizontallyScrolling(false)
            movementMethod = ScrollingMovementMethod()
            isVerticalScrollBarEnabled = true
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = bg(
                Color.rgb(23, 24, 32),
                16,
                Color.rgb(43, 44, 56)
            )

            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE ->
                        view.parent?.requestDisallowInterceptTouchEvent(true)

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL ->
                        view.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
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
            tv("GENERATED AUDIO", 11f, Color.rgb(150, 120, 245), true),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        samplesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(samplesContainer)

        renderSamples()

        content.addView(
            tv("VOICE", 11f, Color.rgb(150, 120, 245), true),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )

        val voiceCard = selectionCard(
            "🎙",
            "Voice",
            selectedVoiceDisplay()
        )

        selectedVoiceText =
            voiceCard.findViewWithTag("value") as TextView

        voiceCard.setOnClickListener {
            showVoiceSelector()
        }

        content.addView(
            voiceCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(72)
            ).apply {
                topMargin = dp(8)
            }
        )

        content.addView(
            tv("VOICE MODEL", 11f, Color.rgb(150, 120, 245), true),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )

        val modelCard = selectionCard(
            "⚙",
            "Voice Model",
            modelDisplay()
        )

        selectedModelText =
            modelCard.findViewWithTag("value") as TextView

        modelCard.setOnClickListener {
            showModelSelector()
        }

        content.addView(
            modelCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(72)
            ).apply {
                topMargin = dp(8)
            }
        )

        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = bg(Color.rgb(20, 21, 28), 15)
        }

        statusCard.addView(
            tv("Generation Status", 14f, Color.WHITE, true)
        )

        statusText = tv(
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

        progressText = tv("", 12f, Color.rgb(145, 147, 160))

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

        generateButton = tv(
            "Generate Voiceover",
            16f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = bg(Color.rgb(105, 70, 205), 16)
            setOnClickListener { generateVoiceover() }
        }

        content.addView(
            generateButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
            ).apply {
                topMargin = dp(20)
            }
        )

        content.addView(
            tv(
                "API key stays outside the APK. Voice generation is handled through the secure testing bridge.",
                12f,
                Color.rgb(115, 117, 130)
            ).apply {
                setPadding(dp(4), dp(14), dp(4), 0)
            }
        )

        outerScroll.addView(content)

        root.addView(
            outerScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun selectionCard(
        icon: String,
        title: String,
        value: String
    ): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = bg(
                Color.rgb(20, 21, 29),
                15,
                Color.rgb(48, 49, 62)
            )
        }

        card.addView(
            tv(icon, 23f, Color.WHITE).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(dp(44), dp(48))
        )

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        labels.addView(tv(title, 12f, Color.rgb(145, 147, 160)))

        val valueView = tv(value, 15f, Color.WHITE, true)
        valueView.tag = "value"

        labels.addView(
            valueView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(3)
            }
        )

        card.addView(
            labels,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                leftMargin = dp(12)
            }
        )

        card.addView(
            tv("›", 28f, Color.rgb(145, 147, 160)).apply {
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(dp(35), dp(48))
        )

        return card
    }

    private fun selectedVoiceDisplay(): String =
        selectedVoice.replaceFirstChar { it.uppercase() }

    private fun modelDisplay(): String =
        if (selectedModel.endsWith("-hd")) {
            "HD • Higher quality"
        } else {
            "Standard • TTS-1"
        }

    private fun showVoiceSelector() {

        val all = ArrayList<String>()
        all.addAll(femaleVoices)
        all.addAll(maleVoices)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(4), dp(10), dp(4))
        }

        val scroll = ScrollView(this)
        scroll.addView(container)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Choose Voice")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .create()

        all.forEach { voice ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(4), dp(8), dp(4))
            }

            val radio = RadioButton(this).apply {
                isChecked = voice == selectedVoice
            }

            val category =
                if (femaleVoices.contains(voice))
                    "Female"
                else
                    "Male"

            val labels = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
            }

            labels.addView(
                tv(
                    voice.replaceFirstChar { it.uppercase() },
                    15f,
                    Color.WHITE,
                    true
                )
            )

            labels.addView(
                tv(
                    category,
                    11f,
                    Color.rgb(145, 147, 160)
                )
            )

            row.addView(
                radio,
                LinearLayout.LayoutParams(dp(48), dp(56))
            )

            row.addView(
                labels,
                LinearLayout.LayoutParams(
                    0,
                    dp(56),
                    1f
                )
            )

            val choose = {
                selectedVoice = voice
                selectedVoiceText.text = selectedVoiceDisplay()
                dialog.dismiss()
            }

            radio.setOnClickListener { choose() }
            row.setOnClickListener { choose() }

            container.addView(row)
        }

        dialog.show()
    }

    private fun showModelSelector() {

        val choices = arrayOf(
            "openai/tts-1",
            "openai/tts-1-hd"
        )

        val descriptions = arrayOf(
            "Standard quality",
            "Higher quality"
        )

        val checked =
            if (selectedModel == choices[1]) 1 else 0

        AlertDialog.Builder(this)
            .setTitle("Choose Voice Model")
            .setSingleChoiceItems(
                choices.mapIndexed { index, value ->
                    "${value}  •  ${descriptions[index]}"
                }.toTypedArray(),
                checked
            ) { dialog, which ->
                selectedModel = choices[which]
                selectedModelText.text = modelDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun historyPrefs() =
        getSharedPreferences(
            "voiceover_samples",
            MODE_PRIVATE
        )

    private fun getSamples(): MutableList<VoiceSample> {

        val result = mutableListOf<VoiceSample>()

        val raw = historyPrefs().getString(
            project.id,
            "[]"
        ) ?: "[]"

        try {
            val array = JSONArray(raw)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                result.add(
                    VoiceSample(
                        item.getString("path"),
                        item.getString("voice"),
                        item.getString("model"),
                        item.getLong("createdAt")
                    )
                )
            }
        } catch (_: Exception) {
        }

        return result
    }

    private fun saveSample(sample: VoiceSample) {

        val samples = getSamples()
        samples.add(0, sample)

        val array = JSONArray()

        samples.forEach {
            array.put(
                JSONObject().apply {
                    put("path", it.path)
                    put("voice", it.voice)
                    put("model", it.model)
                    put("createdAt", it.createdAt)
                }
            )
        }

        historyPrefs()
            .edit()
            .putString(project.id, array.toString())
            .apply()
    }

    private fun renderSamples() {

        samplesContainer.removeAllViews()

        val samples = getSamples()

        if (samples.isEmpty() &&
            project.voiceoverPath.isNotBlank()
        ) {
            samples.add(
                VoiceSample(
                    project.voiceoverPath,
                    selectedVoice,
                    selectedModel,
                    project.updatedAt
                )
            )
        }

        if (samples.isEmpty()) {

            val empty = tv(
                "Generated voice samples will appear here.",
                13f,
                Color.rgb(120, 122, 135)
            )

            empty.setPadding(
                dp(14),
                dp(15),
                dp(14),
                dp(15)
            )

            samplesContainer.addView(
                empty,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(58)
                )
            )

            return
        }

        samples.forEach { sample ->
            addSampleCard(sample)
        }
    }

    private fun addSampleCard(sample: VoiceSample) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = bg(
                Color.rgb(20, 21, 29),
                15,
                Color.rgb(45, 46, 59)
            )
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val voice = tv(
            sample.voice.replaceFirstChar { it.uppercase() },
            16f,
            Color.WHITE,
            true
        )

        top.addView(
            voice,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val modelShort =
            if (sample.model.endsWith("-hd"))
                "TTS-1 HD"
            else
                "TTS-1"

        top.addView(
            tv(
                modelShort,
                11f,
                Color.rgb(160, 140, 235),
                true
            )
        )

        card.addView(top)

        card.addView(
            tv(
                sample.model,
                11f,
                Color.rgb(125, 127, 140)
            ),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(3)
            }
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val play = tv(
            if (playingPath == sample.path)
                "⏸  Pause"
            else
                "▶  Play",
            14f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = bg(Color.rgb(45, 46, 58), 12)
            setOnClickListener {
                playSample(sample)
            }
        }

        actions.addView(
            play,
            LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            ).apply {
                rightMargin = dp(8)
                topMargin = dp(12)
            }
        )

        val download = tv(
            "↓  Download",
            14f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = bg(Color.rgb(105, 70, 205), 12)
            setOnClickListener {
                downloadSample(sample)
            }
        }

        actions.addView(
            download,
            LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            ).apply {
                topMargin = dp(12)
            }
        )

        card.addView(actions)

        samplesContainer.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(9)
            }
        )
    }

    private fun playSample(sample: VoiceSample) {

        val file = File(sample.path)

        if (!file.exists()) {
            Toast.makeText(
                this,
                "Audio file not found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (playingPath == sample.path) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.start()
                }
            }

            renderSamples()
            return
        }

        mediaPlayer?.release()

        try {

            mediaPlayer = MediaPlayer().apply {

                setDataSource(file.absolutePath)

                setOnPreparedListener {
                    start()
                    playingPath = sample.path
                    renderSamples()
                }

                setOnCompletionListener {
                    playingPath = null
                    renderSamples()
                }

                prepareAsync()
            }

        } catch (error: Exception) {

            playingPath = null

            Toast.makeText(
                this,
                "Playback failed: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun downloadSample(sample: VoiceSample) {

        val file = File(sample.path)

        if (!file.exists()) {
            Toast.makeText(
                this,
                "Audio file not found",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val name =
            "voice_${sample.voice}_${System.currentTimeMillis()}.mp3"

        val intent = Intent(
            Intent.ACTION_CREATE_DOCUMENT
        ).apply {
            type = "audio/mpeg"
            putExtra(
                Intent.EXTRA_TITLE,
                name
            )
        }

        pendingDownloadFile = file
        startActivityForResult(
            intent,
            REQUEST_SAVE_AUDIO
        )
    }

    private var pendingDownloadFile: File? = null

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode != REQUEST_SAVE_AUDIO ||
            resultCode != RESULT_OK
        ) {
            return
        }

        val source = pendingDownloadFile ?: return
        val uri = data?.data ?: return

        try {

            contentResolver.openOutputStream(uri)
                ?.use { output ->

                    source.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }

            Toast.makeText(
                this,
                "Audio saved successfully",
                Toast.LENGTH_SHORT
            ).show()

        } catch (error: Exception) {

            Toast.makeText(
                this,
                "Download failed: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
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

                val result = JSONObject(response)

                if (!result.optBoolean("ok", false)) {
                    throw Exception(
                        result.optString(
                            "error",
                            "Generation failed"
                        )
                    )
                }

                val filename =
                    result.getString("filename")

                runOnUiThread {
                    statusText.text =
                        "Audio generated. Downloading..."

                    progressBar.isIndeterminate = false
                    progressBar.progress = 0

                    progressText.text =
                        "Downloading audio... 0%"
                }

                val encodedFilename =
                    URLEncoder.encode(filename, "UTF-8")

                val audioConnection =
                    URL(
                        "http://127.0.0.1:8765/audio/$encodedFilename"
                    ).openConnection()
                            as HttpURLConnection

                audioConnection.requestMethod = "GET"
                audioConnection.connectTimeout = 15000
                audioConnection.readTimeout = 120000

                if (audioConnection.responseCode !in 200..299) {
                    throw Exception(
                        "Generated audio could not be downloaded"
                    )
                }

                val total =
                    audioConnection.contentLengthLong

                val voiceoverDir =
                    File(filesDir, "voiceovers")

                if (!voiceoverDir.exists()) {
                    voiceoverDir.mkdirs()
                }

                val localFile =
                    File(
                        voiceoverDir,
                        "${project.id}_${System.currentTimeMillis()}.mp3"
                    )

                var downloaded = 0L

                audioConnection.inputStream.use { input ->

                    FileOutputStream(localFile).use { output ->

                        val buffer = ByteArray(8192)

                        while (true) {

                            val count = input.read(buffer)

                            if (count == -1) break

                            output.write(
                                buffer,
                                0,
                                count
                            )

                            downloaded += count

                            if (total > 0) {

                                val percent =
                                    (
                                        downloaded * 100 / total
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

                val sample = VoiceSample(
                    localFile.absolutePath,
                    selectedVoice,
                    selectedModel,
                    System.currentTimeMillis()
                )

                saveSample(sample)

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
                        "Completed • ${localFile.length() / 1024} KB"

                    generateButton.text =
                        "Generate Another Voice"

                    generateButton.isEnabled = true

                    renderSamples()

                    Toast.makeText(
                        this,
                        "New voice sample added",
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

                    progressBar.visibility = View.GONE
                    progressText.text = ""

                    statusText.text =
                        "Error: ${error.message ?: "Unknown error"}"

                    generateButton.text = "Try Again"
                    generateButton.isEnabled = true
                }
            }
        }.start()
    }

    companion object {
        private const val REQUEST_SAVE_AUDIO = 5001
    }
}
