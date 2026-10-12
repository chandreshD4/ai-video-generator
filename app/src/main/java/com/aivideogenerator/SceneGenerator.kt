package com.aivideogenerator

import java.util.UUID

object SceneGenerator {

    fun generateScenes(
        script: String,
        maxWordsPerScene: Int = 45
    ): List<VideoScene> {

        val cleanScript = script.trim()

        if (cleanScript.isEmpty()) {
            return emptyList()
        }

        val safeWordLimit = maxWordsPerScene.coerceAtLeast(10)

        val paragraphs = cleanScript
            .split(Regex("\\n\\s*\\n+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val sentences = mutableListOf<String>()

        for (paragraph in paragraphs) {
            val parts = paragraph
                .split(Regex("(?<=[.!?।])\\s+"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            sentences.addAll(parts)
        }

        val sceneTexts = mutableListOf<String>()
        var currentScene = mutableListOf<String>()
        var currentWordCount = 0

        for (sentence in sentences) {
            val wordCount = sentence
                .split(Regex("\\s+"))
                .count { it.isNotBlank() }

            if (
                currentScene.isNotEmpty() &&
                currentWordCount + wordCount > safeWordLimit
            ) {
                sceneTexts.add(currentScene.joinToString(" "))
                currentScene = mutableListOf()
                currentWordCount = 0
            }

            currentScene.add(sentence)
            currentWordCount += wordCount
        }

        if (currentScene.isNotEmpty()) {
            sceneTexts.add(currentScene.joinToString(" "))
        }

        return sceneTexts.mapIndexed { index, description ->
            val titleText = description
                .replace(Regex("\\s+"), " ")
                .take(55)
                .trim()

            VideoScene(
                id = UUID.randomUUID().toString(),
                sceneNumber = index + 1,
                title = "Scene ${index + 1}: $titleText",
                description = description,
                imagePrompt = "Create a cinematic visual scene illustrating: $description",
                status = "Pending"
            )
        }
    }
}
