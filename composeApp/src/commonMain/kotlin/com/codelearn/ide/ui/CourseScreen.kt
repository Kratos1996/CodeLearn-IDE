package com.codelearn.ide.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.course.*

// ══════════════════════════════════════════════════════════════════════════════
// COURSE HOME — language picker showing Basic + Advanced for all 10 languages
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CourseHomeScreen(
    courseVm: CourseViewModel,
    authVm: AuthViewModel,
    onBack: () -> Unit = {}
) {
    val allProgress by courseVm.allProgress.collectAsState()
    val user by authVm.authState.collectAsState()

    val languages = listOf(
        Triple("kt",   "🟣", "Kotlin"),
        Triple("py",   "🐍", "Python"),
        Triple("js",   "🟨", "JavaScript"),
        Triple("java", "☕", "Java"),
        Triple("cpp",  "🔵", "C++"),
        Triple("c",    "🔷", "C"),
        Triple("cs",   "💜", "C#"),
        Triple("rb",   "🔴", "Ruby"),
        Triple("dart", "🎯", "Dart"),
        Triple("vb",   "🟦", "Visual Basic")
    )

    Column(
        modifier = Modifier.fillMaxSize().background(IDEColors.bg0)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(4.dp))
            Spacer(Modifier.width(12.dp))
            Text("📚 Courses", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = IDEColors.textPrimary, modifier = Modifier.weight(1f))
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Choose a Language", fontSize = 15.sp,
                    color = IDEColors.textSecondary, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp))
            }

            items(languages) { (langId, icon, name) ->
                val basic    = CourseRepository.getBasicCourse(langId) ?: return@items
                val advanced = CourseRepository.getAdvancedCourse(langId) ?: return@items
                LanguageCourseCard(
                    langIcon     = icon,
                    langName     = name,
                    basic        = basic,
                    advanced     = advanced,
                    courseVm     = courseVm,
                    basicPct     = courseVm.courseCompletionPercent(basic),
                    advancedPct  = courseVm.courseCompletionPercent(advanced),
                    advLocked    = !courseVm.isCourseUnlocked(advanced)
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun LanguageCourseCard(
    langIcon: String, langName: String,
    basic: Course, advanced: Course,
    courseVm: CourseViewModel,
    basicPct: Int, advancedPct: Int,
    advLocked: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val color = Color(basic.color)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IDEColors.bg2)
            .border(1.dp, IDEColors.bg4, RoundedCornerShape(16.dp))
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(langIcon, fontSize = 24.sp) }

            Column(modifier = Modifier.weight(1f)) {
                Text(langName, color = IDEColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgressPill("Basic", basicPct, IDEColors.green)
                    ProgressPill("Adv", if (advLocked) -1 else advancedPct, IDEColors.accent)
                }
            }
            Text(if (expanded) "▲" else "▼", color = IDEColors.textMuted, fontSize = 14.sp)
        }

        // Expanded: show Basic + Advanced course rows
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CourseLevelRow(
                    course  = basic,
                    pct     = basicPct,
                    locked  = false,
                    color   = IDEColors.green,
                    label   = "🌱 Basic Course",
                    onClick = { courseVm.openCourse(basic) }
                )
                CourseLevelRow(
                    course  = advanced,
                    pct     = advancedPct,
                    locked  = advLocked,
                    color   = IDEColors.accent,
                    label   = "🚀 Advanced Course",
                    onClick = { if (!advLocked) courseVm.openCourse(advanced) }
                )
            }
        }
    }
}

@Composable
private fun CourseLevelRow(
    course: Course, pct: Int, locked: Boolean,
    color: Color, label: String, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (locked) IDEColors.bg1 else IDEColors.bg3)
            .clickable(enabled = !locked, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(if (locked) "🔒" else if (pct == 100) "✅" else course.icon, fontSize = 18.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = if (locked) IDEColors.textMuted else IDEColors.textPrimary,
                fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("${course.stages.size} stages", color = IDEColors.textMuted, fontSize = 11.sp)
        }
        if (!locked) {
            Column(horizontalAlignment = Alignment.End) {
                Text("$pct%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier.width(60.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = color, trackColor = IDEColors.bg4
                )
            }
        } else {
            Text("Complete Basic first", color = IDEColors.textMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ProgressPill(label: String, pct: Int, color: Color) {
    val text = if (pct < 0) "🔒" else if (pct == 100) "✅" else "$pct%"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = if (pct < 0) 0.05f else 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text("$label $text", color = if (pct < 0) IDEColors.textMuted else color,
            fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// STAGE LIST SCREEN — shows all stages in a course with lock/unlock status
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun StageListScreen(
    course: Course,
    courseVm: CourseViewModel,
    onBack: () -> Unit = {}
) {
    val allProgress by courseVm.allProgress.collectAsState()
    val pct = courseVm.courseCompletionPercent(course)
    val color = Color(course.color)

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), IDEColors.bg0)))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                        modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
                    Text(course.icon, fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(course.title, color = IDEColors.textPrimary, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                        Text(if (course.level == CourseLevel.BASIC) "Basic Course" else "Advanced Course",
                            color = color, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Overall progress bar
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = color, trackColor = IDEColors.bg3
                    )
                    Text("$pct%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text("${course.stages.size} stages • ${course.stages.sumOf { it.lessons.size }} lessons",
                    color = IDEColors.textMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Stage list — vertical timeline
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(course.stages) { index, stage ->
                val locked    = courseVm.isStageLocked(course, stage)
                val completed = courseVm.isStageCompleted(course.id, stage.id)
                val score     = courseVm.getStageScore(course.id, stage.id)

                StageTimelineItem(
                    stage     = stage,
                    index     = index,
                    isFirst   = index == 0,
                    isLast    = index == course.stages.lastIndex,
                    locked    = locked,
                    completed = completed,
                    score     = score,
                    color     = color,
                    onClick   = { if (!locked) courseVm.openStage(stage) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StageTimelineItem(
    stage: CourseStage, index: Int,
    isFirst: Boolean, isLast: Boolean,
    locked: Boolean, completed: Boolean,
    score: Int?, color: Color, onClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline line + dot
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)) {
            if (!isFirst) Box(modifier = Modifier.width(2.dp).height(16.dp)
                .background(if (completed) color else IDEColors.bg4))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            completed -> color
                            locked    -> IDEColors.bg3
                            else      -> color.copy(alpha = 0.2f)
                        }
                    )
                    .border(2.dp, if (locked) IDEColors.bg4 else color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        completed -> "✓"
                        locked    -> "🔒"
                        else      -> stage.icon
                    },
                    fontSize = if (completed || locked) 14.sp else 16.sp,
                    color = if (completed) Color.White else IDEColors.textMuted
                )
            }
            if (!isLast) Box(modifier = Modifier.width(2.dp).height(16.dp)
                .background(if (completed) color else IDEColors.bg4))
        }

        Spacer(Modifier.width(12.dp))

        // Stage card
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (locked) IDEColors.bg1 else IDEColors.bg2)
                .border(1.dp,
                    if (completed) color.copy(alpha = 0.4f)
                    else IDEColors.bg4,
                    RoundedCornerShape(12.dp))
                .clickable(enabled = !locked, onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Stage ${index + 1}: ${stage.name}",
                    color = if (locked) IDEColors.textMuted else IDEColors.textPrimary,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(stage.description,
                    color = IDEColors.textMuted, fontSize = 12.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip("${stage.lessons.size} lessons", IDEColors.blue)
                    InfoChip("${stage.examQuestions.size} exam Qs",
                        if (completed) IDEColors.green else IDEColors.orange)
                    score?.let { InfoChip("$it%", if (it >= 80) IDEColors.green else IDEColors.orange) }
                }
            }
            if (!locked) {
                Text(if (completed) "Review" else "Start", color = color,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) { Text(text, color = color, fontSize = 10.sp) }
}

// ══════════════════════════════════════════════════════════════════════════════
// LESSON SCREEN — shows lesson cards one by one
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LessonScreen(
    stage: CourseStage,
    lessonIndex: Int,
    courseVm: CourseViewModel,
    onBack: () -> Unit = {}
) {
    val lesson = stage.lessons.getOrNull(lessonIndex) ?: return
    val progress = "${lessonIndex + 1} / ${stage.lessons.size}"
    val progressFraction = (lessonIndex + 1f) / stage.lessons.size

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stage.name, color = IDEColors.textPrimary, fontSize = 15.sp,
                    fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = IDEColors.accent, trackColor = IDEColors.bg4
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(progress, color = IDEColors.textMuted, fontSize = 12.sp)
        }

        // Lesson content
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (lesson.type) {
                LessonType.TEXT -> {
                    Text(lesson.title, color = IDEColors.textPrimary, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Text(lesson.body, color = IDEColors.textSecondary, fontSize = 15.sp,
                        lineHeight = 24.sp)
                }
                LessonType.CODE -> {
                    Text(lesson.title, color = IDEColors.textPrimary, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold)
                    if (lesson.body.isNotBlank())
                        Text(lesson.body, color = IDEColors.textSecondary, fontSize = 14.sp)
                    // Code block
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(IDEColors.bg2)
                            .border(1.dp, IDEColors.bg4, RoundedCornerShape(12.dp))
                    ) {
                        Column {
                            // Language label bar
                            Row(
                                modifier = Modifier.fillMaxWidth().background(IDEColors.bg3)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lesson.language.uppercase(), color = IDEColors.accent,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("example", color = IDEColors.textMuted, fontSize = 11.sp)
                            }
                            Text(
                                lesson.code,
                                modifier = Modifier.padding(16.dp),
                                color = IDEColors.textPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                LessonType.TIP -> {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(IDEColors.accent.copy(alpha = 0.1f))
                            .border(1.dp, IDEColors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💡", fontSize = 20.sp)
                        Column {
                            Text(lesson.title, color = IDEColors.accent, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold)
                            Text(lesson.body, color = IDEColors.textSecondary, fontSize = 14.sp,
                                lineHeight = 22.sp)
                        }
                    }
                }
                LessonType.WARNING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(IDEColors.orange.copy(alpha = 0.1f))
                            .border(1.dp, IDEColors.orange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Column {
                            Text(lesson.title, color = IDEColors.orange, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold)
                            Text(lesson.body, color = IDEColors.textSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (lessonIndex > 0) {
                OutlinedButton(
                    onClick = { courseVm.prevLesson() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IDEColors.bg4)
                ) { Text("← Back", color = IDEColors.textSecondary) }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = { courseVm.nextLesson() },
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
            ) {
                val isLast = lessonIndex == stage.lessons.lastIndex
                Text(
                    if (isLast) "Take Exam →" else "Next →",
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EXAM SCREEN — one question at a time with explanation
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ExamScreen(
    stage: CourseStage,
    courseVm: CourseViewModel,
    onBack: () -> Unit = {}
) {
    val questionIndex  by courseVm.examQuestionIndex.collectAsState()
    val selectedOption by courseVm.selectedOptionIndex.collectAsState()
    val showExplain    by courseVm.showExplanation.collectAsState()
    val examResult     by courseVm.examResult.collectAsState()

    // Show result screen when exam is done
    if (examResult != null) {
        ExamResultScreen(
            result   = examResult!!,
            courseVm = courseVm,
            onBack   = onBack
        )
        return
    }

    val question = stage.examQuestions.getOrNull(questionIndex) ?: return
    val total    = stage.examQuestions.size
    val progress = (questionIndex + 1f) / total

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Exam: ${stage.name}", color = IDEColors.textPrimary, fontSize = 16.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${questionIndex + 1}/$total", color = IDEColors.textMuted, fontSize = 14.sp)
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = IDEColors.orange, trackColor = IDEColors.bg3
        )

        Column(
            modifier = Modifier.weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Question ${questionIndex + 1}", color = IDEColors.textMuted, fontSize = 12.sp)
            Text(question.question, color = IDEColors.textPrimary, fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold, lineHeight = 26.sp)

            Spacer(Modifier.height(8.dp))

            // Answer options
            question.options.forEachIndexed { idx, option ->
                val isSelected = selectedOption == idx
                val isCorrect  = idx == question.correctIndex
                val bgColor = when {
                    !showExplain -> if (isSelected) IDEColors.accent.copy(alpha = 0.2f) else IDEColors.bg2
                    isCorrect    -> IDEColors.green.copy(alpha = 0.15f)
                    isSelected   -> IDEColors.red.copy(alpha = 0.15f)
                    else         -> IDEColors.bg2
                }
                val borderColor = when {
                    !showExplain -> if (isSelected) IDEColors.accent else IDEColors.bg4
                    isCorrect    -> IDEColors.green
                    isSelected   -> IDEColors.red
                    else         -> IDEColors.bg4
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !showExplain) { courseVm.selectExamOption(idx) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Option letter circle
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when {
                                showExplain && isCorrect -> "✓"
                                showExplain && isSelected && !isCorrect -> "✗"
                                else -> ('A' + idx).toString()
                            },
                            color = borderColor,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Text(option, color = IDEColors.textPrimary, fontSize = 14.sp,
                        modifier = Modifier.weight(1f))
                }
            }

            // Explanation card (shown after answering)
            AnimatedVisibility(visible = showExplain) {
                val isRight = selectedOption == question.correctIndex
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isRight) IDEColors.green.copy(alpha = 0.1f)
                            else IDEColors.red.copy(alpha = 0.1f)
                        )
                        .border(
                            1.dp,
                            if (isRight) IDEColors.green.copy(0.3f) else IDEColors.red.copy(0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(if (isRight) "🎉" else "💡", fontSize = 18.sp)
                    Column {
                        Text(
                            if (isRight) "Correct!" else "Not quite",
                            color = if (isRight) IDEColors.green else IDEColors.red,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                        Text(question.explanation, color = IDEColors.textSecondary,
                            fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
            }
        }

        // Next button (only visible after selecting)
        AnimatedVisibility(visible = showExplain) {
            Button(
                onClick = { courseVm.nextExamQuestion() },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
            ) {
                Text(
                    if (questionIndex == stage.examQuestions.lastIndex) "Finish Exam" else "Next Question →",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EXAM RESULT SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ExamResultScreen(
    result: ExamResult,
    courseVm: CourseViewModel,
    onBack: () -> Unit = {}
) {
    val xpGained by courseVm.xpGained.collectAsState()
    val newBadge by courseVm.newBadge.collectAsState()
    val newCert  by courseVm.newCertificate.collectAsState()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        courseVm.clearXPGained()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(IDEColors.bg0)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        // Result badge
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape)
                .background(
                    if (result.passed) IDEColors.green.copy(alpha = 0.15f)
                    else IDEColors.red.copy(alpha = 0.15f)
                )
                .border(3.dp, if (result.passed) IDEColors.green else IDEColors.red, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (result.passed) "🏆" else "📖", fontSize = 40.sp)
                Text("${result.score}%",
                    color = if (result.passed) IDEColors.green else IDEColors.red,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(if (result.passed) "Stage Passed! 🎉" else "Keep Trying!",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IDEColors.textPrimary)
        Text("${result.stageName}",
            fontSize = 15.sp, color = IDEColors.textSecondary)

        Spacer(Modifier.height(24.dp))

        // Stats
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatBubble("Correct", "${result.correct}/${result.total}", IDEColors.green)
            StatBubble("Score", "${result.score}%",
                if (result.passed) IDEColors.green else IDEColors.red)
            StatBubble("Pass mark", "70%", IDEColors.orange)
        }

        // XP gained
        if (xpGained > 0) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(IDEColors.accent.copy(alpha = 0.15f))
                    .border(1.dp, IDEColors.accent.copy(0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("⭐ +$xpGained XP earned!", color = IDEColors.accent,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Buttons
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (result.passed) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IDEColors.green)
                ) {
                    Text("Continue to next stage →", color = Color.Black,
                        fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { courseVm.retryExam() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
                ) {
                    Text("Retry Exam", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IDEColors.bg4)
                ) { Text("Review Lessons", color = IDEColors.textSecondary) }
            }
        }
    }

    // ── Badge earned dialog ────────────────────────────────────────────────────
    newBadge?.let { badge ->
        BadgeEarnedDialog(badge = badge, onDismiss = { courseVm.dismissBadge() })
    }

    // ── Certificate earned dialog ──────────────────────────────────────────────
    newCert?.let { certTitle ->
        CertificateEarnedDialog(title = certTitle, onDismiss = { courseVm.dismissCertificate() })
    }
}

@Composable
private fun StatBubble(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IDEColors.bg2)
            .padding(16.dp)
    ) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = IDEColors.textMuted, fontSize = 11.sp)
    }
}

// ── Badge earned dialog ────────────────────────────────────────────────────────

@Composable
fun BadgeEarnedDialog(badge: BadgeInfo, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = IDEColors.bg2,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🎉 Badge Earned!", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = IDEColors.textPrimary)
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(Color(badge.color).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(badge.icon, fontSize = 40.sp) }
                Text(badge.name, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = Color(badge.color))
                Text(badge.description, color = IDEColors.textSecondary, fontSize = 14.sp,
                    textAlign = TextAlign.Center)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Awesome! 🙌", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// ── Certificate earned dialog ──────────────────────────────────────────────────

@Composable
fun CertificateEarnedDialog(title: String, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = IDEColors.bg2,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🏅 Certificate Earned!", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = IDEColors.textPrimary)
                Text("📜", fontSize = 60.sp)
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = IDEColors.green, textAlign = TextAlign.Center)
                Text("Your certificate has been added to your profile.\nYou can view and download it anytime.",
                    color = IDEColors.textSecondary, fontSize = 13.sp,
                    textAlign = TextAlign.Center)
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = IDEColors.green),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("View Profile", color = Color.Black, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
