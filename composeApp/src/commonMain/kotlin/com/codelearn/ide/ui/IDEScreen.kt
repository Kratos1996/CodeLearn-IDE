package com.codelearn.ide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelearn.ide.model.AiHint
import com.codelearn.ide.model.CodeFile
import com.codelearn.ide.model.HintType
import com.codelearn.ide.model.Language
import com.codelearn.ide.viewmodel.ConsoleEntry
import com.codelearn.ide.viewmodel.ConsoleType
import com.codelearn.ide.viewmodel.AppViewModel
import kotlinx.coroutines.delay

enum class SidePanel { NONE, FILES, GIT, DEBUG, AI }
enum class BottomPanel { CONSOLE, DEBUG_VARS, HINTS }

@Composable
fun IDEScreenContent(
    vm: AppViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigateQuiz: () -> Unit = {}
) {
    val currentCode by vm.currentCode.collectAsState()
    val selectedLanguage by vm.selectedLanguage.collectAsState()
    val currentFile by vm.currentFile.collectAsState()
    val openFiles by vm.openFiles.collectAsState()
    val breakpoints by vm.breakpoints.collectAsState()
    val debugSession by vm.debugSession.collectAsState()
    val suggestions by vm.suggestions.collectAsState()
    val showSuggestions by vm.showSuggestions.collectAsState()
    val aiHints by vm.aiHints.collectAsState()
    val consoleOutput by vm.consoleOutput.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val isFixingCode by vm.isFixingCode.collectAsState()
    val errorAnalysis by vm.errorAnalysis.collectAsState()
    val exportResult by vm.exportResult.collectAsState()
    val aiEnabled by vm.aiEnabled.collectAsState()
    val shareLink by vm.shareLink.collectAsState()
    val inlineErrors by vm.inlineErrors.collectAsState()
    val gitStatus by vm.gitService.status.collectAsState()

    var activeSidePanel by remember { mutableStateOf(SidePanel.FILES) }
    var activeBottomPanel by remember { mutableStateOf(BottomPanel.CONSOLE) }
    var bottomPanelHeight by remember { mutableStateOf(180.dp) }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    // Auto-switch to console when code runs
    LaunchedEffect(isRunning) { if (isRunning) activeBottomPanel = BottomPanel.CONSOLE }

    // Show export result
    LaunchedEffect(exportResult) {
        if (exportResult != null) {
            kotlinx.coroutines.delay(3000)
            vm.clearExportResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IDEColors.bg0)
    ) {
        // ─── Top Toolbar ──────────────────────────────────────────────────────
        IDETopBar(
            currentFile = currentFile,
            selectedLanguage = selectedLanguage,
            isRunning = isRunning,
            isDebugging = debugSession.isActive,
            showLangDropdown = showLanguageDropdown,
            onShowLangDropdown = { showLanguageDropdown = it },
            onLanguageSelect = { vm.requestLanguageChange(it) },
            onRun = { vm.runCode() },
            onDebug = { if (debugSession.isActive) vm.stopDebug() else vm.startDebug() },
            onSave = { vm.saveCurrentFile() },
            onHome = { onNavigateHome() },
            onQuiz = { onNavigateQuiz() },
            onExportFile = { vm.exportCurrentFile() },
            onExportProject = { vm.exportCurrentProject() },
            onShareCode = { vm.shareCurrentCode() },
            onToggleAI = { vm.toggleAI() },
            aiEnabled = aiEnabled
        )

        // ─── Tab Bar ──────────────────────────────────────────────────────────
        if (openFiles.isNotEmpty()) {
            TabBar(
                openFiles = openFiles,
                currentFile = currentFile,
                onSelect = { vm.openFile(it) },
                onClose = { vm.closeFile(it) }
            )
        }

        // ─── Main Content Area ────────────────────────────────────────────────
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Activity Bar (leftmost icons)
            ActivityBar(
                activePanel = activeSidePanel,
                onPanelSelect = { activeSidePanel = if (activeSidePanel == it) SidePanel.NONE else it },
                modifiedFilesCount = openFiles.count { it.isModified },
                gitChangesCount = gitStatus.modifiedFiles.size + gitStatus.stagedFiles.size,
                hintsCount = aiHints.size
            )

            // Side Panel
            AnimatedVisibility(
                visible = activeSidePanel != SidePanel.NONE,
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { -it }
            ) {
                SidePanelContent(
                    panel = activeSidePanel,
                    vm = vm,
                    onNewFile = { showNewFileDialog = true }
                )
            }

            // Editor + Console
            Column(modifier = Modifier.weight(1f)) {
                // Code Editor
                CodeEditor(
                    code = currentCode,
                    language = selectedLanguage,
                    breakpoints = breakpoints,
                    currentDebugLine = debugSession.currentLine,
                    suggestions = suggestions,
                    showSuggestions = showSuggestions,
                    inlineErrors = inlineErrors,
                    onCodeChange = { vm.updateCode(it) },
                    onCursorChange = { vm.setCursorPosition(it) },
                    onToggleBreakpoint = { vm.toggleBreakpoint(it) },
                    onSuggestionSelected = { vm.applySuggestion(it) },
                    onDismissSuggestions = { vm.dismissSuggestions() },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )

                // Inline error summary bar
                if (inlineErrors.isNotEmpty() && aiEnabled) {
                    val errCount = inlineErrors.count { it.type == com.codelearn.ide.ai.InlineErrorType.ERROR }
                    val warnCount = inlineErrors.count { it.type == com.codelearn.ide.ai.InlineErrorType.WARNING }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IDEColors.bg1)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍 AI:", color = IDEColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (errCount > 0)
                            Text("❌ $errCount error${if (errCount > 1) "s" else ""}",
                                color = IDEColors.red, fontSize = 11.sp)
                        if (warnCount > 0)
                            Text("⚠️ $warnCount warning${if (warnCount > 1) "s" else ""}",
                                color = IDEColors.orange, fontSize = 11.sp)
                        // Show first error message
                        val firstError = inlineErrors.firstOrNull()
                        if (firstError != null)
                            Text("Line ${firstError.line + 1}: ${firstError.message}",
                                color = IDEColors.textSecondary, fontSize = 11.sp,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                maxLines = 1, modifier = Modifier.weight(1f))
                        Text("Details →", color = IDEColors.accent, fontSize = 11.sp,
                            modifier = Modifier.clickable { activeBottomPanel = BottomPanel.HINTS })
                    }
                }

                // Bottom Panel
                BottomPanelSection(
                    activePanel = activeBottomPanel,
                    onPanelSelect = { activeBottomPanel = it },
                    consoleOutput = consoleOutput,
                    debugSession = debugSession,
                    aiHints = aiHints,
                    isAnalyzing = isRunning || isFixingCode,
                    errorAnalysis = errorAnalysis,
                    inlineErrors = inlineErrors,
                    onClearConsole = { vm.clearConsole() },
                    onRequestHint = { vm.requestAiHint() },
                    onFixWithAI = { vm.fixCodeWithAI(); activeBottomPanel = BottomPanel.CONSOLE },
                    onExplainCode = { vm.explainCodeWithAI(); activeBottomPanel = BottomPanel.CONSOLE }
                )

                // Export result toast
                exportResult?.let { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = if (msg.startsWith("✅")) IDEColors.green.copy(alpha = 0.9f)
                                    else IDEColors.red.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(msg, modifier = Modifier.padding(8.dp, 4.dp), fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Share link dialog
    shareLink?.let { link ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { vm.clearShareLink() }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = IDEColors.bg1,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔗 Share Code", color = IDEColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Share this link. Anyone can view your code:", color = IDEColors.textSecondary, fontSize = 13.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(IDEColors.bg0)
                            .padding(10.dp)
                    ) {
                        Text(link, color = IDEColors.accent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { vm.clearShareLink() }) { Text("Close") }
                    }
                }
            }
        }
    }

    if (showNewFileDialog) {
        NewFileDialog(
            currentLanguage = selectedLanguage,
            onDismiss = { showNewFileDialog = false },
            onCreate = { name, lang ->
                vm.createNewFile(name, lang)
                showNewFileDialog = false
            }
        )
    }
}

@Composable
fun SidePanelContent(panel: SidePanel, vm: AppViewModel, onNewFile: () -> Unit) {
    val project by vm.currentProject.collectAsState()
    val gitStatus by vm.gitService.status.collectAsState()
    val aiHints by vm.aiHints.collectAsState()
    val debugSession by vm.debugSession.collectAsState()

    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(IDEColors.bg1)
    ) {
        when (panel) {
            SidePanel.FILES -> {
                SidePanelHeader("Explorer") {
                    ToolbarIconBtn("+", "New File", onClick = onNewFile)
                }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    project?.files?.let { files ->
                        items(files) { file ->
                            FileTreeItem(file, onSelect = { vm.openFile(it) })
                        }
                    }
                }
            }
            SidePanel.GIT -> {
                SidePanelHeader("Source Control")
                Column(modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState())) {
                    Text("Branch: ${gitStatus.branch}", color = IDEColors.accent, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

                    if (gitStatus.stagedFiles.isNotEmpty()) {
                        GitSection("Staged Changes", gitStatus.stagedFiles, IDEColors.green) { fileName ->
                            vm.gitService.unstageFile(fileName)
                        }
                    }
                    if (gitStatus.modifiedFiles.isNotEmpty()) {
                        GitSection("Changes", gitStatus.modifiedFiles, IDEColors.orange) { fileName ->
                            vm.gitService.stageFile(fileName)
                        }
                    }
                    if (gitStatus.untrackedFiles.isNotEmpty()) {
                        GitSection("Untracked", gitStatus.untrackedFiles, IDEColors.textSecondary) { fileName ->
                            vm.gitService.stageFile(fileName)
                        }
                    }

                    if (gitStatus.stagedFiles.isNotEmpty()) {
                        var commitMsg by remember { mutableStateOf("") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = commitMsg,
                            onValueChange = { commitMsg = it },
                            placeholder = { Text("Commit message...", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IDEColors.accent,
                                unfocusedBorderColor = IDEColors.bg4,
                                focusedTextColor = IDEColors.textPrimary,
                                unfocusedTextColor = IDEColors.textPrimary,
                                unfocusedPlaceholderColor = IDEColors.textMuted,
                                focusedPlaceholderColor = IDEColors.textMuted
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { vm.gitService.commit(commitMsg.ifBlank { "Update" }); commitMsg = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✓ Commit", fontSize = 12.sp)
                        }
                    }

                    if (gitStatus.commits.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Recent Commits", color = IDEColors.textSecondary, fontSize = 11.sp)
                        gitStatus.commits.take(5).forEach { commit ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("${commit.hash}  ", color = IDEColors.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(commit.message, color = IDEColors.textSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            SidePanel.DEBUG -> {
                SidePanelHeader("Debug")
                Column(modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        if (debugSession.isActive) "● Active" else "○ Inactive",
                        color = if (debugSession.isActive) IDEColors.green else IDEColors.textMuted,
                        fontSize = 12.sp
                    )
                    if (debugSession.isActive) {
                        Spacer(Modifier.height(8.dp))
                        Text("Variables", color = IDEColors.textSecondary, fontSize = 11.sp)
                        debugSession.variables.entries.forEach { (name, value) ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("$name = ", color = IDEColors.cyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text(value, color = IDEColors.orange, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Call Stack", color = IDEColors.textSecondary, fontSize = 11.sp)
                        debugSession.callStack.forEach {
                            Text("  $it", color = IDEColors.textPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
            SidePanel.AI -> {
                SidePanelHeader("AI Hints")
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    if (aiHints.isEmpty()) {
                        item {
                            Text(
                                "Write some code and AI will suggest improvements!",
                                color = IDEColors.textMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    items(aiHints) { hint ->
                        AiHintCard(hint)
                    }
                }
            }
            SidePanel.NONE -> {}
        }
    }
}

@Composable
fun IDETopBar(
    currentFile: CodeFile?,
    selectedLanguage: Language,
    isRunning: Boolean,
    isDebugging: Boolean,
    showLangDropdown: Boolean,
    onShowLangDropdown: (Boolean) -> Unit,
    onLanguageSelect: (Language) -> Unit,
    onRun: () -> Unit,
    onDebug: () -> Unit,
    onSave: () -> Unit,
    onHome: () -> Unit,
    onQuiz: () -> Unit,
    onExportFile: () -> Unit = {},
    onExportProject: () -> Unit = {},
    onShareCode: () -> Unit = {},
    onToggleAI: () -> Unit = {},
    aiEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(IDEColors.bg1)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Home button
        ToolbarIconBtn("🏠", "Home", onClick = onHome)

        // File name
        Text(
            text = currentFile?.name ?: "untitled",
            color = IDEColors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(120.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Language Selector
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(IDEColors.bg3)
                    .clickable { onShowLangDropdown(true) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(selectedLanguage.icon, fontSize = 14.sp)
                Text(selectedLanguage.displayName, color = IDEColors.textPrimary, fontSize = 13.sp)
                Text("▾", color = IDEColors.textSecondary, fontSize = 11.sp)
            }
            DropdownMenu(
                expanded = showLangDropdown,
                onDismissRequest = { onShowLangDropdown(false) },
                modifier = Modifier.background(IDEColors.bg3)
            ) {
                Language.values().forEach { lang ->
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(lang.icon)
                                Text(lang.displayName, color = IDEColors.textPrimary)
                            }
                        },
                        onClick = { onLanguageSelect(lang); onShowLangDropdown(false) }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Action buttons
        ToolbarIconBtn("💾", "Save", onClick = onSave)
            // Export file
            ToolbarIconBtn("📤", "Export", onClick = onExportFile)
            // Share code as link
            ToolbarIconBtn("🔗", "Share", onClick = onShareCode)
            // AI toggle (glows when enabled)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (aiEnabled) IDEColors.accent.copy(alpha = 0.2f) else IDEColors.bg3)
                    .clickable(onClick = onToggleAI)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (aiEnabled) "🤖 AI" else "🤖 OFF",
                    fontSize = 12.sp,
                    color = if (aiEnabled) IDEColors.accent else IDEColors.textMuted,
                    fontWeight = FontWeight.Medium
                )
            }

        ToolbarIconBtn(
            icon = if (isDebugging) "⏹" else "🐛",
            tooltip = if (isDebugging) "Stop Debug" else "Debug",
            tint = if (isDebugging) IDEColors.red else IDEColors.orange,
            onClick = onDebug
        )

        // Run button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isRunning) IDEColors.bg4 else IDEColors.green)
                .clickable(enabled = !isRunning, onClick = onRun)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (isRunning) "⏸" else "▶", fontSize = 14.sp)
                Text("Run", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        ToolbarIconBtn("🧩", "Quiz", onClick = onQuiz)
    }
}

@Composable
fun ToolbarIconBtn(
    icon: String,
    tooltip: String,
    tint: Color = IDEColors.textSecondary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 16.sp)
    }
}

@Composable
fun TabBar(
    openFiles: List<CodeFile>,
    currentFile: CodeFile?,
    onSelect: (CodeFile) -> Unit,
    onClose: (CodeFile) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(IDEColors.bg1)
            .horizontalScroll(rememberScrollState())
    ) {
        openFiles.forEach { file ->
            val isActive = file.id == currentFile?.id
            Row(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(if (isActive) IDEColors.bg2 else Color.Transparent)
                    .border(
                        bottom = if (isActive) BorderStroke(2.dp, IDEColors.accent) else BorderStroke(0.dp, Color.Transparent)
                    )
                    .clickable { onSelect(file) }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (file.isModified) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(IDEColors.orange)
                    )
                }
                Text(
                    file.language.icon,
                    fontSize = 12.sp
                )
                Text(
                    file.name,
                    color = if (isActive) IDEColors.textPrimary else IDEColors.textSecondary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable { onClose(file) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("×", color = IDEColors.textMuted, fontSize = 14.sp)
                }
            }
        }
    }
}

// Helper to add bottom border
fun Modifier.border(bottom: BorderStroke): Modifier = this

@Composable
fun ActivityBar(
    activePanel: SidePanel,
    onPanelSelect: (SidePanel) -> Unit,
    modifiedFilesCount: Int,
    gitChangesCount: Int,
    hintsCount: Int
) {
    Column(
        modifier = Modifier
            .width(44.dp)
            .fillMaxHeight()
            .background(IDEColors.bg1)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ActivityBarIcon("📁", SidePanel.FILES, activePanel, onPanelSelect, modifiedFilesCount)
        ActivityBarIcon("🔀", SidePanel.GIT, activePanel, onPanelSelect, gitChangesCount)
        ActivityBarIcon("🐛", SidePanel.DEBUG, activePanel, onPanelSelect)
        ActivityBarIcon("🤖", SidePanel.AI, activePanel, onPanelSelect, hintsCount)
    }
}

@Composable
fun ActivityBarIcon(
    icon: String,
    panel: SidePanel,
    activePanel: SidePanel,
    onClick: (SidePanel) -> Unit,
    badgeCount: Int = 0
) {
    val isActive = panel == activePanel
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) IDEColors.accent.copy(0.2f) else Color.Transparent)
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) IDEColors.accent else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick(panel) },
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 16.sp)
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(IDEColors.accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (badgeCount > 9) "9+" else "$badgeCount",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SidePanel(panel: SidePanel, vm: AppViewModel, onNewFile: () -> Unit) {
    val project by vm.currentProject.collectAsState()
    val gitStatus by vm.gitService.status.collectAsState()
    val aiHints by vm.aiHints.collectAsState()
    val debugSession by vm.debugSession.collectAsState()

    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(IDEColors.bg1)
    ) {
        when (panel) {
            SidePanel.FILES -> {
                SidePanelHeader("Explorer") {
                    ToolbarIconBtn("+", "New File", onClick = onNewFile)
                }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    project?.files?.let { files ->
                        items(files) { file ->
                            FileTreeItem(file, onSelect = { vm.openFile(it) })
                        }
                    }
                }
            }
            SidePanel.GIT -> {
                SidePanelHeader("Source Control")
                Column(modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState())) {
                    Text("Branch: ${gitStatus.branch}", color = IDEColors.accent, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

                    if (gitStatus.stagedFiles.isNotEmpty()) {
                        GitSection("Staged Changes", gitStatus.stagedFiles, IDEColors.green) { fileName ->
                            vm.gitService.unstageFile(fileName)
                        }
                    }
                    if (gitStatus.modifiedFiles.isNotEmpty()) {
                        GitSection("Changes", gitStatus.modifiedFiles, IDEColors.orange) { fileName ->
                            vm.gitService.stageFile(fileName)
                        }
                    }
                    if (gitStatus.untrackedFiles.isNotEmpty()) {
                        GitSection("Untracked", gitStatus.untrackedFiles, IDEColors.textSecondary) { fileName ->
                            vm.gitService.stageFile(fileName)
                        }
                    }

                    if (gitStatus.stagedFiles.isNotEmpty()) {
                        var commitMsg by remember { mutableStateOf("") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = commitMsg,
                            onValueChange = { commitMsg = it },
                            placeholder = { Text("Commit message...", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IDEColors.accent,
                                unfocusedBorderColor = IDEColors.bg4,
                                focusedTextColor = IDEColors.textPrimary,
                                unfocusedTextColor = IDEColors.textPrimary,
                                unfocusedPlaceholderColor = IDEColors.textMuted,
                                focusedPlaceholderColor = IDEColors.textMuted
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { vm.gitService.commit(commitMsg.ifBlank { "Update" }); commitMsg = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✓ Commit", fontSize = 12.sp)
                        }
                    }

                    if (gitStatus.commits.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Recent Commits", color = IDEColors.textSecondary, fontSize = 11.sp)
                        gitStatus.commits.take(5).forEach { commit ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("${commit.hash}  ", color = IDEColors.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(commit.message, color = IDEColors.textSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            SidePanel.DEBUG -> {
                SidePanelHeader("Debug")
                Column(modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        if (debugSession.isActive) "● Active" else "○ Inactive",
                        color = if (debugSession.isActive) IDEColors.green else IDEColors.textMuted,
                        fontSize = 12.sp
                    )
                    if (debugSession.isActive) {
                        Spacer(Modifier.height(8.dp))
                        Text("Variables", color = IDEColors.textSecondary, fontSize = 11.sp)
                        debugSession.variables.entries.forEach { (name, value) ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("$name = ", color = IDEColors.cyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text(value, color = IDEColors.orange, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Call Stack", color = IDEColors.textSecondary, fontSize = 11.sp)
                        debugSession.callStack.forEach {
                            Text("  $it", color = IDEColors.textPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
            SidePanel.AI -> {
                SidePanelHeader("AI Hints")
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    if (aiHints.isEmpty()) {
                        item {
                            Text(
                                "Write some code and AI will suggest improvements!",
                                color = IDEColors.textMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    items(aiHints) { hint ->
                        AiHintCard(hint)
                    }
                }
            }
            SidePanel.NONE -> {}
        }
    }
}

@Composable
fun SidePanelHeader(title: String, action: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(IDEColors.bg0)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title.uppercase(), color = IDEColors.textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        action?.invoke()
    }
}

@Composable
fun FileTreeItem(file: CodeFile, onSelect: (CodeFile) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onSelect(file) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(file.language.icon, fontSize = 13.sp)
        Text(
            file.name,
            color = if (file.isModified) IDEColors.orange else IDEColors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (file.isModified) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(IDEColors.orange)
            )
        }
    }
}

@Composable
fun GitSection(title: String, files: List<String>, color: Color, onAction: (String) -> Unit) {
    Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
    files.forEach { fileName ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAction(fileName) }
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("M ", color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text(fileName, color = IDEColors.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun AiHintCard(hint: AiHint) {
    val (borderColor, bgColor, icon) = when (hint.hintType) {
        HintType.ERROR      -> Triple(IDEColors.red, IDEColors.red.copy(0.08f), "❌")
        HintType.WARNING    -> Triple(IDEColors.orange, IDEColors.orange.copy(0.08f), "⚠️")
        HintType.SUGGESTION -> Triple(IDEColors.accent, IDEColors.accent.copy(0.08f), "💡")
        HintType.INFO       -> Triple(IDEColors.blue, IDEColors.blue.copy(0.08f), "ℹ️")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 13.sp)
            Text(hint.title, color = IDEColors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Text(hint.message, color = IDEColors.textSecondary, fontSize = 11.sp)
        if (hint.codeExample.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(IDEColors.bg0)
                    .padding(8.dp)
            ) {
                Text(hint.codeExample, color = IDEColors.green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun BottomPanelSection(
    activePanel: BottomPanel,
    onPanelSelect: (BottomPanel) -> Unit,
    consoleOutput: List<com.codelearn.ide.viewmodel.ConsoleEntry>,
    debugSession: com.codelearn.ide.model.DebugSession,
    aiHints: List<AiHint>,
    isAnalyzing: Boolean,
    errorAnalysis: com.codelearn.ide.ai.AiErrorAnalysis? = null,
    inlineErrors: List<com.codelearn.ide.ai.InlineError> = emptyList(),
    onClearConsole: () -> Unit,
    onRequestHint: () -> Unit,
    onFixWithAI: () -> Unit = {},
    onExplainCode: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(IDEColors.bg1)
    ) {
        // Panel tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(IDEColors.bg0)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab("Console", BottomPanel.CONSOLE, activePanel, onPanelSelect)
            BottomTab("Debug", BottomPanel.DEBUG_VARS, activePanel, onPanelSelect)
            BottomTab("AI Hints", BottomPanel.HINTS, activePanel, onPanelSelect)

            Spacer(Modifier.weight(1f))

            when (activePanel) {
                BottomPanel.CONSOLE -> ToolbarIconBtn("🗑", "Clear", onClick = onClearConsole)
                BottomPanel.HINTS -> ToolbarIconBtn("✨", "Analyze", onClick = onRequestHint)
                else -> {}
            }
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(IDEColors.bg2)
        ) {
            when (activePanel) {
                BottomPanel.CONSOLE -> {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(consoleOutput.size) { scrollState.animateScrollTo(scrollState.maxValue) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        consoleOutput.forEach { entry ->
                            val entryColor = when (entry.type) {
                                ConsoleType.SUCCESS   -> IDEColors.green
                                ConsoleType.ERROR     -> IDEColors.red
                                ConsoleType.WARNING   -> IDEColors.orange
                                ConsoleType.INFO      -> IDEColors.blue
                                ConsoleType.DEBUG     -> IDEColors.cyan
                                ConsoleType.DEBUG_VAR -> IDEColors.orange
                                ConsoleType.MUTED     -> IDEColors.textMuted
                                ConsoleType.NORMAL    -> IDEColors.textSecondary
                            }
                            Text(
                                text = entry.text,
                                color = entryColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (consoleOutput.isEmpty()) {
                            Text("Press ▶ Run to see output...", color = IDEColors.textMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                BottomPanel.DEBUG_VARS -> {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        if (debugSession.isActive) {
                            debugSession.variables.entries.forEach { (k, v) ->
                                Row {
                                    Text("$k", color = IDEColors.cyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    Text(" = ", color = IDEColors.textSecondary, fontSize = 12.sp)
                                    Text(v, color = IDEColors.orange, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        } else {
                            Text("Start debugging to see variables", color = IDEColors.textMuted, fontSize = 12.sp)
                        }
                    }
                }
                BottomPanel.HINTS -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        if (isAnalyzing) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = IDEColors.accent, strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("🔍 Analysing code...", color = IDEColors.accent, fontSize = 12.sp)
                                }
                            }
                        }
                        // Action buttons
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                ActionChip(label = "✨ Full Review", onClick = onRequestHint)
                                ActionChip(label = "📖 Explain Code", onClick = onExplainCode)
                                errorAnalysis?.let {
                                    if (it.fixedCode.isNotBlank()) {
                                        ActionChip(label = "🔧 Auto Fix", color = IDEColors.green, onClick = onFixWithAI)
                                    }
                                }
                            }
                        }
                        // Inline errors (real-time, no button needed)
                        if (inlineErrors.isNotEmpty()) {
                            item {
                                Text("⚡ Live Issues", color = IDEColors.textSecondary,
                                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 4.dp))
                            }
                            items(inlineErrors) { err ->
                                val (color, icon) = when (err.type) {
                                    com.codelearn.ide.ai.InlineErrorType.ERROR   -> Pair(IDEColors.red, "❌")
                                    com.codelearn.ide.ai.InlineErrorType.WARNING -> Pair(IDEColors.orange, "⚠️")
                                    com.codelearn.ide.ai.InlineErrorType.INFO    -> Pair(IDEColors.blue, "💡")
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color.copy(alpha = 0.08f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(icon, fontSize = 12.sp)
                                    Column {
                                        Text("Line ${err.line + 1}", color = color,
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(err.message, color = IDEColors.textPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                        // Full review hints
                        items(aiHints) { AiHintCard(hint = it) }
                        if (!isAnalyzing && aiHints.isEmpty() && inlineErrors.isEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("✅ No issues found!", color = IDEColors.green,
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Click '✨ Full Review' for deeper analysis",
                                        color = IDEColors.textMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionChip(
    label: String,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color = IDEColors.accent
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BottomTab(label: String, panel: BottomPanel, activePanel: BottomPanel, onClick: (BottomPanel) -> Unit) {
    val isActive = panel == activePanel
    Box(
        modifier = Modifier
            .clickable { onClick(panel) }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = if (isActive) IDEColors.textPrimary else IDEColors.textMuted,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(IDEColors.accent)
            )
        }
    }
}

@Composable
fun NewFileDialog(currentLanguage: Language, onDismiss: () -> Unit, onCreate: (String, Language) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedLang by remember { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IDEColors.bg2,
        title = { Text("New File", color = IDEColors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("File name") },
                    placeholder = { Text("e.g. Utils.kt") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IDEColors.accent,
                        focusedLabelColor = IDEColors.accent,
                        cursorColor = IDEColors.accent,
                        unfocusedTextColor = IDEColors.textPrimary,
                        focusedTextColor = IDEColors.textPrimary,
                        unfocusedLabelColor = IDEColors.textSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = if (name.contains('.')) name
                    else "$name.${selectedLang.fileExtension}"
                    onCreate(finalName, selectedLang)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = IDEColors.textSecondary) }
        }
    )
}
