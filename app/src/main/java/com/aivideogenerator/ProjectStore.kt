package com.aivideogenerator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class VideoProject(
    val id: String,
    val name: String,
    val script: String,
    val createdAt: Long,
    val updatedAt: Long,
    val voiceoverPath: String = "",
    val voiceoverStatus: String = "Not generated"
)

class ProjectStore(context: Context) {

    private val prefs = context.getSharedPreferences(
        "ai_video_projects",
        Context.MODE_PRIVATE
    )

    fun getProjects(): MutableList<VideoProject> {
        val result = mutableListOf<VideoProject>()
        val raw = prefs.getString("projects", "[]") ?: "[]"

        return try {
            val array = JSONArray(raw)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                result.add(
                    VideoProject(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        script = item.getString("script"),
                        createdAt = item.getLong("createdAt"),
                        updatedAt = item.getLong("updatedAt"),
                        voiceoverPath = item.optString("voiceoverPath", ""),
                        voiceoverStatus = item.optString(
                            "voiceoverStatus",
                            "Not generated"
                        )
                    )
                )
            }

            result
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveProject(project: VideoProject) {
        val projects = getProjects()

        val index = projects.indexOfFirst {
            it.id == project.id
        }

        if (index >= 0) {
            projects[index] = project
        } else {
            projects.add(0, project)
        }

        writeProjects(projects)
    }

    fun deleteProject(id: String) {
        val projects = getProjects()
        projects.removeAll { it.id == id }
        writeProjects(projects)
    }

    fun getProject(id: String): VideoProject? {
        return getProjects().firstOrNull {
            it.id == id
        }
    }

    private fun writeProjects(projects: List<VideoProject>) {
        val array = JSONArray()

        projects.forEach { project ->
            val item = JSONObject()

            item.put("id", project.id)
            item.put("name", project.name)
            item.put("script", project.script)
            item.put("createdAt", project.createdAt)
            item.put("updatedAt", project.updatedAt)
            item.put("voiceoverPath", project.voiceoverPath)
            item.put("voiceoverStatus", project.voiceoverStatus)

            array.put(item)
        }

        prefs.edit()
            .putString("projects", array.toString())
            .apply()
    }
}
