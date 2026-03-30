package com.codelearn.ide.firebase

import com.codelearn.ide.auth.AuthResult
import com.codelearn.ide.auth.CourseProgress
import com.codelearn.ide.auth.LocalAuthCache
import com.codelearn.ide.auth.UserProfile
import com.codelearn.ide.getPlatform
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.ContentEncoding
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.time.Clock

// ─── Firebase Config ──────────────────────────────────────────────────────────
// Replace these with your actual Firebase project values from:
// Firebase Console → Project Settings → Your apps → Web app config

object FirebaseConfig {
    const val API_KEY         = "AIzaSyAK1e_Q4Ee-CDdMIejs0bPIwu3gNKENgDs"
    const val PROJECT_ID      = "codelearnide"
    const val DB_URL          = "https://codelearnide-default-rtdb.asia-southeast1.firebasedatabase.app"

    // Firebase REST endpoints
    const val AUTH_SIGNUP     = "accounts:signUp"
    const val AUTH_SIGNIN     = "accounts:signInWithPassword"
    const val AUTH_REFRESH    = "token"
    const val AUTH_PROFILE    = "accounts:lookup"
}


// ─── Firebase REST models ─────────────────────────────────────────────────────

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
    val returnSecureToken: Boolean = true
)

@Serializable
data class AuthResponse(
    val idToken: String = "",
    val email: String = "",
    val refreshToken: String = "",
    val expiresIn: String = "",
    val localId: String = "",
    val error: AuthErrorWrapper? = null
)

@Serializable
data class AuthErrorWrapper(val code: Int = 0, val message: String = "")

@Serializable
data class RefreshRequest(
    val grant_type: String = "refresh_token",
    val refresh_token: String
)

@Serializable
data class RefreshResponse(
    val id_token: String = "",
    val refresh_token: String = "",
    val error: JsonElement? = null
)

// ─── Firebase Service ─────────────────────────────────────────────────────────

object FirebaseService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    private val client = HttpClient {
        install(ContentEncoding) {}
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                    useAlternativeNames = true
                    /*encodeDefaults = false
                    explicitNulls = false
                    allowStructuredMapKeys = false*/
                }
            )
        }

        install(Logging) {
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000L
            connectTimeoutMillis = 10000L
            socketTimeoutMillis = 10000L
        }
        engine {
            /* Interceptor { chain ->
                 val chainRequest = chain.request()
                 val request = chainRequest.newBuilder()
                     .header("User-Agent", "android")
                     .header("device-type", "android")
                     .addHeader("X-Visitor-Id", AppPreference.fingerJS)
                     .header("x-request-id", AppPreference.fingerRequestId)
                     .method(chainRequest.method, chainRequest.body)
                     .build()
                 chain.withConnectTimeout(60, TimeUnit.SECONDS)
                 return@Interceptor chain.proceed(request)
             }*/
        }
        install(ResponseObserver) {
            onResponse { response ->
                KtorSimpleLogger("KTOR HTTP_CLIENT: ${response.status.value}")
            }
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }

        HttpResponseValidator {
            validateResponse { response ->
                when(response.status.value) {
                    500 -> throw ServerResponseException(response, "Server Error")
                }
            }

            handleResponseExceptionWithRequest { cause, _ ->
                when (cause) {
                    is ClientRequestException -> {
                        if (cause.response.status.value == 401) {
                            // token expired
                            //refreshToken()
                        }
                    }
                }
            }
        }

        defaultRequest {
            header("device-type", getPlatform())
            header(HttpHeaders.UserAgent, getPlatform())

            url {
                protocol = URLProtocol.HTTPS
                host = "https://identitytoolkit.googleapis.com/v1/"
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // AUTH
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun signUp(name: String, email: String, password: String): AuthResult {
        return try {
            val res: AuthResponse = client.post("${FirebaseConfig.AUTH_SIGNUP}?key=${FirebaseConfig.API_KEY}") {
                contentType(ContentType.Application.Json)
                setBody(SignUpRequest(email, password))
            }.body()

            if (res.error != null) return AuthResult.Failure(friendlyError(res.error.message))

            // Create profile in Realtime DB
            val profile = UserProfile(
                uid           = res.localId,
                name          = name,
                email         = email,
                avatarInitials = name.initials(),
                level         = 1,
                xp            = 0,
                joinedAt      = Clock.System.now().epochSeconds
            )
            saveProfile(res.localId, res.idToken, profile)

            AuthResult.Success(profile)
        } catch (e: Exception) {
            AuthResult.Failure("Sign up failed: ${e.message}")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val res: AuthResponse = client.post("${FirebaseConfig.AUTH_SIGNIN}?key=${FirebaseConfig.API_KEY}") {
                contentType(ContentType.Application.Json)
                setBody(SignInRequest(email, password))
            }.body()

            if (res.error != null) return AuthResult.Failure(friendlyError(res.error.message))

            // Load profile from DB
            val profile = loadProfile(res.localId, res.idToken)
                ?: UserProfile(uid = res.localId, email = email, name = email.substringBefore("@"), avatarInitials = email.take(2).uppercase())

            AuthResult.Success(profile)
        } catch (e: Exception) {
            AuthResult.Failure("Sign in failed: ${e.message}")
        }
    }

    suspend fun refreshToken(refreshToken: String): String? {
        return try {
            val res: RefreshResponse = client.post("${FirebaseConfig.AUTH_REFRESH}?key=${FirebaseConfig.API_KEY}") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refresh_token = refreshToken))
            }.body()
            res.id_token.ifBlank { null }
        } catch (_: Exception) { null }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REALTIME DATABASE — Users
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun saveProfile(uid: String, idToken: String, profile: UserProfile) {
        try {
            client.put("${FirebaseConfig.DB_URL}/users/$uid/profile.json?auth=$idToken") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(UserProfile.serializer(), profile))
            }
        } catch (_: Exception) {}
    }

    suspend fun loadProfile(uid: String, idToken: String): UserProfile? {
        return try {
            val raw = client.get("${FirebaseConfig.DB_URL}/users/$uid/profile.json?auth=$idToken")
                .body<String>()
            if (raw == "null") null
            else json.decodeFromString(UserProfile.serializer(), raw)
        } catch (_: Exception) { null }
    }

    suspend fun updateXP(uid: String, idToken: String, xp: Int, level: Int) {
        try {
            client.patch("${FirebaseConfig.DB_URL}/users/$uid/profile.json?auth=$idToken") {
                contentType(ContentType.Application.Json)
                setBody("""{"xp":$xp,"level":$level}""")
            }
        } catch (_: Exception) {}
    }

    suspend fun addBadge(uid: String, idToken: String, badgeId: String, profile: UserProfile): UserProfile {
        if (profile.badges.contains(badgeId)) return profile
        val updated = profile.copy(badges = profile.badges + badgeId)
        saveProfile(uid, idToken, updated)
        return updated
    }

    suspend fun addCertificate(uid: String, idToken: String, certId: String, profile: UserProfile): UserProfile {
        if (profile.certificates.contains(certId)) return profile
        val updated = profile.copy(certificates = profile.certificates + certId)
        saveProfile(uid, idToken, updated)
        return updated
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REALTIME DATABASE — Course Progress
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun saveCourseProgress(uid: String, idToken: String, progress: CourseProgress) {
        try {
            client.put("${FirebaseConfig.DB_URL}/users/$uid/courseProgress/${progress.courseId}.json?auth=$idToken") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(CourseProgress.serializer(), progress))
            }
        } catch (_: Exception) {}
    }

    suspend fun loadAllCourseProgress(uid: String, idToken: String): Map<String, CourseProgress> {
        return try {
            val raw = client.get("${FirebaseConfig.DB_URL}/users/$uid/courseProgress.json?auth=$idToken")
                .body<String>()
            if (raw == "null") emptyMap()
            else {
                val obj = json.parseToJsonElement(raw).jsonObject
                obj.entries.associate { (k, v) ->
                    k to json.decodeFromJsonElement(CourseProgress.serializer(), v)
                }
            }
        } catch (_: Exception) { emptyMap() }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REALTIME DATABASE — Community Chat
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun sendChatMessage(
        roomId: String, idToken: String,
        senderUid: String, senderName: String, text: String
    ) {
        // Use PUT with timestamp as key — guarantees unique messages, no overwriting
        val ts = Clock.System.now().epochSeconds
        val msgKey = "msg_${ts}_${senderUid.take(6)}"
        val msg = buildJsonObject {
            put("uid", senderUid)
            put("name", senderName)
            put("text", text)
            put("ts", ts)
        }
        // No auth param needed in test mode — add ?auth= only if rules require it
        val url = if (idToken.isNotBlank())
            "${FirebaseConfig.DB_URL}/chat/$roomId/$msgKey.json?auth=$idToken"
        else
            "${FirebaseConfig.DB_URL}/chat/$roomId/$msgKey.json"

        try {
            val response = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(msg.toString())
            }
            // Log result for debugging
            println("ChatSend[$roomId]: ${response.status}")
        } catch (e: Exception) {
            println("ChatSend ERROR: ${e.message}")
        }
    }

    suspend fun loadChatMessages(roomId: String, idToken: String, limit: Int = 50): List<ChatMessage> {
        // Fetch without orderBy (no Firebase index required)
        // Sort client-side instead
        val url = if (idToken.isNotBlank())
            "${FirebaseConfig.DB_URL}/chat/$roomId.json?auth=$idToken&limitToLast=$limit"
        else
            "${FirebaseConfig.DB_URL}/chat/$roomId.json?limitToLast=$limit"

        return try {
            val raw = client.get(url).body<String>()
            println("ChatLoad[$roomId]: ${raw.take(100)}")
            if (raw == "null" || raw.isBlank()) return emptyList()
            val element = json.parseToJsonElement(raw)
            if (element !is kotlinx.serialization.json.JsonObject) return emptyList()
            element.jsonObject.values.mapNotNull { v ->
                try {
                    val o = v.jsonObject
                    ChatMessage(
                        uid  = o["uid"]?.jsonPrimitive?.content ?: "",
                        name = o["name"]?.jsonPrimitive?.content ?: "User",
                        text = o["text"]?.jsonPrimitive?.content ?: "",
                        ts   = o["ts"]?.jsonPrimitive?.longOrNull ?: 0L
                    )
                } catch (_: Exception) { null }
            }.sortedBy { it.ts }
        } catch (e: Exception) {
            println("ChatLoad ERROR: ${e.message}")
            emptyList()
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REALTIME DATABASE — Challenges
    // ══════════════════════════════════════════════════════════════════════════

    suspend fun postChallenge(idToken: String, challenge: ChallengeRecord) {
        try {
            client.post("${FirebaseConfig.DB_URL}/challenges.json?auth=$idToken") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ChallengeRecord.serializer(), challenge))
            }
        } catch (_: Exception) {}
    }

    suspend fun loadLeaderboard(idToken: String): List<LeaderboardEntry> {
        return try {
            val raw = client.get(
                "${FirebaseConfig.DB_URL}/leaderboard.json?auth=$idToken&orderBy=\"xp\"&limitToLast=20"
            ).body<String>()
            if (raw == "null") return emptyList()
            val obj = json.parseToJsonElement(raw).jsonObject
            obj.values.mapNotNull { v ->
                try {
                    val o = v.jsonObject
                    LeaderboardEntry(
                        uid   = o["uid"]?.jsonPrimitive?.content ?: "",
                        name  = o["name"]?.jsonPrimitive?.content ?: "",
                        xp    = o["xp"]?.jsonPrimitive?.intOrNull ?: 0,
                        level = o["level"]?.jsonPrimitive?.intOrNull ?: 1,
                        initials = o["initials"]?.jsonPrimitive?.content ?: "?"
                    )
                } catch (_: Exception) { null }
            }.sortedByDescending { it.xp }
        } catch (_: Exception) { emptyList() }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private fun friendlyError(raw: String): String = when {
        raw.contains("EMAIL_EXISTS")          -> "This email is already registered. Please sign in."
        raw.contains("WEAK_PASSWORD")         -> "Password must be at least 6 characters."
        raw.contains("INVALID_EMAIL")         -> "Please enter a valid email address."
        raw.contains("EMAIL_NOT_FOUND")       -> "No account found with this email."
        raw.contains("INVALID_PASSWORD")      -> "Wrong password. Please try again."
        raw.contains("INVALID_LOGIN_CREDENTIALS") -> "Wrong email or password."
        raw.contains("TOO_MANY_ATTEMPTS")     -> "Too many attempts. Please try again later."
        raw.contains("USER_DISABLED")         -> "This account has been disabled."
        else -> "Error: $raw"
    }

    fun dispose() { client.close() }
}

// ─── Shared data models for Firebase ─────────────────────────────────────────

@Serializable
data class ChatMessage(
    val uid: String = "",
    val name: String = "",
    val text: String = "",
    val ts: Long = 0L
)

@Serializable
data class ChallengeRecord(
    val challengerId: String = "",
    val challengerName: String = "",
    val opponentId: String = "",
    val opponentName: String = "",
    val language: String = "",
    val challengerScore: Int = 0,
    val opponentScore: Int = 0,
    val winnerId: String = "",
    val ts: Long = 0L
)

@Serializable
data class LeaderboardEntry(
    val uid: String = "",
    val name: String = "",
    val xp: Int = 0,
    val level: Int = 1,
    val initials: String = ""
)

// ─── Extensions ───────────────────────────────────────────────────────────────

fun String.initials(): String {
    val parts = trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}