package com.codelearn.ide.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.codelearn.ide.ai.CodeIntelligence
import com.codelearn.ide.ai.AiErrorAnalysis
import com.codelearn.ide.ai.AiCodeReview
import com.codelearn.ide.ai.IssueSeverity
import com.codelearn.ide.export.FileExporter
import com.codelearn.ide.data.AppDatabase
import com.codelearn.ide.data.SavedFile
import com.codelearn.ide.data.SavedProject
import com.codelearn.ide.git.GitService
import com.codelearn.ide.interpreter.Judge0Engine
import com.codelearn.ide.interpreter.QuizEvaluator
import com.codelearn.ide.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// ─── Console ──────────────────────────────────────────────────────────────────
enum class ConsoleType { NORMAL, SUCCESS, ERROR, WARNING, INFO, MUTED, DEBUG, DEBUG_VAR }
data class ConsoleEntry(val text: String, val type: ConsoleType = ConsoleType.NORMAL)

// ─── Save Prompt state (drives BottomSheet) ───────────────────────────────────
data class SavePromptState(
    val visible: Boolean = false,
    val fileName: String = "",
    val onSave: () -> Unit = {},
    val onDiscard: () -> Unit = {},
    val onCancel: () -> Unit = {}
)

// ─── Language switch dialog state ─────────────────────────────────────────────
data class LangSwitchState(
    val visible: Boolean = false,
    val targetLanguage: Language? = null
)

// ─── CompositionLocal so any composable can reach the ViewModel ───────────────
val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("No AppViewModel provided — wrap root with AppViewModelProvider")
}

// ─── AppViewModel (Voyager ScreenModel) ───────────────────────────────────────
// Navigation is handled by Voyager Navigator; this holds all app state.
class AppViewModel : ScreenModel {

    val gitService = GitService()

    // ── Save Prompt ─────────────────────────────────────────────────────────
    private val _savePrompt = MutableStateFlow(SavePromptState())
    val savePrompt: StateFlow<SavePromptState> = _savePrompt

    // ── Language switch dialog ───────────────────────────────────────────────
    private val _langSwitch = MutableStateFlow(LangSwitchState())
    val langSwitch: StateFlow<LangSwitchState> = _langSwitch

    // ── Projects ────────────────────────────────────────────────────────────
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject

    private val _currentFile = MutableStateFlow<CodeFile?>(null)
    val currentFile: StateFlow<CodeFile?> = _currentFile

    private val _openFiles = MutableStateFlow<List<CodeFile>>(emptyList())
    val openFiles: StateFlow<List<CodeFile>> = _openFiles

    // ── Editor ──────────────────────────────────────────────────────────────
    private val _selectedLanguage = MutableStateFlow(Language.KOTLIN)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage

    private val _currentCode = MutableStateFlow("")
    val currentCode: StateFlow<String> = _currentCode

    private val _cursorPosition = MutableStateFlow(0)
    val cursorPosition: StateFlow<Int> = _cursorPosition

    private val _suggestions = MutableStateFlow<List<AutocompleteSuggestion>>(emptyList())
    val suggestions: StateFlow<List<AutocompleteSuggestion>> = _suggestions

    private val _showSuggestions = MutableStateFlow(false)
    val showSuggestions: StateFlow<Boolean> = _showSuggestions

    // ── Debug ───────────────────────────────────────────────────────────────
    private val _debugSession = MutableStateFlow(DebugSession())
    val debugSession: StateFlow<DebugSession> = _debugSession

    private val _breakpoints = MutableStateFlow<Set<Int>>(emptySet())
    val breakpoints: StateFlow<Set<Int>> = _breakpoints

    // ── AI ──────────────────────────────────────────────────────────────────
    // ── AI State ────────────────────────────────────────────────────────────
    private val _aiHints = MutableStateFlow<List<AiHint>>(emptyList())
    val aiHints: StateFlow<List<AiHint>> = _aiHints

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing

    private val _errorAnalysis = MutableStateFlow<AiErrorAnalysis?>(null)
    val errorAnalysis: StateFlow<AiErrorAnalysis?> = _errorAnalysis

    private val _inlineErrors = MutableStateFlow<List<com.codelearn.ide.ai.InlineError>>(emptyList())
    val inlineErrors: StateFlow<List<com.codelearn.ide.ai.InlineError>> = _inlineErrors

    private val _codeReview = MutableStateFlow<AiCodeReview?>(null)
    val codeReview: StateFlow<AiCodeReview?> = _codeReview

    private val _aiCompletions = MutableStateFlow<List<com.codelearn.ide.ai.AiCompletion>>(emptyList())
    val aiCompletions: StateFlow<List<com.codelearn.ide.ai.AiCompletion>> = _aiCompletions

    private val _isFixingCode = MutableStateFlow(false)
    val isFixingCode: StateFlow<Boolean> = _isFixingCode

    private val _lastError = MutableStateFlow("")
    val lastError: StateFlow<String> = _lastError

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult

    // ── AI toggle — can be turned OFF (e.g. during quiz) ────────────────────
    private val _aiEnabled = MutableStateFlow(true)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled

    // ── Share link state ─────────────────────────────────────────────────────
    private val _shareLink = MutableStateFlow<String?>(null)
    val shareLink: StateFlow<String?> = _shareLink


    // ── Console ─────────────────────────────────────────────────────────────
    private val _consoleOutput = MutableStateFlow<List<ConsoleEntry>>(emptyList())
    val consoleOutput: StateFlow<List<ConsoleEntry>> = _consoleOutput

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    // ── Quiz ────────────────────────────────────────────────────────────────
    private val _selectedProblem = MutableStateFlow<QuizProblem?>(null)
    val selectedProblem: StateFlow<QuizProblem?> = _selectedProblem

    private val _quizCode = MutableStateFlow("")
    val quizCode: StateFlow<String> = _quizCode

    private val _submissionResult = MutableStateFlow<SubmissionResult?>(null)
    val submissionResult: StateFlow<SubmissionResult?> = _submissionResult

    private val _quizTestResults = MutableStateFlow<List<QuizEvaluator.TestCaseResult>>(emptyList())
    val quizTestResults: StateFlow<List<QuizEvaluator.TestCaseResult>> = _quizTestResults

    private val _solvedProblems = MutableStateFlow<Set<String>>(emptySet())
    val solvedProblems: StateFlow<Set<String>> = _solvedProblems

    private val _theme = MutableStateFlow(IDETheme.DARK)
    val theme: StateFlow<IDETheme> = _theme

    // ── Splash ready signal (Voyager SplashScreen polls this) ────────────────
    private val _splashReady = MutableStateFlow(false)
    val splashReady: StateFlow<Boolean> = _splashReady

    // ────────────────────────────────────────────────────────────────────────
    // INIT
    // ────────────────────────────────────────────────────────────────────────
    init {
        screenModelScope.launch {
            loadFromDatabase()
            delay(2000)
            _splashReady.value = true
        }
        // Real-time inline error detection (fast — 600ms debounce)
        screenModelScope.launch {
            _currentCode.debounce(600).collect { code ->
                if (_aiEnabled.value) {
                    val errors = CodeIntelligence.detectInlineErrors(code, _selectedLanguage.value)
                    _inlineErrors.value = errors
                }
            }
        }

        // AI code review on pause (debounced 2s after typing stops)
        screenModelScope.launch {
            _currentCode.debounce(2000).collect { code ->
                if (code.length > 15 && _aiEnabled.value) {
                    screenModelScope.launch {
                        val review = CodeIntelligence.reviewCode(code, _selectedLanguage.value)
                        _codeReview.value = review
                        // Convert review issues to AiHints for backwards compat
                        _aiHints.value = review.issues.map { issue ->
                            AiHint(
                                title = "Line ${issue.line}: ${issue.message}",
                                message = issue.suggestion,
                                hintType = when (issue.severity) {
                                    IssueSeverity.ERROR -> HintType.ERROR
                                    IssueSeverity.WARNING -> HintType.WARNING
                                    IssueSeverity.INFO -> HintType.INFO
                                }
                            )
                        }
                    }
                }
            }
        }
        // AI-powered autocomplete on cursor move
        screenModelScope.launch {
            _cursorPosition.debounce(600).collect { pos ->
                val code = _currentCode.value
                if (code.isNotBlank() && pos > 3 && _aiEnabled.value) {
                    val codeBeforeCursor = code.take(pos)
                    val completions = CodeIntelligence.getCompletions(codeBeforeCursor, _selectedLanguage.value)
                    _aiCompletions.value = completions
                    // Also map to old suggestions for existing UI
                    _suggestions.value = completions.map { c ->
                        com.codelearn.ide.model.AutocompleteSuggestion(
                            text = c.suggestion,
                            type = com.codelearn.ide.model.SuggestionType.FUNCTION,
                            description = c.explanation
                        )
                    }
                    _showSuggestions.value = completions.isNotEmpty()
                }
            }
        }
        // Autosave code every 8s while editing
        screenModelScope.launch {
            _currentCode.debounce(8000).collect { persistAndSave() }
        }
        // Autosave quiz code every 5s
        screenModelScope.launch {
            _quizCode.debounce(5000).collect { saveQuizCode() }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // DATABASE
    // ────────────────────────────────────────────────────────────────────────
    private fun loadFromDatabase() {
        val state = AppDatabase.load()
        _solvedProblems.value = state.solvedProblems.toSet()
        _theme.value = try { IDETheme.valueOf(state.selectedTheme) } catch (_: Exception) { IDETheme.DARK }

        if (state.projects.isNotEmpty()) {
            val projects = state.projects.map { it.toProject() }
            _projects.value = projects
            val proj = projects.firstOrNull { it.id == state.lastOpenProjectId } ?: projects.first()
            _currentProject.value = proj
            gitService.initRepository(proj.name)
            val file = proj.files.firstOrNull { it.id == state.lastOpenFileId } ?: proj.files.firstOrNull()
            if (file != null) {
                _currentFile.value = file
                _currentCode.value = file.content
                _selectedLanguage.value = file.language
                _openFiles.value = listOf(file)
            }
        } else {
            createDefaultProject()
        }
    }

    fun persistAndSave() {
        val file = _currentFile.value ?: return
        val saved = file.copy(content = _currentCode.value, isModified = false)
        _currentFile.value = saved
        updateFileInProject(saved)
        _openFiles.value = _openFiles.value.map { if (it.id == saved.id) saved else it }
        screenModelScope.launch(Dispatchers.IO) { writeState() }
    }

    private fun writeState() {
        AppDatabase.save(
            AppDatabase.load().copy(
                projects = _projects.value.map { it.toSaved() },
                solvedProblems = _solvedProblems.value.toList(),
                lastOpenProjectId = _currentProject.value?.id,
                lastOpenFileId = _currentFile.value?.id,
                selectedTheme = _theme.value.name,
            )
        )
    }

    private fun saveQuizCode() {
        val problem = _selectedProblem.value ?: return
        val key = "${problem.id}::${_selectedLanguage.value.fileExtension}"
        screenModelScope.launch(Dispatchers.IO) {
            AppDatabase.saveField {
                it.copy(quizCodeByProblemAndLang = it.quizCodeByProblemAndLang + (key to _quizCode.value))
            }
        }
    }

    fun onAppPause() { persistAndSave() }

    // ────────────────────────────────────────────────────────────────────────
    // SAVE PROMPT (BottomSheet shown over any screen)
    // ────────────────────────────────────────────────────────────────────────
    fun showSavePrompt(fileName: String, onSave: () -> Unit, onDiscard: () -> Unit, onCancel: () -> Unit) {
        _savePrompt.value = SavePromptState(true, fileName, onSave, onDiscard, onCancel)
    }

    fun dismissSavePrompt() { _savePrompt.value = SavePromptState(visible = false) }

    /** Call before navigating away from IDE — Voyager navigator.pop() is passed as onClear */
    fun checkUnsavedBeforeAction(onClear: () -> Unit, onCancel: () -> Unit) {
        if (hasUnsavedChanges()) {
            showSavePrompt(
                fileName = _currentFile.value?.name ?: "file",
                onSave = { persistAndSave(); dismissSavePrompt(); onClear() },
                onDiscard = { dismissSavePrompt(); onClear() },
                onCancel = { dismissSavePrompt(); onCancel() }
            )
        } else {
            onClear()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val f = _currentFile.value ?: return false
        return f.isModified && _currentCode.value != f.content
    }

    // ────────────────────────────────────────────────────────────────────────
    // LANGUAGE SWITCHING
    // ────────────────────────────────────────────────────────────────────────
    fun requestLanguageChange(lang: Language) {
        if (lang == _selectedLanguage.value) return
        val starter = getStarterTemplate(_selectedLanguage.value)
        val isModified = _currentCode.value.filter { !it.isWhitespace() } != starter.filter { !it.isWhitespace() }
        if (isModified) {
            _langSwitch.value = LangSwitchState(visible = true, targetLanguage = lang)
        } else {
            applyLanguageSwitch(lang)
        }
    }

    fun confirmLangSwitch(saveFirst: Boolean) {
        val target = _langSwitch.value.targetLanguage ?: return
        _langSwitch.value = LangSwitchState(visible = false)
        if (saveFirst) persistAndSave()
        applyLanguageSwitch(target)
    }

    fun cancelLangSwitch() { _langSwitch.value = LangSwitchState(visible = false) }

    private fun applyLanguageSwitch(lang: Language) {
        _selectedLanguage.value = lang
        val newContent = getStarterTemplate(lang)
        _currentFile.value?.let { file ->
            val baseName = file.name.substringBeforeLast('.')
            val updated = file.copy(name = "$baseName.${lang.fileExtension}", language = lang, content = newContent, isModified = false)
            _currentFile.value = updated
            updateFileInProject(updated)
            _openFiles.value = _openFiles.value.map { if (it.id == updated.id) updated else it }
        }
        _currentCode.value = newContent
        _breakpoints.value = emptySet()
        _aiHints.value = emptyList()
        clearConsole()
        screenModelScope.launch(Dispatchers.IO) { writeState() }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PROJECTS & FILES
    // ────────────────────────────────────────────────────────────────────────
    private fun createDefaultProject() {
        val file = CodeFile(
            id = UUID.randomUUID().toString(), name = "Main.kt", language = Language.KOTLIN,
            content = "fun main() {\n    println(\"Hello, CodeLearn!\")\n\n    val name = \"World\"\n    println(\"Hello, \$name!\")\n\n    for (i in 1..5) {\n        println(\"Count: \$i\")\n    }\n}"
        )
        val project = Project(
            id = UUID.randomUUID().toString(), name = "My First Project",
            language = Language.KOTLIN, files = mutableListOf(file), description = "Getting started"
        )
        _projects.value = listOf(project)
        _currentProject.value = project
        _currentFile.value = file
        _openFiles.value = listOf(file)
        _currentCode.value = file.content
        _selectedLanguage.value = Language.KOTLIN
        gitService.initRepository(project.name)
        screenModelScope.launch(Dispatchers.IO) { writeState() }
    }

    fun createNewProject(name: String, language: Language): Project {
        val file = CodeFile(
            id = UUID.randomUUID().toString(), name = "Main.${language.fileExtension}",
            language = language, content = getStarterTemplate(language)
        )
        val project = Project(
            id = UUID.randomUUID().toString(), name = name,
            language = language, files = mutableListOf(file)
        )
        _projects.value = _projects.value + project
        _currentProject.value = project
        _currentFile.value = file
        _openFiles.value = listOf(file)
        _currentCode.value = file.content
        _selectedLanguage.value = language
        gitService.initRepository(name)
        clearConsole(); _aiHints.value = emptyList(); _breakpoints.value = emptySet()
        screenModelScope.launch(Dispatchers.IO) { writeState() }
        return project
    }

    // ── Open an existing project ──────────────────────────────────────────────
    fun openProject(project: Project, onReady: () -> Unit = {}) {
        fun doSwitch() {
            _currentProject.value = project
            val file = project.files.firstOrNull()
            if (file != null) {
                _currentFile.value = file
                _currentCode.value = file.content
                _selectedLanguage.value = file.language
                _openFiles.value = listOf(file)
            } else {
                _currentFile.value = null
                _currentCode.value = ""
                _openFiles.value = emptyList()
            }
            clearConsole()
            _aiHints.value = emptyList()
            _inlineErrors.value = emptyList()
            _breakpoints.value = emptySet()
            gitService.initRepository(project.name)
            screenModelScope.launch(Dispatchers.IO) { writeState() }
            onReady()
        }

        if (hasUnsavedChanges()) {
            showSavePrompt(
                fileName = _currentFile.value?.name ?: "file",
                onSave    = { persistAndSave(); dismissSavePrompt(); doSwitch() },
                onDiscard = { dismissSavePrompt(); doSwitch() },
                onCancel  = { dismissSavePrompt() }
            )
        } else {
            doSwitch()
        }
    }

    // ── Delete a project ──────────────────────────────────────────────────────
    fun deleteProject(project: Project) {
        val remaining = _projects.value.filter { it.id != project.id }
        _projects.value = remaining
        // If we just deleted the active project, switch to another
        if (_currentProject.value?.id == project.id) {
            if (remaining.isNotEmpty()) {
                openProject(remaining.last())
            } else {
                createDefaultProject()
            }
        }
        screenModelScope.launch(Dispatchers.IO) { writeState() }
    }

    // ── Rename a project ──────────────────────────────────────────────────────
    fun renameProject(project: Project, newName: String) {
        val trimmed = newName.trim().ifBlank { return }
        val updated = project.copy(name = trimmed)
        _projects.value = _projects.value.map { if (it.id == project.id) updated else it }
        if (_currentProject.value?.id == project.id) _currentProject.value = updated
        screenModelScope.launch(Dispatchers.IO) { writeState() }
    }

    fun createNewFile(name: String, language: Language) {
        val finalName = if (name.contains('.')) name else "$name.${language.fileExtension}"
        val file = CodeFile(id = UUID.randomUUID().toString(), name = finalName, language = language, content = getStarterTemplate(language))
        _currentProject.value?.files?.add(file)
        _openFiles.value = _openFiles.value + file
        _currentFile.value = file; _currentCode.value = file.content; _selectedLanguage.value = language
        gitService.markFileNew(finalName)
        screenModelScope.launch(Dispatchers.IO) { writeState() }
    }

    fun openFile(file: CodeFile) {
        if (hasUnsavedChanges()) {
            showSavePrompt(
                fileName = _currentFile.value?.name ?: "file",
                onSave = { persistAndSave(); dismissSavePrompt(); doOpenFile(file) },
                onDiscard = { dismissSavePrompt(); doOpenFile(file) },
                onCancel = { dismissSavePrompt() }
            )
        } else { doOpenFile(file) }
    }

    private fun doOpenFile(file: CodeFile) {
        if (_openFiles.value.none { it.id == file.id }) _openFiles.value = _openFiles.value + file
        _currentFile.value = file; _currentCode.value = file.content; _selectedLanguage.value = file.language
        clearConsole()
    }

    fun closeFile(file: CodeFile) {
        val isCurrentModified = file.id == _currentFile.value?.id && hasUnsavedChanges()
        if (isCurrentModified) {
            showSavePrompt(
                fileName = file.name,
                onSave = {
                    val s = file.copy(content = _currentCode.value, isModified = false)
                    updateFileInProject(s); dismissSavePrompt(); doCloseFile(file)
                    screenModelScope.launch(Dispatchers.IO) { writeState() }
                },
                onDiscard = { dismissSavePrompt(); doCloseFile(file) },
                onCancel = { dismissSavePrompt() }
            )
        } else { doCloseFile(file) }
    }

    private fun doCloseFile(file: CodeFile) {
        val newOpen = _openFiles.value.filter { it.id != file.id }
        _openFiles.value = newOpen
        if (_currentFile.value?.id == file.id) {
            val next = newOpen.lastOrNull()
            _currentFile.value = next; _currentCode.value = next?.content ?: ""; _selectedLanguage.value = next?.language ?: Language.KOTLIN
        }
    }

    fun updateCode(code: String) {
        _currentCode.value = code
        _currentFile.value?.let { file ->
            val modified = code != file.content
            if (file.isModified != modified) {
                val updated = file.copy(isModified = modified)
                _currentFile.value = updated
                updateFileInProject(updated)
                _openFiles.value = _openFiles.value.map { if (it.id == updated.id) updated else it }
                if (modified) gitService.markFileModified(file.name)
            }
        }
    }

    fun setCursorPosition(pos: Int) { _cursorPosition.value = pos }

    fun saveCurrentFile() {
        persistAndSave()
        addConsole("✅ ${_currentFile.value?.name ?: "File"} saved", ConsoleType.SUCCESS)
    }

    private fun updateFileInProject(file: CodeFile) {
        val proj = _currentProject.value ?: return
        val i = proj.files.indexOfFirst { it.id == file.id }
        if (i >= 0) proj.files[i] = file
    }

    // ────────────────────────────────────────────────────────────────────────
    // AUTOCOMPLETE
    // ────────────────────────────────────────────────────────────────────────
    fun dismissSuggestions() { _showSuggestions.value = false }

    fun applySuggestion(s: AutocompleteSuggestion) {
        val code = _currentCode.value
        val pos = _cursorPosition.value.coerceIn(0, code.length)
        var start = pos - 1
        while (start >= 0 && (code[start].isLetterOrDigit() || code[start] == '_')) start--
        val prefix = code.substring(start + 1, pos)
        updateCode(code.substring(0, pos - prefix.length) + s.text + code.substring(pos))
        _showSuggestions.value = false
    }

    // ────────────────────────────────────────────────────────────────────────
    // RUN CODE
    // ────────────────────────────────────────────────────────────────────────
    fun runCode() {
        screenModelScope.launch {
            _isRunning.value = true
            _errorAnalysis.value = null
            clearConsole()
            val lang = _selectedLanguage.value
            addConsole("▶  Running ${lang.displayName} via Piston...", ConsoleType.INFO)

            val result = Judge0Engine.execute(_currentCode.value, lang)

            when {
                result.timedOut -> {
                    addConsole("⏱  Time Limit Exceeded (>10s)", ConsoleType.ERROR)
                    addConsole("💡  Your code might have an infinite loop", ConsoleType.WARNING)
                }
                result.exitCode != 0 || result.stderr.isNotBlank() -> {
                    // Show raw error first
                    if (result.stdout.isNotBlank()) {
                        result.stdout.lines().forEach { addConsole(it, ConsoleType.NORMAL) }
                        addConsole("", ConsoleType.NORMAL)
                    }
                    if (result.stderr.isNotBlank()) {
                        addConsole("❌  Error (${lang.displayName}):", ConsoleType.ERROR)
                        result.stderr.lines().take(20).forEach { addConsole("   $it", ConsoleType.ERROR) }
                    }
                    addConsole("", ConsoleType.NORMAL)
                    addConsole("Exit code: ${result.exitCode}  •  ${result.durationMs}ms  •  engine: ${result.engine}", ConsoleType.MUTED)

                    // Store error for AI analysis
                    _lastError.value = result.stderr

                    // Trigger AI error explanation in background
                    if (result.stderr.isNotBlank()) {
                        addConsole("", ConsoleType.NORMAL)
                        addConsole("🤖  AI is analysing your error...", ConsoleType.INFO)
                        screenModelScope.launch {
                            val analysis = CodeIntelligence.explainError(_currentCode.value, result.stderr, lang)
                            _errorAnalysis.value = analysis
                            addConsole("─────────────────────────────", ConsoleType.MUTED)
                            addConsole("🤖  AI Error Analysis: ${analysis.errorTitle}", ConsoleType.WARNING)
                            addConsole(analysis.plainExplanation, ConsoleType.INFO)
                            if (analysis.tip.isNotBlank()) addConsole("💡  Tip: ${analysis.tip}", ConsoleType.INFO)
                            if (analysis.fixedCode.isNotBlank()) addConsole("✨  Tap 'Fix with AI' to apply the suggested fix", ConsoleType.SUCCESS)
                        }
                    }
                }
                else -> {
                    if (result.stdout.isNotBlank()) {
                        result.stdout.lines().forEach { addConsole(it, ConsoleType.NORMAL) }
                    } else {
                        addConsole("[No output produced]", ConsoleType.MUTED)
                        addConsole("💡  Use println() / print() / cout / puts to see output", ConsoleType.MUTED)
                    }
                    addConsole("", ConsoleType.NORMAL)
                    addConsole("✅  Done in ${result.durationMs}ms  •  engine: ${result.engine}", ConsoleType.SUCCESS)
                    _lastError.value = ""
                    _errorAnalysis.value = null
                }
            }
            _isRunning.value = false
        }
    }

    // ── Fix code with AI ───────────────────────────────────────────────────
    fun fixCodeWithAI() {
        val error = _lastError.value
        if (error.isBlank()) { addConsole("No error to fix", ConsoleType.MUTED); return }
        screenModelScope.launch {
            _isFixingCode.value = true
            addConsole("🔧  Applying AI fix...", ConsoleType.INFO)
            val (fixedCode, explanation) = Pair(CodeIntelligence.tryAutoFix(_currentCode.value, _lastError.value, _selectedLanguage.value), "Auto-fix applied")
            if (fixedCode != _currentCode.value) {
                updateCode(fixedCode)
                addConsole("✅  Code fixed! Changes applied to editor.", ConsoleType.SUCCESS)
                addConsole("📝  What changed: $explanation", ConsoleType.INFO)
            } else {
                addConsole("⚠️  Could not auto-fix. $explanation", ConsoleType.WARNING)
            }
            _isFixingCode.value = false
        }
    }

    // ── Explain code with AI ───────────────────────────────────────────────
    fun explainCodeWithAI() {
        screenModelScope.launch {
            _isAnalyzing.value = true
            addConsole("🤖  Explaining your code...", ConsoleType.INFO)
            val explanation = CodeIntelligence.explainCode(_currentCode.value, _selectedLanguage.value)
            addConsole("─────────────────────────────", ConsoleType.MUTED)
            addConsole("📚  Code Explanation:", ConsoleType.INFO)
            explanation.lines().forEach { addConsole(it, ConsoleType.NORMAL) }
            _isAnalyzing.value = false
        }
    }

    // ── Export file to device ──────────────────────────────────────────────
    fun exportCurrentFile() {
        val file = _currentFile.value ?: return
        val saved = file.copy(content = _currentCode.value)
        screenModelScope.launch(Dispatchers.IO) {
            val result = FileExporter.exportFile(saved)
            _exportResult.value = result.message
        }
    }

    fun exportCurrentProject() {
        val project = _currentProject.value ?: return
        screenModelScope.launch(Dispatchers.IO) {
            val result = FileExporter.exportProjectAsZip(project)
            _exportResult.value = result.message
        }
    }

    fun clearExportResult() { _exportResult.value = null }

    fun clearConsole() { _consoleOutput.value = emptyList() }
    fun addConsole(text: String, type: ConsoleType = ConsoleType.NORMAL) {
        _consoleOutput.value = _consoleOutput.value + ConsoleEntry(text, type)
    }

    // ────────────────────────────────────────────────────────────────────────
    // DEBUG
    // ────────────────────────────────────────────────────────────────────────
    fun toggleBreakpoint(line: Int) {
        val s = _breakpoints.value.toMutableSet()
        if (line in s) s.remove(line) else s.add(line)
        _breakpoints.value = s
    }

    fun startDebug() {
        if (_breakpoints.value.isEmpty()) { addConsole("⚠️  Add breakpoints — click line numbers", ConsoleType.WARNING); return }
        screenModelScope.launch {
            clearConsole()
            val codeLines = _currentCode.value.lines()
            val bps = _breakpoints.value.sorted()
            addConsole("🐛  Debug started  •  ${bps.size} breakpoint(s)", ConsoleType.INFO)
            _debugSession.value = DebugSession(isActive = true, breakpoints = bps.map { Breakpoint(_currentFile.value?.id ?: "", it) })
            for (bp in bps) {
                delay(500)
                val lineCode = codeLines.getOrNull(bp)?.trim() ?: continue
                val vars = extractVars(codeLines, bp)
                _debugSession.value = _debugSession.value.copy(currentLine = bp, variables = vars, callStack = buildStack(codeLines, bp))
                addConsole("⏸  Line ${bp + 1}: $lineCode", ConsoleType.DEBUG)
                vars.forEach { (k, v) -> addConsole("     $k = $v", ConsoleType.DEBUG_VAR) }
                delay(700)
            }
            addConsole("▶  Continuing...", ConsoleType.INFO)
            val run = Judge0Engine.execute(_currentCode.value, _selectedLanguage.value)
            if (run.stdout.isNotBlank()) run.stdout.lines().forEach { addConsole("  $it", ConsoleType.NORMAL) }
            if (run.stderr.isNotBlank()) run.stderr.lines().take(5).forEach { addConsole("  $it", ConsoleType.ERROR) }
            addConsole("✅  Debug complete", ConsoleType.SUCCESS)
            _debugSession.value = DebugSession(isActive = false)
        }
    }

    fun stopDebug() { _debugSession.value = DebugSession(isActive = false); addConsole("⏹  Stopped", ConsoleType.WARNING) }

    private fun extractVars(lines: List<String>, upTo: Int): Map<String, String> {
        val vars = mutableMapOf<String, String>()
        val p = Regex("""(?:val|var|let|const|int|double|String|bool|auto)\s+(\w+)\s*(?::\s*\w+)?\s*=\s*"?([^";\n]+)"?""")
        for (i in 0..minOf(upTo, lines.size - 1)) {
            p.find(lines[i].trim())?.let { m ->
                val n = m.groupValues[1].trim(); val v = m.groupValues[2].trim().trimEnd(';', '"')
                if (n.isNotBlank() && v.isNotBlank()) vars[n] = v
            }
        }
        return vars
    }

    private fun buildStack(lines: List<String>, current: Int): List<String> {
        val stack = mutableListOf<String>()
        val p = Regex("""(?:fun|def|void|function)\s+(\w+)\s*\(""")
        for (i in 0..minOf(current, lines.size - 1)) {
            p.find(lines[i].trim())?.let { m -> val fn = m.groupValues[1]; if (fn != "main") stack.add(0, "$fn() [L${i+1}]") }
        }
        stack.add("main() [L${current+1}]")
        return stack.take(5)
    }

    // ────────────────────────────────────────────────────────────────────────
    // AI HINTS
    // ────────────────────────────────────────────────────────────────────────
    fun requestAiHint() {
        screenModelScope.launch {
            _isAnalyzing.value = true
            addConsole("🤖  Running AI code review...", ConsoleType.INFO)
            val review = CodeIntelligence.reviewCode(_currentCode.value, _selectedLanguage.value)
            _codeReview.value = review
            _aiHints.value = review.issues.map { issue ->
                AiHint(
                    title = "Line ${issue.line}: ${issue.message}",
                    message = issue.suggestion,
                    hintType = when (issue.severity) {
                        IssueSeverity.ERROR -> HintType.ERROR
                        IssueSeverity.WARNING -> HintType.WARNING
                        IssueSeverity.INFO -> HintType.INFO
                    }
                )
            }
            addConsole("─────────────────────────────", ConsoleType.MUTED)
            addConsole("🤖  Review: ${review.overallFeedback}", ConsoleType.INFO)
            review.issues.forEach { issue ->
                val icon = when (issue.severity) {
                    IssueSeverity.ERROR -> "❌"
                    IssueSeverity.WARNING -> "⚠️"
                    IssueSeverity.INFO -> "💡"
                }
                addConsole("$icon  Line ${issue.line}: ${issue.message}", when (issue.severity) {
                    IssueSeverity.ERROR -> ConsoleType.ERROR
                    IssueSeverity.WARNING -> ConsoleType.WARNING
                    IssueSeverity.INFO -> ConsoleType.INFO
                })
                if (issue.suggestion.isNotBlank()) addConsole("   Fix: ${issue.suggestion}", ConsoleType.MUTED)
            }
            _isAnalyzing.value = false
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // QUIZ
    // ────────────────────────────────────────────────────────────────────────
    fun selectProblem(problem: QuizProblem) {
        _selectedProblem.value = problem
        _submissionResult.value = null
        _quizTestResults.value = emptyList()
        // Auto-disable AI during quiz so no hints leak to student
        setAIEnabled(false)
        val lang = _selectedLanguage.value
        val saved = AppDatabase.load().quizCodeByProblemAndLang["${problem.id}::${lang.fileExtension}"]
        _quizCode.value = saved ?: (problem.starterCode[lang.fileExtension] ?: problem.starterCode.values.first())
        clearConsole()
    }

    fun updateQuizCode(code: String) { _quizCode.value = code }

    fun setQuizLanguage(lang: Language) {
        _selectedLanguage.value = lang
        val problem = _selectedProblem.value ?: return
        val saved = AppDatabase.load().quizCodeByProblemAndLang["${problem.id}::${lang.fileExtension}"]
        _quizCode.value = saved ?: (problem.starterCode[lang.fileExtension] ?: problem.starterCode.values.first())
        _submissionResult.value = null; _quizTestResults.value = emptyList(); clearConsole()
    }

    fun runQuizTests() {
        screenModelScope.launch {
            _isRunning.value = true; _quizTestResults.value = emptyList(); clearConsole()
            val problem = _selectedProblem.value ?: return@launch
            addConsole("▶  Running visible tests...", ConsoleType.INFO)
            val eval = QuizEvaluator.evaluate(problem, _quizCode.value, _selectedLanguage.value, runHidden = false)
            if (eval.compilationError.isNotEmpty()) {
                addConsole("❌  Compilation Error: ${eval.compilationError}", ConsoleType.ERROR)
                _isRunning.value = false; return@launch
            }
            _quizTestResults.value = eval.testResults
            eval.testResults.forEach { tc ->
                addConsole("${if (tc.passed) "✅" else "❌"}  Test ${tc.index + 1}", if (tc.passed) ConsoleType.SUCCESS else ConsoleType.ERROR)
                addConsole("   Input:    ${tc.input}", ConsoleType.MUTED)
                addConsole("   Expected: ${tc.expected}", ConsoleType.MUTED)
                addConsole("   Got:      ${tc.actual}", if (tc.passed) ConsoleType.SUCCESS else ConsoleType.ERROR)
                if (tc.error.isNotEmpty()) addConsole("   ${tc.error}", ConsoleType.ERROR)
                addConsole("", ConsoleType.NORMAL)
            }
            val p = eval.testResults.count { it.passed }; val t = eval.testResults.size
            addConsole("$p / $t passed  •  ${eval.totalMs}ms", if (p == t) ConsoleType.SUCCESS else ConsoleType.WARNING)
            _isRunning.value = false
        }
    }

    fun submitQuizSolution() {
        screenModelScope.launch {
            _isRunning.value = true; _submissionResult.value = null; _quizTestResults.value = emptyList(); clearConsole()
            val problem = _selectedProblem.value ?: return@launch
            val lang = _selectedLanguage.value; val code = _quizCode.value
            val starter = problem.starterCode[lang.fileExtension] ?: problem.starterCode.values.first()
            if (code.filter { !it.isWhitespace() } == starter.filter { !it.isWhitespace() }) {
                addConsole("❌  Write your solution first!", ConsoleType.ERROR)
                _submissionResult.value = SubmissionResult(SubmissionStatus.WRONG_ANSWER, 0, problem.testCases.size)
                _isRunning.value = false; return@launch
            }
            addConsole("⏳  Submitting ${problem.testCases.size} test cases...", ConsoleType.INFO)
            val eval = QuizEvaluator.evaluate(problem, code, lang, runHidden = true)
            if (eval.compilationError.isNotEmpty()) {
                addConsole("❌  ${eval.compilationError}", ConsoleType.ERROR)
                _submissionResult.value = SubmissionResult(SubmissionStatus.COMPILE_ERROR, 0, problem.testCases.size, errorMessage = eval.compilationError)
                _isRunning.value = false; return@launch
            }
            _quizTestResults.value = eval.testResults
            val passed = eval.testResults.count { it.passed }
            val total = problem.testCases.size
            val status = if (passed == total) SubmissionStatus.ACCEPTED else SubmissionStatus.WRONG_ANSWER
            eval.testResults.forEach { tc ->
                addConsole("${if (tc.passed) "✅" else "❌"}  Test ${tc.index + 1}${if (tc.isHidden) " (hidden)" else ""}", if (tc.passed) ConsoleType.SUCCESS else ConsoleType.ERROR)
                if (!tc.passed && !tc.isHidden) { addConsole("   Expected: ${tc.expected}", ConsoleType.MUTED); addConsole("   Got: ${tc.actual}", ConsoleType.ERROR) }
            }
            addConsole(if (status == SubmissionStatus.ACCEPTED) "🎉  Accepted! $passed/$total" else "❌  Wrong Answer $passed/$total",
                if (status == SubmissionStatus.ACCEPTED) ConsoleType.SUCCESS else ConsoleType.ERROR)
            if (status == SubmissionStatus.ACCEPTED) { _solvedProblems.value = _solvedProblems.value + problem.id; screenModelScope.launch(Dispatchers.IO) { writeState() } }
            _submissionResult.value = SubmissionResult(status, passed, total, eval.totalMs, (12..60L).random())
            _isRunning.value = false
        }
    }

    // ── AI Toggle ─────────────────────────────────────────────────────────────
    fun toggleAI() {
        _aiEnabled.value = !_aiEnabled.value
        if (!_aiEnabled.value) {
            _aiHints.value = emptyList()
            _codeReview.value = null
            _aiCompletions.value = emptyList()
            _suggestions.value = emptyList()
            _showSuggestions.value = false
            _inlineErrors.value = emptyList()
        }
    }

    fun setAIEnabled(enabled: Boolean) {
        _aiEnabled.value = enabled
        if (!enabled) {
            _aiHints.value = emptyList()
            _codeReview.value = null
            _aiCompletions.value = emptyList()
            _suggestions.value = emptyList()
            _showSuggestions.value = false
        }
    }

    // ── Share Code as Link (using hastebin - free, no login) ─────────────────
    // Creates a shareable link with the code embedded as base64 in URL
    // so recipients can open it in browser and copy the code
    fun shareCurrentCode() {
        val code = _currentCode.value
        val lang = _selectedLanguage.value
        val fileName = _currentFile.value?.name ?: "code.${lang.fileExtension}"
        screenModelScope.launch {
            try {
                // Encode code as base64 URL fragment (no server needed)
                val encoded = java.util.Base64.getUrlEncoder().encodeToString(code.toByteArray())
                val link = "https://codelearn-share.netlify.app/#lang=${lang.fileExtension}&name=$fileName&code=$encoded"
                _shareLink.value = link
                addConsole("🔗  Share link generated!", ConsoleType.SUCCESS)
                addConsole("   $link", ConsoleType.INFO)
            } catch (e: Exception) {
                // Fallback: generate a simple data URL
                val encoded = java.util.Base64.getUrlEncoder().encodeToString(code.toByteArray())
                _shareLink.value = "data:text/plain;base64,$encoded"
                addConsole("📋  Code encoded — use 'Copy Link' to share", ConsoleType.INFO)
            }
        }
    }

    fun clearShareLink() { _shareLink.value = null }


    fun setTheme(t: IDETheme) { _theme.value = t; screenModelScope.launch(Dispatchers.IO) { writeState() } }

    fun getStarterTemplate(language: Language): String = when (language) {
        Language.KOTLIN     -> "fun main() {\n    println(\"Hello, World!\")\n}"
        Language.JAVA       -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}"
        Language.PYTHON     -> "# Python starter\nprint(\"Hello, World!\")\n\nname = \"Student\"\nprint(f\"Hello, {name}!\")"
        Language.JAVASCRIPT -> "// JavaScript starter\nconsole.log(\"Hello, World!\");\n\nconst name = \"Student\";\nconsole.log(`Hello, \${name}!`);"
        Language.CSHARP     -> "using System;\n\nclass Program {\n    static void Main(string[] args) {\n        Console.WriteLine(\"Hello, World!\");\n    }\n}"
        Language.CPP        -> "#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Hello, World!\" << endl;\n    return 0;\n}"
        Language.C          -> "#include <stdio.h>\n\nint main() {\n    printf(\"Hello, World!\\n\");\n    return 0;\n}"
        Language.RUBY       -> "puts \"Hello, World!\"\n"
        Language.DART       -> "void main() {\n  print('Hello, World!');\n}"
        Language.VB         -> "Module Program\n    Sub Main(args As String())\n        Console.WriteLine(\"Hello, World!\")\n    End Sub\nEnd Module"
    }

    // ── Serialization helpers ────────────────────────────────────────────────
    private fun Project.toSaved() = SavedProject(id, name, language.fileExtension, files.map { it.toSaved() }, description, createdAt)
    private fun CodeFile.toSaved() = SavedFile(id, name, language.fileExtension, content, false, createdAt)
    private fun SavedProject.toProject() = Project(id, name, Language.fromExtension(languageCode), files.map { it.toFile() }.toMutableList(), createdAt,description)
    private fun SavedFile.toFile() = CodeFile(id, name, Language.fromExtension(languageCode), content, false, createdAt)

    companion object {
        /** Convenience accessor inside @Composable — use LocalAppViewModel.current */
        val current: AppViewModel
            @Composable get() = LocalAppViewModel.current
    }
}
