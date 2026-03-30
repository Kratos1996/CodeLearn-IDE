package com.codelearn.ide.model

import androidx.compose.ui.graphics.Color

// ─── Language Enum ────────────────────────────────────────────────────────────
enum class Language(
    val displayName: String,
    val fileExtension: String,
    val icon: String,
    val monoColor: Color
) {
    KOTLIN("Kotlin",        "kt",   "🟣", Color(0xFF7F52FF)),
    JAVA("Java",            "java", "☕", Color(0xFFED8B00)),
    PYTHON("Python",        "py",   "🐍", Color(0xFF3572A5)),
    JAVASCRIPT("JavaScript","js",   "🟨", Color(0xFFF7DF1E)),
    CSHARP("C#",            "cs",   "💜", Color(0xFF9B4F96)),
    CPP("C++",              "cpp",  "🔵", Color(0xFF004488)),
    C("C",                  "c",    "🔷", Color(0xFF1C6BB0)),
    RUBY("Ruby",            "rb",   "🔴", Color(0xFFCC342D)),
    DART("Dart",            "dart", "🎯", Color(0xFF00B4AB)),
    VB("Visual Basic",      "vb",   "🟦", Color(0xFF5C2D91));

    companion object {
        fun fromExtension(ext: String): Language =
            values().firstOrNull { it.fileExtension == ext } ?: KOTLIN
    }
}

// ─── File Model ───────────────────────────────────────────────────────────────
data class CodeFile(
    val id: String,
    val name: String,
    val language: Language,
    var content: String = "",
    var isModified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Project Model ────────────────────────────────────────────────────────────
data class Project(
    val id: String,
    val name: String,
    val language: Language,
    val files: MutableList<CodeFile> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    var description: String = ""
)

// ─── Breakpoint ───────────────────────────────────────────────────────────────
data class Breakpoint(
    val fileId: String,
    val line: Int,
    val isEnabled: Boolean = true
)

// ─── Debug State ──────────────────────────────────────────────────────────────
data class DebugSession(
    val isActive: Boolean = false,
    val currentLine: Int = -1,
    val variables: Map<String, String> = emptyMap(),
    val callStack: List<String> = emptyList(),
    val output: List<String> = emptyList(),
    val breakpoints: List<Breakpoint> = emptyList()
)

// ─── Git State ────────────────────────────────────────────────────────────────
data class GitStatus(
    val branch: String = "main",
    val stagedFiles: List<String> = emptyList(),
    val modifiedFiles: List<String> = emptyList(),
    val untrackedFiles: List<String> = emptyList(),
    val commits: List<GitCommit> = emptyList(),
    val isInitialized: Boolean = false
)

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val timestamp: Long
)

// ─── Autocomplete Suggestion ──────────────────────────────────────────────────
data class AutocompleteSuggestion(
    val text: String,
    val type: SuggestionType,
    val description: String = "",
    val insertText: String = text
)

enum class SuggestionType {
    KEYWORD, FUNCTION, CLASS, VARIABLE, SNIPPET, BUILTIN
}

// ─── AI Hint ──────────────────────────────────────────────────────────────────
data class AiHint(
    val title: String,
    val message: String,
    val codeExample: String = "",
    val hintType: HintType = HintType.SUGGESTION
)

enum class HintType { ERROR, WARNING, SUGGESTION, INFO }

// ─── Theme ────────────────────────────────────────────────────────────────────
enum class IDETheme { DARK, LIGHT, MONOKAI, DRACULA, GITHUB }
