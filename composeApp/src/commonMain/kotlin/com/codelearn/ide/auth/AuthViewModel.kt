package com.codelearn.ide.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.codelearn.ide.firebase.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.Clock

class AuthViewModel : ScreenModel {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ── Auth state ────────────────────────────────────────────────────────────
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Current user convenience
    val currentUser get() = (_authState.value as? AuthState.LoggedIn)?.user
    val currentUid  get() = currentUser?.uid ?: ""
    val idToken     get() = loadCache()?.idToken ?: ""

    // ── Offline cache file ────────────────────────────────────────────────────
    private val cacheFile: File by lazy {
        val dir = File(System.getProperty("user.home") ?: ".", ".codelearnide")
        dir.mkdirs()
        File(dir, "auth_cache.json")
    }

    init { checkSession() }

    // ═════════════════════════════════════════════════════════════════════════
    // SESSION
    // ═════════════════════════════════════════════════════════════════════════

    private fun checkSession() {
        screenModelScope.launch {
            val cache = loadCache()
            if (cache != null && cache.uid.isNotBlank()) {
                // Try to refresh token and reload profile
                val newToken = if (cache.refreshToken.isNotBlank()) {
                    FirebaseService.refreshToken(cache.refreshToken) ?: cache.idToken
                } else cache.idToken

                val profile = if (newToken.isNotBlank()) {
                    FirebaseService.loadProfile(cache.uid, newToken) ?: cache.profile
                } else cache.profile

                if (profile != null) {
                    saveCache(cache.copy(idToken = newToken, profile = profile))
                    _authState.value = AuthState.LoggedIn(profile)
                } else {
                    _authState.value = AuthState.LoggedOut
                }
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SIGN UP
    // ═════════════════════════════════════════════════════════════════════════

    fun signUp(name: String, email: String, password: String) {
        if (!validateSignUp(name, email, password)) return
        screenModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = FirebaseService.signUp(name.trim(), email.trim(), password)) {
                is AuthResult.Success -> {
                    saveCache(LocalAuthCache(
                        uid = result.user.uid, email = email, name = name,
                        profile = result.user, savedAt = Clock.System.now().epochSeconds
                    ))
                    _authState.value = AuthState.LoggedIn(result.user)
                }
                is AuthResult.Failure -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SIGN IN
    // ═════════════════════════════════════════════════════════════════════════

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter your email and password."
            return
        }
        screenModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = FirebaseService.signIn(email.trim(), password)) {
                is AuthResult.Success -> {
                    saveCache(LocalAuthCache(
                        uid = result.user.uid, email = email, name = result.user.name,
                        profile = result.user, savedAt = Clock.System.now().epochSeconds                    ))
                    _authState.value = AuthState.LoggedIn(result.user)
                }
                is AuthResult.Failure -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SIGN OUT
    // ═════════════════════════════════════════════════════════════════════════

    fun signOut() {
        cacheFile.delete()
        _authState.value = AuthState.LoggedOut
        _errorMessage.value = null
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UPDATE PROFILE (called by CourseViewModel after XP/badge changes)
    // ═════════════════════════════════════════════════════════════════════════

    fun updateLocalProfile(updated: UserProfile) {
        val cache = loadCache() ?: return
        saveCache(cache.copy(profile = updated))
        _authState.value = AuthState.LoggedIn(updated)
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CACHE HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    fun loadCache(): LocalAuthCache? = try {
        if (cacheFile.exists())
            json.decodeFromString<LocalAuthCache>(cacheFile.readText())
        else null
    } catch (_: Exception) { null }

    private fun saveCache(cache: LocalAuthCache) = try {
        cacheFile.writeText(json.encodeToString(cache))
    } catch (_: Exception) {}

    // ── Validation ────────────────────────────────────────────────────────────

    private fun validateSignUp(name: String, email: String, password: String): Boolean {
        return when {
            name.trim().length < 2 -> { _errorMessage.value = "Name must be at least 2 characters."; false }
            !email.contains("@") || !email.contains(".") -> { _errorMessage.value = "Please enter a valid email."; false }
            password.length < 6 -> { _errorMessage.value = "Password must be at least 6 characters."; false }
            else -> true
        }
    }

    fun clearError() { _errorMessage.value = null }
}
