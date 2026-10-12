package com.aivideogenerator

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SceneImageGenerator {

    private const val SERVER_URL = "http://127.0.0.1:8766"
    private const val MAX_IMAGE_BYTES = 20 * 1024 * 1024

    private val executor: ExecutorService =
        Executors.newSingleThreadExecutor()

    private val mainHandler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onSuccess(imagePath: String)
        fun onError(message: String)
    }

    fun generate(
        context: Context,
        prompt: String,
        callback: Callback
    ) {
        val appContext = context.applicationContext

        if (prompt.isBlank()) {
            callback.onError("Image prompt is empty")
            return
        }

        executor.execute {
            try {
                val requestBody = JSONObject()
                    .put("prompt", prompt)
                    .toString()
                    .toByteArray(Charsets.UTF_8)

                val connection = URL("$SERVER_URL/generate")
                    .openConnection() as HttpURLConnection

                val result = try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 15000
                    connection.readTimeout = 210000
                    connection.doOutput = true
                    connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=utf-8"
                    )
                    connection.setRequestProperty(
                        "Accept",
                        "application/json"
                    )

                    connection.outputStream.use {
                        it.write(requestBody)
                    }

                    val status = connection.responseCode
                    val stream = if (status in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                    val responseText = stream?.bufferedReader()?.use {
                        it.readText()
                    }.orEmpty()

                    if (status !in 200..299) {
                        val message = try {
                            JSONObject(responseText)
                                .optString("error", "Image generation failed")
                        } catch (_: Exception) {
                            "Image generation failed (HTTP $status)"
                        }
                        throw IllegalStateException(message)
                    }

                    JSONObject(responseText)
                } finally {
                    connection.disconnect()
                }

                val filename = result.optString("filename")
                if (!filename.matches(
                        Regex("[a-f0-9]{32}\\.(png|jpg|webp)")
                    )
                ) {
                    throw IllegalStateException(
                        "Image server returned an invalid filename"
                    )
                }

                val imageUrl = "$SERVER_URL/image/$filename"
                val imageConnection =
                    URL(imageUrl).openConnection() as HttpURLConnection

                val savedPath = try {
                    imageConnection.requestMethod = "GET"
                    imageConnection.connectTimeout = 15000
                    imageConnection.readTimeout = 60000

                    if (imageConnection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IllegalStateException("Could not download generated image")
                    }

                    val contentType = imageConnection.contentType.orEmpty()
                    if (!contentType.startsWith("image/")) {
                        throw IllegalStateException("Server did not return an image")
                    }

                    val imageDirectory = File(appContext.filesDir, "scenes")
                    if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                        throw IllegalStateException("Could not create image folder")
                    }

                    val destination = File(imageDirectory, filename)
                    var totalBytes = 0

                    imageConnection.inputStream.use { input ->
                        FileOutputStream(destination).use { output ->
                            val buffer = ByteArray(8192)
                            while (true) {
                                val count = input.read(buffer)
                                if (count < 0) break

                                totalBytes += count
                                if (totalBytes > MAX_IMAGE_BYTES) {
                                    throw IllegalStateException("Generated image is too large")
                                }

                                output.write(buffer, 0, count)
                            }
                        }
                    }

                    if (totalBytes == 0) {
                        destination.delete()
                        throw IllegalStateException("Downloaded image is empty")
                    }

                    destination.absolutePath
                } finally {
                    imageConnection.disconnect()
                }

                mainHandler.post {
                    callback.onSuccess(savedPath)
                }
            } catch (e: Exception) {
                val message = e.message ?: "Image generation failed"
                mainHandler.post {
                    callback.onError(message)
                }
            }
        }
    }
}
