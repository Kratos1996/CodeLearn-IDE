package com.codelearn.ide.course

import kotlinx.serialization.Serializable

// ─── Course Level ─────────────────────────────────────────────────────────────

enum class CourseLevel { BASIC, ADVANCED }

// ─── Lesson content types ─────────────────────────────────────────────────────

enum class LessonType { TEXT, CODE, TIP, WARNING }

@Serializable
data class LessonCard(
    val type: LessonType = LessonType.TEXT,
    val title: String = "",
    val body: String = "",
    val code: String = "",
    val language: String = ""
)

// ─── Exam question types ──────────────────────────────────────────────────────

@Serializable
data class ExamQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = ""    // shown after answering
)

// ─── Stage ────────────────────────────────────────────────────────────────────

@Serializable
data class CourseStage(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val description: String = "",
    val lessons: List<LessonCard> = emptyList(),
    val examQuestions: List<ExamQuestion> = emptyList(),
    val passMark: Int = 70          // % needed to pass exam
)

// ─── Course ───────────────────────────────────────────────────────────────────

@Serializable
data class Course(
    val id: String = "",
    val languageId: String = "",    // matches Language.fileExtension
    val level: CourseLevel = CourseLevel.BASIC,
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val color: Long = 0xFF7C6AF7,
    val stages: List<CourseStage> = emptyList(),
    val badgeId: String = "",
    val certificateTitle: String = ""
)

// ─── Badge definition ─────────────────────────────────────────────────────────

data class BadgeInfo(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val color: Long
)

// ─── XP rewards ───────────────────────────────────────────────────────────────

object XPRewards {
    const val LESSON_READ    = 5
    const val STAGE_COMPLETE = 20
    const val EXAM_PASS      = 50
    const val EXAM_PERFECT   = 100   // 100% score
    const val COURSE_COMPLETE = 200
    const val CHALLENGE_WIN  = 30
    const val CHALLENGE_PLAY = 10

    fun xpToLevel(xp: Int): Int = when {
        xp < 100  -> 1
        xp < 250  -> 2
        xp < 500  -> 3
        xp < 1000 -> 4
        xp < 2000 -> 5
        xp < 3500 -> 6
        xp < 5000 -> 7
        xp < 7000 -> 8
        xp < 10000 -> 9
        else -> 10
    }

    fun xpForNextLevel(xp: Int): Int {
        val thresholds = listOf(100,250,500,1000,2000,3500,5000,7000,10000,Int.MAX_VALUE)
        return thresholds.firstOrNull { it > xp } ?: Int.MAX_VALUE
    }
}
