package com.codelearn.ide.course

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.auth.CourseProgress
import com.codelearn.ide.auth.UserProfile
import com.codelearn.ide.firebase.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class CourseViewModel(private val authVm: AuthViewModel) : ScreenModel {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ── Progress state ────────────────────────────────────────────────────────
    private val _allProgress = MutableStateFlow<Map<String, CourseProgress>>(emptyMap())
    val allProgress: StateFlow<Map<String, CourseProgress>> = _allProgress

    // ── Active course/stage/lesson ────────────────────────────────────────────
    private val _activeCourse = MutableStateFlow<Course?>(null)
    val activeCourse: StateFlow<Course?> = _activeCourse

    private val _activeStage = MutableStateFlow<CourseStage?>(null)
    val activeStage: StateFlow<CourseStage?> = _activeStage

    private val _activeLessonIndex = MutableStateFlow(0)
    val activeLessonIndex: StateFlow<Int> = _activeLessonIndex

    // ── Exam state ────────────────────────────────────────────────────────────
    private val _examActive = MutableStateFlow(false)
    val examActive: StateFlow<Boolean> = _examActive

    private val _examQuestionIndex = MutableStateFlow(0)
    val examQuestionIndex: StateFlow<Int> = _examQuestionIndex

    private val _examAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // questionIdx -> chosen option
    private val _examResult = MutableStateFlow<ExamResult?>(null)
    val examResult: StateFlow<ExamResult?> = _examResult

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex

    private val _showExplanation = MutableStateFlow(false)
    val showExplanation: StateFlow<Boolean> = _showExplanation

    // ── XP animation ─────────────────────────────────────────────────────────
    private val _xpGained = MutableStateFlow(0)
    val xpGained: StateFlow<Int> = _xpGained

    private val _newBadge = MutableStateFlow<BadgeInfo?>(null)
    val newBadge: StateFlow<BadgeInfo?> = _newBadge

    private val _newCertificate = MutableStateFlow<String?>(null)
    val newCertificate: StateFlow<String?> = _newCertificate

    // ── Local progress cache ──────────────────────────────────────────────────
    private val progressFile: File by lazy {
        val dir = File(System.getProperty("user.home") ?: ".", ".codelearnide")
        dir.mkdirs()
        File(dir, "course_progress.json")
    }

    init { loadProgress() }

    // ═════════════════════════════════════════════════════════════════════════
    // PROGRESS LOADING
    // ═════════════════════════════════════════════════════════════════════════

    private fun loadProgress() {
        screenModelScope.launch {
            // Load from local cache first (instant, works offline)
            _allProgress.value = loadLocalProgress()

            // Then sync from Firebase if online
            val uid   = authVm.currentUid
            val token = authVm.idToken
            if (uid.isNotBlank() && token.isNotBlank()) {
                val remote = FirebaseService.loadAllCourseProgress(uid, token)
                if (remote.isNotEmpty()) {
                    // Merge: take the more complete version for each course
                    val merged = (_allProgress.value + remote).toMutableMap()
                    remote.forEach { (k, v) ->
                        val local = _allProgress.value[k]
                        merged[k] = if (local != null && local.completedStageIds.size > v.completedStageIds.size) local else v
                    }
                    _allProgress.value = merged
                    saveLocalProgress(merged)
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // COURSE NAVIGATION
    // ═════════════════════════════════════════════════════════════════════════

    fun openCourse(course: Course) {
        _activeCourse.value = course
        _activeStage.value = null
        _activeLessonIndex.value = 0
        _examActive.value = false
        _examResult.value = null
    }

    fun openStage(stage: CourseStage) {
        _activeStage.value = stage
        _activeLessonIndex.value = 0
        _examActive.value = false
        _examResult.value = null
        _selectedOptionIndex.value = null
        _showExplanation.value = false
    }

    fun nextLesson() {
        val stage = _activeStage.value ?: return
        val next = _activeLessonIndex.value + 1
        if (next < stage.lessons.size) {
            _activeLessonIndex.value = next
            // Award XP for reading a lesson
            awardXP(XPRewards.LESSON_READ)
        } else {
            // All lessons read — start exam
            startExam()
        }
    }

    fun prevLesson() {
        if (_activeLessonIndex.value > 0) _activeLessonIndex.value--
    }

    // ═════════════════════════════════════════════════════════════════════════
    // EXAM
    // ═════════════════════════════════════════════════════════════════════════

    private fun startExam() {
        _examActive.value = true
        _examQuestionIndex.value = 0
        _examAnswers.value = emptyMap()
        _examResult.value = null
        _selectedOptionIndex.value = null
        _showExplanation.value = false
    }

    fun selectExamOption(optionIndex: Int) {
        if (_showExplanation.value) return  // already answered this question
        _selectedOptionIndex.value = optionIndex
        _showExplanation.value = true

        // Record answer
        val qIdx = _examQuestionIndex.value
        _examAnswers.value = _examAnswers.value + (qIdx to optionIndex)
    }

    fun nextExamQuestion() {
        val stage = _activeStage.value ?: return
        val next = _examQuestionIndex.value + 1
        if (next < stage.examQuestions.size) {
            _examQuestionIndex.value = next
            _selectedOptionIndex.value = null
            _showExplanation.value = false
        } else {
            // All questions answered — calculate result
            finishExam()
        }
    }

    private fun finishExam() {
        val stage  = _activeStage.value ?: return
        val course = _activeCourse.value ?: return
        val answers = _examAnswers.value
        val questions = stage.examQuestions

        val correct = questions.indices.count { idx ->
            answers[idx] == questions[idx].correctIndex
        }
        val scorePercent = if (questions.isEmpty()) 100
                           else (correct * 100) / questions.size
        val passed = scorePercent >= stage.passMark

        _examResult.value = ExamResult(
            stageId      = stage.id,
            stageName    = stage.name,
            score        = scorePercent,
            correct      = correct,
            total        = questions.size,
            passed       = passed
        )

        if (passed) {
            onStagePassed(course, stage, scorePercent)
        }
    }

    private fun onStagePassed(course: Course, stage: CourseStage, score: Int) {
        screenModelScope.launch {
            val uid   = authVm.currentUid
            val token = authVm.idToken

            // Update progress
            val existing = _allProgress.value[course.id] ?: CourseProgress(courseId = course.id)
            val newCompleted = (existing.completedStageIds + stage.id).distinct()
            val newScores = existing.examScores + (stage.id to score)
            val allStagesDone = course.stages.all { it.id in newCompleted }

            val updated = existing.copy(
                completedStageIds = newCompleted,
                examScores        = newScores,
                isCompleted       = allStagesDone,
                completedAt       = if (allStagesDone) System.currentTimeMillis() else existing.completedAt
            )

            val newProgressMap = _allProgress.value + (course.id to updated)
            _allProgress.value = newProgressMap
            saveLocalProgress(newProgressMap)

            // XP
            val xp = if (score == 100) XPRewards.EXAM_PERFECT else XPRewards.EXAM_PASS
            awardXP(XPRewards.STAGE_COMPLETE + xp)

            // Sync to Firebase
            if (uid.isNotBlank() && token.isNotBlank()) {
                screenModelScope.launch(Dispatchers.IO) { FirebaseService.saveCourseProgress(uid, token, updated) }
            }

            // Course completed — award badge + certificate
            if (allStagesDone) {
                awardXP(XPRewards.COURSE_COMPLETE)
                onCourseCompleted(course, uid, token)
            }
        }
    }

    private suspend fun onCourseCompleted(course: Course, uid: String, token: String) {
        // Badge
        val badge = CourseRepository.getBadge(course.badgeId)
        if (badge != null) {
            _newBadge.value = badge
            val profile = authVm.currentUser ?: return
            val updatedProfile = profile.copy(badges = (profile.badges + badge.id).distinct())
            authVm.updateLocalProfile(updatedProfile)
            if (uid.isNotBlank() && token.isNotBlank()) {
                screenModelScope.launch(Dispatchers.IO) { FirebaseService.addBadge(uid, token, badge.id, updatedProfile)
                }
            }
        }
        // Certificate
        _newCertificate.value = course.certificateTitle
        val profile = authVm.currentUser ?: return
        val updatedProfile = profile.copy(certificates = (profile.certificates + course.id).distinct())
        authVm.updateLocalProfile(updatedProfile)
        if (uid.isNotBlank() && token.isNotBlank()) {
            screenModelScope.launch(Dispatchers.IO) { FirebaseService.addCertificate(uid, token, course.id, updatedProfile) }
        }
    }

    fun retryExam() {
        _examResult.value = null
        startExam()
    }

    fun dismissBadge()       { _newBadge.value = null }
    fun dismissCertificate() { _newCertificate.value = null }

    // ═════════════════════════════════════════════════════════════════════════
    // XP & LEVEL
    // ═════════════════════════════════════════════════════════════════════════

    private fun awardXP(amount: Int) {
        val profile = authVm.currentUser ?: return
        _xpGained.value = amount
        val newXP    = profile.xp + amount
        val newLevel = XPRewards.xpToLevel(newXP)
        val updated  = profile.copy(xp = newXP, level = newLevel)
        authVm.updateLocalProfile(updated)

        val uid   = authVm.currentUid
        val token = authVm.idToken
        if (uid.isNotBlank() && token.isNotBlank()) {
            screenModelScope.launch(Dispatchers.IO) {
                FirebaseService.updateXP(uid, token, newXP, newLevel)
            }
        }
    }

    fun clearXPGained() { _xpGained.value = 0 }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    fun clearActiveStage()  { _activeStage.value = null; _examActive.value = false; _examResult.value = null }
    fun clearActiveCourse() { _activeCourse.value = null; _activeStage.value = null; _examActive.value = false }

    fun isCourseUnlocked(course: Course): Boolean {
        if (course.level == CourseLevel.BASIC) return true
        // Advanced unlocked only after Basic is completed
        val basicId = "${course.languageId}_basic"
        return _allProgress.value[basicId]?.isCompleted == true
    }

    fun isStageLocked(course: Course, stage: CourseStage): Boolean {
        val stageIndex = course.stages.indexOf(stage)
        if (stageIndex == 0) return false
        val prevStage = course.stages[stageIndex - 1]
        return prevStage.id !in (_allProgress.value[course.id]?.completedStageIds ?: emptyList())
    }

    fun isStageCompleted(courseId: String, stageId: String): Boolean =
        stageId in (_allProgress.value[courseId]?.completedStageIds ?: emptyList())

    fun getStageScore(courseId: String, stageId: String): Int? =
        _allProgress.value[courseId]?.examScores?.get(stageId)

    fun courseCompletionPercent(course: Course): Int {
        val progress = _allProgress.value[course.id] ?: return 0
        if (course.stages.isEmpty()) return 0
        return (progress.completedStageIds.size * 100) / course.stages.size
    }

    // ── Local persistence ──────────────────────────────────────────────────────

    private fun loadLocalProgress(): Map<String, CourseProgress> = try {
        if (progressFile.exists()) {
            val raw = progressFile.readText()
            json.decodeFromString<Map<String, CourseProgress>>(raw)
        } else emptyMap()
    } catch (_: Exception) { emptyMap() }

    private fun saveLocalProgress(map: Map<String, CourseProgress>) = try {
        progressFile.writeText(json.encodeToString(map))
    } catch (_: Exception) {}
}

// ─── Exam Result ──────────────────────────────────────────────────────────────

data class ExamResult(
    val stageId: String,
    val stageName: String,
    val score: Int,
    val correct: Int,
    val total: Int,
    val passed: Boolean
)
