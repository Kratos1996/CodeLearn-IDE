package com.codelearn.ide.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelearn.ide.model.Language
import com.codelearn.ide.model.Project
import com.codelearn.ide.model.QuizRepository
import com.codelearn.ide.viewmodel.AppViewModel
import com.codelearn.ide.model.QuizProblem
import com.codelearn.ide.course.CourseRepository
import com.codelearn.ide.course.CourseLevel
import kotlin.time.Clock

@Composable
fun HomeScreenContent(
    vm: AppViewModel,
    authVm: com.codelearn.ide.auth.AuthViewModel? = null,
    courseVm: com.codelearn.ide.course.CourseViewModel? = null,
    onOpenIDE: () -> Unit = {},
    onOpenQuiz: () -> Unit = {},
    onOpenCourses: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenChallenge: () -> Unit = {},
    onOpenProblem: ((QuizProblem) -> Unit)? = null,
    onCreateProject: (Language, String) -> Unit = { _, _ -> }
) {
    val projects by vm.projects.collectAsState()
    val currentProject by vm.currentProject.collectAsState()
    val solvedProblems by vm.solvedProblems.collectAsState()
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAllProjects by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    var projectToRename by remember { mutableStateOf<Project?>(null) }
    var renameInput by remember { mutableStateOf("") }
    val authState = authVm?.authState?.collectAsState()?.value
    val currentUser = (authState as? com.codelearn.ide.auth.AuthState.LoggedIn)?.user
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IDEColors.bg0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Header ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(IDEColors.bg3, IDEColors.bg0))
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(IDEColors.accent, IDEColors.blue))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("</> ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "CodeLearn IDE",
                                color = IDEColors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Learn. Code. Master.",
                                color = IDEColors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconChip("📚", "Courses") { onOpenCourses() }
                        IconChip("🧩", "Quiz") { onOpenQuiz() }
                        // Profile avatar — shows initials if logged in, lock if guest
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (currentUser != null) IDEColors.accent
                                    else IDEColors.bg3
                                )
                                .border(
                                    1.dp,
                                    if (currentUser != null) IDEColors.accent else IDEColors.bg4,
                                    androidx.compose.foundation.shape.CircleShape
                                )
                                .clickable { onOpenProfile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                currentUser?.avatarInitials ?: "👤",
                                fontSize = if (currentUser != null) 13.sp else 16.sp,
                                color = if (currentUser != null) Color.White else IDEColors.textMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ─── Stats Row ───────────────────────────────────────────────────
            AnimatedVisibility(visible, enter = fadeIn() + slideInVertically()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🏆",
                        value = "${solvedProblems.size}",
                        label = "Solved",
                        color = IDEColors.green
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📁",
                        value = "${projects.size}",
                        label = "Projects",
                        color = IDEColors.blue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🌐",
                        value = "8",
                        label = "Languages",
                        color = IDEColors.accent
                    )
                }
            }

            // ─── My Projects ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Projects", color = IDEColors.textPrimary,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (projects.size > 3) {
                    TextButton(onClick = { showAllProjects = !showAllProjects }) {
                        Text(if (showAllProjects) "Show Less" else "See All (${projects.size})",
                            color = IDEColors.accent, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(IDEColors.accent)
                        .clickable { showNewProjectDialog = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+ New", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (projects.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IDEColors.bg2)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📁", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No projects yet", color = IDEColors.textSecondary, fontSize = 14.sp)
                        Text("Tap '+ New' to create your first project",
                            color = IDEColors.textMuted, fontSize = 12.sp)
                    }
                }
            } else {
                val displayedProjects = if (showAllProjects) projects else projects.takeLast(3).reversed()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    displayedProjects.forEach { project ->
                        ProjectCard(
                            project = project,
                            isActive = project.id == currentProject?.id,
                            onOpen = {
                                vm.openProject(project, onReady = onOpenIDE)
                            },
                            onDelete = { projectToDelete = project },
                            onRename = { projectToRename = project; renameInput = project.name }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ─── Quick Actions ───────────────────────────────────────────────
            SectionHeader("Quick Start")
            if (currentUser == null) {
                // Guest banner — prompt to sign in
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(IDEColors.accent.copy(alpha = 0.12f))
                        .border(1.dp, IDEColors.accent.copy(0.35f), RoundedCornerShape(14.dp))
                        .clickable { onOpenProfile() }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sign in to unlock everything",
                            color = IDEColors.textPrimary, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold)
                        Text("IDE, Courses, Quiz, Chat and Challenges",
                            color = IDEColors.textSecondary, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(IDEColors.accent)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Sign In", color = Color.Black, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { QuickActionCard("New Project", "🚀", IDEColors.accent,
                    locked = currentUser == null) { showNewProjectDialog = true } }
                item { QuickActionCard("Open IDE",    "⚡", IDEColors.blue,
                    locked = currentUser == null)   { onOpenIDE() } }
                item { QuickActionCard("Practice",    "🧩", IDEColors.green,
                    locked = currentUser == null)  { onOpenQuiz() } }
                item { QuickActionCard("Community",   "💬", IDEColors.cyan,
                    locked = currentUser == null)   { onOpenChat() } }
                item { QuickActionCard("Challenge",   "⚔️", IDEColors.orange,
                    locked = currentUser == null) { onOpenChallenge() } }
            }

            // ─── Recent Problems ──────────────────────────────────────────────
            SectionHeader("Practice Problems")
            val problems = QuizRepository.getProblems().take(3)
            problems.forEach { problem ->
                ProblemPreviewCard(
                    problem = problem,
                    isSolved = solvedProblems.contains(problem.id),
                    onClick = { onOpenProblem?.invoke(problem) ?: vm.selectProblem(problem) }
                )
            }
            if (problems.isNotEmpty()) {
                TextButton(
                    onClick = { onOpenQuiz() },
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text("See All Problems →", color = IDEColors.accent)
                }
            }

            // ─── Start Learning — Course Preview ──────────────────────────────
            SectionHeader("Start Learning")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val langs = listOf(
                    Pair("kt","🟣 Kotlin"), Pair("py","🐍 Python"),
                    Pair("js","🟨 JavaScript"), Pair("java","☕ Java"), Pair("cpp","🔵 C++")
                )
                items(langs) { pair ->
                    val langId = pair.first
                    val label  = pair.second
                    val basic = CourseRepository.getBasicCourse(langId)
                    val pct   = basic?.let { courseVm?.courseCompletionPercent(it) } ?: 0
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(IDEColors.bg2)
                            .border(1.dp, IDEColors.bg4, RoundedCornerShape(14.dp))
                            .clickable { if (currentUser != null) onOpenCourses() else onOpenProfile() }
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(label.substringBefore(" "), fontSize = 28.sp)
                            Text(label.substringAfter(" "), color = IDEColors.textPrimary,
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${basic?.stages?.size ?: 5} stages", color = IDEColors.textMuted, fontSize = 11.sp)
                            if (pct > 0) {
                                LinearProgressIndicator(
                                    progress = { pct / 100f },
                                    modifier = Modifier.fillMaxWidth().height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = IDEColors.accent, trackColor = IDEColors.bg4
                                )
                                Text("$pct%", color = IDEColors.accent, fontSize = 10.sp)
                            } else {
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(IDEColors.accent)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) { Text("Start →", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier.width(120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(IDEColors.accent.copy(alpha = 0.1f))
                            .border(1.dp, IDEColors.accent.copy(0.3f), RoundedCornerShape(14.dp))
                            .clickable { if (currentUser != null) onOpenCourses() else onOpenProfile() }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📚", fontSize = 28.sp)
                            Text(text = "All 10 Languages",
                                color = IDEColors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

            }

            Spacer(Modifier.height(8.dp))

            // ─── Language Cards ───────────────────────────────────────────────
            SectionHeader("Languages")
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(Language.values().toList()) { lang ->
                    LanguageChipCard(lang) {
                        onCreateProject(lang, "${lang.displayName} Project")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ─── New Project Dialog ───────────────────────────────────────────────

        // ─── Delete Confirm Dialog ────────────────────────────────────────────────
        projectToDelete?.let { proj ->
            AlertDialog(
                onDismissRequest = { projectToDelete = null },
                containerColor = IDEColors.bg2,
                titleContentColor = IDEColors.textPrimary,
                textContentColor = IDEColors.textSecondary,
                title = { Text("Delete Project?") },
                text  = {
                    Column {
                        Text("Are you sure you want to delete '${proj.name}'?")
                        Spacer(Modifier.height(4.dp))
                        Text("This cannot be undone.", color = IDEColors.red, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.deleteProject(proj)
                        projectToDelete = null
                    }) { Text("Delete", color = IDEColors.red) }
                },
                dismissButton = {
                    TextButton(onClick = { projectToDelete = null }) {
                        Text("Cancel", color = IDEColors.accent)
                    }
                }
            )
        }

        // ─── Rename Dialog ────────────────────────────────────────────────────────
        projectToRename?.let { proj ->
            AlertDialog(
                onDismissRequest = { projectToRename = null },
                containerColor = IDEColors.bg2,
                titleContentColor = IDEColors.textPrimary,
                title = { Text("Rename Project") },
                text  = {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Project name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IDEColors.accent,
                            unfocusedBorderColor = IDEColors.bg4,
                            focusedTextColor = IDEColors.textPrimary,
                            unfocusedTextColor = IDEColors.textPrimary,
                            cursorColor = IDEColors.accent
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.renameProject(proj, renameInput)
                        projectToRename = null
                    }) { Text("Rename", color = IDEColors.accent) }
                },
                dismissButton = {
                    TextButton(onClick = { projectToRename = null }) {
                        Text("Cancel", color = IDEColors.textMuted)
                    }
                }
            )
        }

        if (showNewProjectDialog) {
            NewProjectDialog(
                onDismiss = { showNewProjectDialog = false },
                onCreate = { name, lang ->
                    onCreateProject(lang, name)
                    showNewProjectDialog = false
                }
            )
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: String, value: String, label: String, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(IDEColors.bg2)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(label, color = IDEColors.textSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = IDEColors.textPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun QuickActionCard(
    title: String, icon: String, color: Color,
    locked: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (locked) IDEColors.bg1 else IDEColors.bg2)
            .border(1.dp,
                if (locked) IDEColors.bg4 else color.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box {
                Text(if (locked) "🔒" else icon, fontSize = 28.sp)
            }
            Text(title,
                color = if (locked) IDEColors.textMuted else IDEColors.textPrimary,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ProblemPreviewCard(
    problem: QuizProblem,
    isSolved: Boolean,
    onClick: () -> Unit
) {
    val diffColor = Color(problem.difficulty.color)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IDEColors.bg2)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSolved) IDEColors.green.copy(0.15f) else IDEColors.bg3),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isSolved) "✅" else "📝", fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(problem.title, color = IDEColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(problem.category, color = IDEColors.textSecondary, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(diffColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(problem.difficulty.label, color = diffColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LanguageChipCard(language: Language, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IDEColors.bg2)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(language.icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            language.displayName,
            color = IDEColors.textSecondary,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IconChip(label: String, icon: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(IDEColors.bg3)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Text(label, color = IDEColors.textPrimary, fontSize = 13.sp)
    }
}

@Composable
fun NewProjectDialog(onDismiss: () -> Unit, onCreate: (String, Language) -> Unit) {
    var name by remember { mutableStateOf("My Project") }
    var selectedLang by remember { mutableStateOf(Language.KOTLIN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IDEColors.bg2,
        title = { Text("New Project", color = IDEColors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
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
                Text("Language", color = IDEColors.textSecondary, fontSize = 13.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Language.values().toList()) { lang ->
                        val isSelected = lang == selectedLang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) IDEColors.accent else IDEColors.bg3)
                                .border(1.dp, if (isSelected) IDEColors.accent else IDEColors.bg4, RoundedCornerShape(10.dp))
                                .clickable { selectedLang = lang }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lang.icon, fontSize = 14.sp)
                                Text(
                                    lang.displayName,
                                    color = if (isSelected) Color.White else IDEColors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, selectedLang) },
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
            ) {
                Text("Create", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = IDEColors.textSecondary)
            }
        }
    )
}

// ─── Project Card ─────────────────────────────────────────────────────────────

@Composable
fun ProjectCard(
    project: com.codelearn.ide.model.Project,
    isActive: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val borderColor = if (isActive) IDEColors.accent else IDEColors.bg4
    val bgColor     = if (isActive) IDEColors.accent.copy(alpha = 0.07f) else IDEColors.bg2

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Language icon circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(project.language.monoColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(project.language.icon, fontSize = 20.sp)
        }

        // Name + meta
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    project.name,
                    color = IDEColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(IDEColors.accent)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("OPEN", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    project.language.displayName,
                    color = project.language.monoColor,
                    fontSize = 11.sp
                )
                Text("·", color = IDEColors.textMuted, fontSize = 11.sp)
                Text(
                    "${project.files.size} file${if (project.files.size != 1) "s" else ""}",
                    color = IDEColors.textMuted,
                    fontSize = 11.sp
                )
                Text("·", color = IDEColors.textMuted, fontSize = 11.sp)
                Text(
                    formatRelativeTime(project.createdAt),
                    color = IDEColors.textMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Open button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) IDEColors.accent else IDEColors.bg3)
                .clickable(onClick = onOpen)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                if (isActive) "Continue" else "Open",
                color = if (isActive) Color.Black else IDEColors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 3-dot menu
        Box {
            Text(
                "⋮",
                color = IDEColors.textMuted,
                fontSize = 18.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { showMenu = true }
                    .padding(4.dp)
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                containerColor = IDEColors.bg2
            ) {
                DropdownMenuItem(
                    text = { Text("✏️  Rename", color = IDEColors.textPrimary, fontSize = 13.sp) },
                    onClick = { showMenu = false; onRename() }
                )
                DropdownMenuItem(
                    text = { Text("🗑  Delete", color = IDEColors.red, fontSize = 13.sp) },
                    onClick = { showMenu = false; onDelete() }
                )
            }
        }
    }
}

// ─── Helper: format relative time ─────────────────────────────────────────────
fun formatRelativeTime(timestamp: Long): String {
    val diff = Clock.System.now().epochSeconds - timestamp
    val minutes = diff / 60_000
    val hours   = diff / 3_600_000
    val days    = diff / 86_400_000
    return when {
        minutes < 2  -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours   < 24 -> "${hours}h ago"
        days    < 7  -> "${days}d ago"
        days    < 30 -> "${days / 7}w ago"
        else         -> "${days / 30}mo ago"
    }
}