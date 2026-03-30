package com.codelearn.ide.auth

import kotlinx.serialization.Serializable

// ─── User Profile ─────────────────────────────────────────────────────────────

@Serializable
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val avatarInitials: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val joinedAt: Long = 0L,
    val badges: List<String> = emptyList(),          // badge IDs earned
    val certificates: List<String> = emptyList(),    // cert IDs earned
    val courseProgress: Map<String, CourseProgress> = emptyMap() // courseId -> progress
)

@Serializable
data class CourseProgress(
    val courseId: String = "",
    val completedStageIds: List<String> = emptyList(),
    val completedExamIds: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val examScores: Map<String, Int> = emptyMap()   // examId -> score %
)

// ─── Auth State ───────────────────────────────────────────────────────────────

sealed class AuthState {
    object Loading   : AuthState()
    object LoggedOut : AuthState()
    data class LoggedIn(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

// ─── Auth Result ──────────────────────────────────────────────────────────────

sealed class AuthResult {
    data class Success(val user: UserProfile) : AuthResult()
    data class Failure(val message: String)   : AuthResult()
}

// ─── Local auth cache (offline) ───────────────────────────────────────────────

@Serializable
data class LocalAuthCache(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val idToken: String = "",        // Firebase ID token (expires in 1h)
    val refreshToken: String = "",   // used to get new ID token
    val savedAt: Long = 0L,
    val profile: UserProfile? = null
)
