package com.codelearn.ide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelearn.ide.model.*
import com.codelearn.ide.syntax.SyntaxHighlighter
import com.codelearn.ide.viewmodel.ConsoleEntry
import com.codelearn.ide.viewmodel.ConsoleType
import com.codelearn.ide.viewmodel.AppViewModel

@Composable
fun QuizListContent(
    vm: AppViewModel,
    onOpenProblem: (com.codelearn.ide.model.QuizProblem) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val problems = QuizRepository.getProblems()
    val solvedProblems by vm.solvedProblems.collectAsState()
    val selectedLanguage by vm.selectedLanguage.collectAsState()
    var filterDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    var filterCategory by remember { mutableStateOf<String?>(null) }

    val categories = problems.map { it.category }.distinct()

    val filtered = problems.filter { p ->
        (filterDifficulty == null || p.difficulty == filterDifficulty) &&
        (filterCategory == null || p.category == filterCategory)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IDEColors.bg0)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(IDEColors.bg3, IDEColors.bg0)))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Practice", color = IDEColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(
                            "${solvedProblems.size}/${problems.size} solved",
                            color = IDEColors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToolbarIconBtn("🏠", "Home") { onBack() }
                        ToolbarIconBtn("⚡", "IDE") { onBack() }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Progress bar
                val progress = if (problems.isEmpty()) 0f else solvedProblems.size.toFloat() / problems.size
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(IDEColors.bg4)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(IDEColors.accent, IDEColors.green)))
                    )
                }
            }
        }

        // Filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip("All", filterDifficulty == null && filterCategory == null) {
                    filterDifficulty = null; filterCategory = null
                }
            }
            items(Difficulty.values().toList()) { diff ->
                FilterChip(
                    label = diff.label,
                    isSelected = filterDifficulty == diff,
                    color = Color(diff.color)
                ) { filterDifficulty = if (filterDifficulty == diff) null else diff }
            }
            items(categories) { cat ->
                FilterChip(cat, filterCategory == cat) {
                    filterCategory = if (filterCategory == cat) null else cat
                }
            }
        }

        // Problems list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { problem ->
                ProblemCard(
                    problem = problem,
                    isSolved = solvedProblems.contains(problem.id),
                    onClick = { onOpenProblem(problem) }
                )
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, color: Color = IDEColors.accent, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color.copy(0.2f) else IDEColors.bg2)
            .border(1.dp, if (isSelected) color else IDEColors.bg4, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (isSelected) color else IDEColors.textSecondary, fontSize = 13.sp)
    }
}

@Composable
fun ProblemCard(problem: QuizProblem, isSolved: Boolean, onClick: () -> Unit) {
    val diffColor = Color(problem.difficulty.color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IDEColors.bg2)
            .border(1.dp, if (isSolved) IDEColors.green.copy(0.2f) else IDEColors.bg4, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSolved) IDEColors.green.copy(0.15f) else IDEColors.bg3),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isSolved) "✅" else "📝", fontSize = 20.sp)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(problem.title, color = IDEColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(problem.category, color = IDEColors.textSecondary, fontSize = 12.sp)
                Text("•", color = IDEColors.textMuted, fontSize = 12.sp)
                Text("${problem.testCases.size} tests", color = IDEColors.textMuted, fontSize = 12.sp)
            }
            // Tags
            if (problem.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 6.dp)) {
                    problem.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IDEColors.bg3)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(tag, color = IDEColors.textMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(diffColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(problem.difficulty.label, color = diffColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Quiz Detail Screen ───────────────────────────────────────────────────────
@Composable
fun QuizDetailContent(
    vm: AppViewModel,
    onBack: () -> Unit = {}
) {
    // Re-enable AI when navigating away from quiz
    DisposableEffect(Unit) { onDispose { vm.setAIEnabled(true) } }
    val problem by vm.selectedProblem.collectAsState()
    val quizCode by vm.quizCode.collectAsState()
    val selectedLanguage by vm.selectedLanguage.collectAsState()
    val submissionResult by vm.submissionResult.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val consoleOutput by vm.consoleOutput.collectAsState()
    val quizTestResults by vm.quizTestResults.collectAsState()
    var showHints by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0=description, 1=submission

    val p = problem ?: return

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(IDEColors.bg0)
    ) {
        // ─── Left: Problem Panel ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
                .background(IDEColors.bg1)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(IDEColors.bg0)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolbarIconBtn("←", "Back") { onBack() }
                Text(p.title, color = IDEColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(p.difficulty.color).copy(0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(p.difficulty.label, color = Color(p.difficulty.color), fontSize = 11.sp)
                }
            }

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth().background(IDEColors.bg1).padding(horizontal = 16.dp)
            ) {
                listOf("Description", "Submissions").forEachIndexed { i, tab ->
                    Box(
                        modifier = Modifier
                            .clickable { activeTab = i }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                    ) {
                        Column {
                            Text(tab, color = if (activeTab == i) IDEColors.textPrimary else IDEColors.textMuted, fontSize = 13.sp)
                            if (activeTab == i) {
                                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(IDEColors.accent))
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }
            }

            HorizontalDivider(color = IDEColors.bg4)

            // Content
            LazyColumn(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTab == 0) {
                    item {
                        // Description
                        Text(p.description, color = IDEColors.textPrimary, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                    item {
                        // Examples
                        Text("Examples", color = IDEColors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        p.examples.forEachIndexed { i, example ->
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IDEColors.bg2)
                                    .padding(12.dp)
                            ) {
                                Text("Example ${i + 1}", color = IDEColors.textSecondary, fontSize = 11.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Input: ", color = IDEColors.textMuted, fontSize = 12.sp)
                                Text(example.input, color = IDEColors.cyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Spacer(Modifier.height(4.dp))
                                Text("Output: ", color = IDEColors.textMuted, fontSize = 12.sp)
                                Text(example.output, color = IDEColors.green, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                if (example.explanation.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(example.explanation, color = IDEColors.textSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    item {
                        // Constraints
                        Text("Constraints", color = IDEColors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        p.constraints.forEach { constraint ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = IDEColors.textMuted, fontSize = 12.sp)
                                Text(constraint, color = IDEColors.textSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    if (showHints) {
                        item {
                            Text("Hints", color = IDEColors.orange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            p.hints.forEachIndexed { i, hint ->
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(IDEColors.orange.copy(0.08f))
                                        .border(1.dp, IDEColors.orange.copy(0.2f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text("Hint ${i + 1}: ", color = IDEColors.orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(hint, color = IDEColors.textSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        submissionResult?.let { result ->
                            SubmissionResultCard(result)
                        } ?: Text("No submissions yet. Submit your solution!", color = IDEColors.textMuted, fontSize = 13.sp)
                    }
                }
            }

            // Hint button
            TextButton(
                onClick = { showHints = !showHints },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    if (showHints) "Hide Hints" else "💡 Show Hints",
                    color = IDEColors.orange,
                    fontSize = 13.sp
                )
            }
        }

        // ─── Right: Code Editor ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
                .background(IDEColors.bg2)
        ) {
            // Editor toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(IDEColors.bg1)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language picker
                var showLangPicker by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IDEColors.bg3)
                            .clickable { showLangPicker = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(selectedLanguage.icon, fontSize = 13.sp)
                        Text(selectedLanguage.displayName, color = IDEColors.textPrimary, fontSize = 12.sp)
                        Text("▾", color = IDEColors.textSecondary, fontSize = 10.sp)
                    }
                    DropdownMenu(
                        expanded = showLangPicker,
                        onDismissRequest = { showLangPicker = false },
                        modifier = Modifier.background(IDEColors.bg3)
                    ) {
                        Language.values().forEach { lang ->
                            if (p.starterCode.containsKey(lang.fileExtension)) {
                                DropdownMenuItem(
                                    text = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(lang.icon)
                                            Text(lang.displayName, color = IDEColors.textPrimary)
                                        }
                                    },
                                    onClick = {
                                        vm.setQuizLanguage(lang)
                                        vm.updateQuizCode(p.starterCode[lang.fileExtension] ?: "")
                                        showLangPicker = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Run Tests
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IDEColors.bg3)
                        .border(1.dp, IDEColors.accent.copy(0.4f), RoundedCornerShape(6.dp))
                        .clickable(enabled = !isRunning) { vm.runQuizTests() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("▷ Run", color = IDEColors.accent, fontSize = 13.sp)
                }

                // Submit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IDEColors.green)
                        .clickable(enabled = !isRunning) { vm.submitQuizSolution() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Submit", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Code editor area
            BasicCodeEditor(
                code = quizCode,
                language = selectedLanguage,
                onCodeChange = { vm.updateQuizCode(it) },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            // Output / Result
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(IDEColors.bg1)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Output",
                        color = IDEColors.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        submissionResult?.let { result ->
                            val statusColor = when (result.status) {
                                SubmissionStatus.ACCEPTED -> IDEColors.green
                                SubmissionStatus.WRONG_ANSWER -> IDEColors.red
                                else -> IDEColors.orange
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    when (result.status) {
                                        SubmissionStatus.ACCEPTED -> "✅ Accepted"
                                        SubmissionStatus.WRONG_ANSWER -> "❌ Wrong Answer"
                                        SubmissionStatus.TIME_LIMIT -> "⏱ Time Limit"
                                        SubmissionStatus.RUNTIME_ERROR -> "💥 Runtime Error"
                                        else -> "⚠️ Error"
                                    },
                                    color = statusColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${result.passedTests}/${result.totalTests} tests passed",
                                    color = IDEColors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (result.status == SubmissionStatus.ACCEPTED) {
                                Text("Runtime: ${result.runtime}ms • Memory: ${result.memory}MB", color = IDEColors.textMuted, fontSize = 11.sp)
                            }
                        } ?: consoleOutput.forEach { entry ->
                            val c = when (entry.type) {
                                ConsoleType.SUCCESS -> IDEColors.green
                                ConsoleType.ERROR -> IDEColors.red
                                ConsoleType.WARNING -> IDEColors.orange
                                ConsoleType.MUTED -> IDEColors.textMuted
                                ConsoleType.INFO -> IDEColors.blue
                                else -> IDEColors.textSecondary
                            }
                            Text(entry.text, color = c, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionResultCard(result: SubmissionResult) {
    val (color, icon, label) = when (result.status) {
        SubmissionStatus.ACCEPTED -> Triple(IDEColors.green, "✅", "Accepted")
        SubmissionStatus.WRONG_ANSWER -> Triple(IDEColors.red, "❌", "Wrong Answer")
        SubmissionStatus.TIME_LIMIT -> Triple(IDEColors.orange, "⏱", "Time Limit Exceeded")
        SubmissionStatus.RUNTIME_ERROR -> Triple(IDEColors.red, "💥", "Runtime Error")
        else -> Triple(IDEColors.orange, "⚠️", "Error")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(icon, fontSize = 24.sp)
            Column {
                Text(label, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${result.passedTests}/${result.totalTests} test cases passed", color = IDEColors.textSecondary, fontSize = 13.sp)
            }
        }
        if (result.status == SubmissionStatus.ACCEPTED) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column {
                    Text("Runtime", color = IDEColors.textMuted, fontSize = 11.sp)
                    Text("${result.runtime} ms", color = IDEColors.green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Memory", color = IDEColors.textMuted, fontSize = 11.sp)
                    Text("${result.memory} MB", color = IDEColors.blue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BasicCodeEditor(code: String, language: Language, onCodeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val highlighted = remember(code, language) {
        SyntaxHighlighter.highlight(code, language)
    }

    Box(modifier = modifier.background(IDEColors.bg2)) {
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            textStyle = TextStyle(
                color = IDEColors.textPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 21.sp
            ),
            cursorBrush = SolidColor(IDEColors.accent),
            visualTransformation = {
                TransformedText(highlighted, OffsetMapping.Identity)
            }
        )
    }
}
