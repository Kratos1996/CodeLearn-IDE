package com.codelearn.ide.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// ─── Serializable data models for persistence ─────────────────────────────────

@Serializable
data class SavedFile(
    val id: String,
    val name: String,
    val languageCode: String,
    val content: String,
    val isModified: Boolean = false,
    val createdAt: Long = 0L
)

@Serializable
data class SavedProject(
    val id: String,
    val name: String,
    val languageCode: String,
    val files: List<SavedFile>,
    val description: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class AppState(
    val projects: List<SavedProject> = emptyList(),
    val solvedProblems: List<String> = emptyList(),
    val lastOpenProjectId: String? = null,
    val lastOpenFileId: String? = null,
    val quizCodeByProblemAndLang: Map<String, String> = emptyMap(),
    val selectedTheme: String = "DARK",
)

// ─── Database singleton ────────────────────────────────────────────────────────

object AppDatabase {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dbFile: File by lazy {
        val userHome = System.getProperty("user.home") ?: "."
        val dir = File(userHome, ".codelearnide")
        dir.mkdirs()
        File(dir, "appstate.json")
    }

    fun load(): AppState {
        return try {
            if (dbFile.exists()) {
                json.decodeFromString<AppState>(dbFile.readText())
            } else {
                AppState()
            }
        } catch (e: Exception) {
            AppState()
        }
    }

    fun save(state: AppState) {
        try {
            dbFile.writeText(json.encodeToString(state))
        } catch (_: Exception) {}
    }

    fun saveField(update: (AppState) -> AppState) {
        val current = load()
        save(update(current))
    }
}
