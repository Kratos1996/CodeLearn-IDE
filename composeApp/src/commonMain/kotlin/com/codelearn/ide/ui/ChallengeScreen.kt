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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.course.CourseRepository
import com.codelearn.ide.course.ExamQuestion
import com.codelearn.ide.course.XPRewards
import com.codelearn.ide.firebase.ChallengeRecord
import com.codelearn.ide.firebase.FirebaseService
import com.codelearn.ide.firebase.LeaderboardEntry
import com.codelearn.ide.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── Challenge ViewModel ──────────────────────────────────────────────────────

class ChallengeViewModel(private val authVm: AuthViewModel) : ScreenModel {

    private val _phase = MutableStateFlow<ChallengePhase>(ChallengePhase.Setup)
    val phase: StateFlow<ChallengePhase> = _phase

    private val _questions       = MutableStateFlow<List<ExamQuestion>>(emptyList())
    val questions: StateFlow<List<ExamQuestion>> = _questions

    private val _questionIndex   = MutableStateFlow(0)
    val questionIndex: StateFlow<Int> = _questionIndex

    private val _myScore         = MutableStateFlow(0)
    val myScore: StateFlow<Int> = _myScore

    private val _opponentScore   = MutableStateFlow(0)    // simulated for offline mode
    val opponentScore: StateFlow<Int> = _opponentScore

    private val _selectedOption  = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption

    private val _showExplanation = MutableStateFlow(false)
    val showExplanation: StateFlow<Boolean> = _showExplanation

    private val _leaderboard     = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard

    private val _selectedLang    = MutableStateFlow(Language.KOTLIN)
    val selectedLang: StateFlow<Language> = _selectedLang

    private val _opponentName    = MutableStateFlow("Bot")
    val opponentName: StateFlow<String> = _opponentName

    fun selectLanguage(lang: Language) { _selectedLang.value = lang }

    fun startChallenge(opponentName: String = "Bot") {
        val lang     = _selectedLang.value
        val courseId = "${lang.fileExtension}_basic"
        val course   = CourseRepository.getBasicCourse(lang.fileExtension)

        // Collect all exam questions from the course stages, shuffle, take 5
        val allQuestions = course?.stages?.flatMap { it.examQuestions }
            ?: emptyList()
        val selected = allQuestions.shuffled().take(5)

        if (selected.isEmpty()) {
            _phase.value = ChallengePhase.NoContent
            return
        }

        _questions.value = selected
        _questionIndex.value = 0
        _myScore.value = 0
        _opponentScore.value = 0
        _selectedOption.value = null
        _showExplanation.value = false
        _opponentName.value = opponentName
        _phase.value = ChallengePhase.InProgress
    }

    fun selectOption(idx: Int) {
        if (_showExplanation.value) return
        _selectedOption.value = idx
        _showExplanation.value = true

        val question = _questions.value.getOrNull(_questionIndex.value) ?: return
        if (idx == question.correctIndex) _myScore.value++

        // Simulate opponent answering (60% accuracy for bot)
        val opponentCorrect = (Math.random() < 0.60)
        if (opponentCorrect) _opponentScore.value++
    }

    fun nextQuestion() {
        val next = _questionIndex.value + 1
        if (next < _questions.value.size) {
            _questionIndex.value = next
            _selectedOption.value = null
            _showExplanation.value = false
        } else {
            finishChallenge()
        }
    }

    private fun finishChallenge() {
        val myScore  = _myScore.value
        val oppScore = _opponentScore.value
        val total    = _questions.value.size
        val won      = myScore > oppScore
        _phase.value = ChallengePhase.Result(myScore, oppScore, total, won)

        // Award XP
        val user  = authVm.currentUser ?: return
        val xp    = if (won) XPRewards.CHALLENGE_WIN else XPRewards.CHALLENGE_PLAY
        val newXP = user.xp + xp
        authVm.updateLocalProfile(user.copy(xp = newXP,
            level = XPRewards.xpToLevel(newXP)))

        // Save to Firebase
        val token = authVm.idToken
        val uid   = authVm.currentUid
        if (token.isNotBlank()) {
            screenModelScope.launch {
                val record = ChallengeRecord(
                    challengerId   = uid,
                    challengerName = user.name,
                    opponentId     = "bot",
                    opponentName   = _opponentName.value,
                    language       = _selectedLang.value.displayName,
                    challengerScore = myScore,
                    opponentScore  = oppScore,
                    winnerId       = if (won) uid else "bot",
                    ts             = System.currentTimeMillis()
                )
                FirebaseService.postChallenge(token, record)
            }
        }
    }

    fun loadLeaderboard() {
        val token = authVm.idToken
        if (token.isBlank()) return
        screenModelScope.launch {
            val entries = FirebaseService.loadLeaderboard(token)
            _leaderboard.value = entries
        }
    }

    fun resetChallenge() { _phase.value = ChallengePhase.Setup }
}

sealed class ChallengePhase {
    object Setup      : ChallengePhase()
    object InProgress : ChallengePhase()
    object NoContent  : ChallengePhase()
    data class Result(val myScore: Int, val oppScore: Int, val total: Int, val won: Boolean) : ChallengePhase()
}

// ─── Challenge Screen ─────────────────────────────────────────────────────────

@Composable
fun ChallengeScreen(
    challengeVm: ChallengeViewModel,
    authVm: AuthViewModel,
    onBack: () -> Unit = {}
) {
    val phase by challengeVm.phase.collectAsState()

    when (val p = phase) {
        is ChallengePhase.Setup      -> ChallengeSetupScreen(challengeVm, onBack)
        is ChallengePhase.InProgress -> ChallengeQuizScreen(challengeVm, onBack)
        is ChallengePhase.Result     -> ChallengeResultScreen(p, challengeVm, onBack)
        is ChallengePhase.NoContent  -> {
            Box(modifier = Modifier.fillMaxSize().background(IDEColors.bg0),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No questions available yet", color = IDEColors.textSecondary)
                    TextButton(onClick = { challengeVm.resetChallenge() }) { Text("Go Back") }
                }
            }
        }
    }
}

@Composable
private fun ChallengeSetupScreen(
    challengeVm: ChallengeViewModel,
    onBack: () -> Unit
) {
    val selectedLang by challengeVm.selectedLang.collectAsState()
    val leaderboard  by challengeVm.leaderboard.collectAsState()

    LaunchedEffect(Unit) { challengeVm.loadLeaderboard() }

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
            Text("⚔️ Challenge", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = IDEColors.textPrimary, modifier = Modifier.weight(1f))
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Challenge banner
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(IDEColors.accent.copy(alpha = 0.15f))
                        .border(1.dp, IDEColors.accent.copy(0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("⚔️", fontSize = 40.sp)
                        Text("Coding Challenge", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = IDEColors.textPrimary)
                        Text("Answer 5 questions faster and more accurately\nthan your opponent!",
                            color = IDEColors.textSecondary, fontSize = 13.sp,
                            textAlign = TextAlign.Center)
                    }
                }
            }

            item { Text("Choose Language", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = IDEColors.textSecondary) }

            // Language grid
            item {
                val langs = Language.values().toList()
                val rows = langs.chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            row.forEach { lang ->
                                val isSelected = lang == selectedLang
                                Box(
                                    modifier = Modifier.weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) lang.monoColor.copy(alpha = 0.25f)
                                            else IDEColors.bg2
                                        )
                                        .border(
                                            1.5.dp,
                                            if (isSelected) lang.monoColor else IDEColors.bg4,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { challengeVm.selectLanguage(lang) }
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(lang.icon, fontSize = 18.sp)
                                        Text(lang.displayName.take(6),
                                            color = if (isSelected) lang.monoColor else IDEColors.textMuted,
                                            fontSize = 9.sp, fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            // Fill remaining cells
                            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { challengeVm.startChallenge("Bot") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
                ) {
                    Text("⚔️  Start Challenge vs Bot",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Leaderboard
            if (leaderboard.isNotEmpty()) {
                item {
                    Text("🏆 Leaderboard", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                        color = IDEColors.textSecondary)
                }
                itemsIndexed(leaderboard.take(10)) { idx, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(IDEColors.bg2)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            when(idx) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "#${idx+1}" },
                            fontSize = if(idx < 3) 18.sp else 13.sp,
                            color = IDEColors.textMuted
                        )
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(IDEColors.accent.copy(0.3f)),
                            contentAlignment = Alignment.Center) {
                            Text(entry.initials, color = IDEColors.accent,
                                fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.name, color = IDEColors.textPrimary, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium)
                            Text("Level ${entry.level}", color = IDEColors.textMuted, fontSize = 11.sp)
                        }
                        Text("${entry.xp} XP", color = IDEColors.accent,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun ChallengeQuizScreen(
    challengeVm: ChallengeViewModel,
    onBack: () -> Unit
) {
    val questions    by challengeVm.questions.collectAsState()
    val qIdx         by challengeVm.questionIndex.collectAsState()
    val myScore      by challengeVm.myScore.collectAsState()
    val oppScore     by challengeVm.opponentScore.collectAsState()
    val selectedOpt  by challengeVm.selectedOption.collectAsState()
    val showExplain  by challengeVm.showExplanation.collectAsState()
    val opponentName by challengeVm.opponentName.collectAsState()

    val question = questions.getOrNull(qIdx) ?: return
    val total    = questions.size

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        // Score header
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // My score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(IDEColors.accent), contentAlignment = Alignment.Center) {
                    Text("ME", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text("$myScore", color = IDEColors.accent, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔️", fontSize = 24.sp)
                Text("Q ${qIdx+1} / $total", color = IDEColors.textMuted, fontSize = 12.sp)
                LinearProgressIndicator(
                    progress = { (qIdx + 1f) / total },
                    modifier = Modifier.width(80.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = IDEColors.orange, trackColor = IDEColors.bg3
                )
            }

            // Opponent score
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(IDEColors.red.copy(0.7f)), contentAlignment = Alignment.Center) {
                    Text("🤖", fontSize = 18.sp)
                }
                Text("$oppScore", color = IDEColors.red, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(question.question, color = IDEColors.textPrimary, fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold, lineHeight = 26.sp)

            question.options.forEachIndexed { idx, option ->
                val isSelected = selectedOpt == idx
                val isCorrect  = idx == question.correctIndex
                val bgColor = when {
                    !showExplain -> if (isSelected) IDEColors.accent.copy(0.2f) else IDEColors.bg2
                    isCorrect    -> IDEColors.green.copy(0.15f)
                    isSelected   -> IDEColors.red.copy(0.15f)
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
                        .clickable(enabled = !showExplain) { challengeVm.selectOption(idx) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.size(30.dp).clip(CircleShape)
                        .background(borderColor.copy(0.2f)),
                        contentAlignment = Alignment.Center) {
                        Text(
                            when { showExplain && isCorrect -> "✓"; showExplain && isSelected && !isCorrect -> "✗"; else -> ('A'+idx).toString() },
                            color = borderColor, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Text(option, color = IDEColors.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                }
            }

            AnimatedVisibility(visible = showExplain) {
                val isRight = selectedOpt == question.correctIndex
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isRight) IDEColors.green.copy(0.1f) else IDEColors.red.copy(0.1f))
                        .border(1.dp, if (isRight) IDEColors.green.copy(0.3f) else IDEColors.red.copy(0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(if (isRight) "🎉" else "💡", fontSize = 18.sp)
                    Column {
                        Text(if (isRight) "Correct! +1 point" else "Wrong",
                            color = if (isRight) IDEColors.green else IDEColors.red,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(question.explanation, color = IDEColors.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        AnimatedVisibility(visible = showExplain) {
            Button(
                onClick = { challengeVm.nextQuestion() },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
            ) {
                Text(if (qIdx == total - 1) "See Results" else "Next →",
                    color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChallengeResultScreen(
    result: ChallengePhase.Result,
    challengeVm: ChallengeViewModel,
    onBack: () -> Unit
) {
    val won   = result.won
    val draw  = result.myScore == result.oppScore
    val title = when { draw -> "Draw! 🤝"; won -> "You Won! 🏆"; else -> "You Lost 😤" }
    val color = when { draw -> IDEColors.orange; won -> IDEColors.green; else -> IDEColors.red }

    Column(
        modifier = Modifier.fillMaxSize().background(IDEColors.bg0)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))
        Text(if (won) "🏆" else if (draw) "🤝" else "😤", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${result.myScore}", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    color = IDEColors.accent)
                Text("Your Score", color = IDEColors.textMuted, fontSize = 12.sp)
            }
            Text("vs", color = IDEColors.textMuted, fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${result.oppScore}", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    color = IDEColors.red)
                Text("Opponent", color = IDEColors.textMuted, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("${result.total} questions total",
            color = IDEColors.textMuted, fontSize = 14.sp)

        Spacer(Modifier.height(8.dp))
        val xp = if (won) XPRewards.CHALLENGE_WIN else XPRewards.CHALLENGE_PLAY
        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(IDEColors.accent.copy(0.15f))
            .padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("+$xp XP earned", color = IDEColors.accent,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { challengeVm.resetChallenge() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)) {
                Text("Challenge Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, IDEColors.bg4)) {
                Text("Back to Home", color = IDEColors.textSecondary)
            }
        }
    }
}
