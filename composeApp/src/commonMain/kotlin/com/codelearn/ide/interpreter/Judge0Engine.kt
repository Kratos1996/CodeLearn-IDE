package com.codelearn.ide.interpreter

import com.codelearn.ide.model.Language
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ─── Result ───────────────────────────────────────────────────────────────────

data class ExecutionResult(
    val stdout: String,
    val stderr: String,
    val exitCode: Int,
    val durationMs: Long,
    val timedOut: Boolean = false,
    val engine: String = "judge0",
    val statusDescription: String = ""
)

// ─── Judge0 CE API models ─────────────────────────────────────────────────────
// Docs: https://ce.judge0.com
// Free, no API key, no whitelist. Supports 60+ languages.
// Uses async polling: POST /submissions → token → GET /submissions/{token}

@Serializable
data class Judge0SubmitRequest(
    val source_code: String,           // plain text (not base64)
    val language_id: Int,
    val stdin: String = "",
    val cpu_time_limit: Double = 10.0, // seconds
    val wall_time_limit: Double = 15.0
)

@Serializable
data class Judge0SubmitResponse(
    val token: String = ""
)

@Serializable
data class Judge0Result(
    val stdout: String? = null,
    val stderr: String? = null,
    val compile_output: String? = null,
    val message: String? = null,
    val status: Judge0Status? = null,
    val time: String? = null,
    val exit_code: Int? = null
)

@Serializable
data class Judge0Status(
    val id: Int = 0,
    val description: String = ""
)

// Judge0 status IDs:
// 1=In Queue, 2=Processing, 3=Accepted, 4=Wrong Answer,
// 5=Time Limit Exceeded, 6=Compilation Error, 7=Runtime Error (SIGSEGV),
// 8=Runtime Error (SIGXFSZ), 9=Runtime Error (SIGFPE), 10=Runtime Error (SIGABRT),
// 11=Runtime Error (NZEC), 12=Runtime Error (Other), 13=Internal Error, 14=Exec Format Error

// ─── Language → Judge0 language_id mapping ────────────────────────────────────

private val JUDGE0_LANG_IDS = mapOf(
    Language.KOTLIN     to 78,  // Kotlin (1.3.70)
    Language.JAVA       to 62,  // Java (OpenJDK 13.0.1)
    Language.PYTHON     to 71,  // Python (3.8.1)
    Language.JAVASCRIPT to 63,  // JavaScript (Node.js 12.14.0)
    Language.CSHARP     to 51,  // C# (Mono 6.6.0.161)
    Language.CPP        to 54,  // C++ (GCC 9.2.0)
    Language.C          to 50,  // C (GCC 9.2.0)
    Language.RUBY       to 72,  // Ruby (2.7.0)
    Language.DART       to 90,  // Dart (2.7.2) — in Extra CE, but CE also has it
    Language.VB         to 47   // Visual Basic.Net (vbnc 0.0.0.5943)
)

// ─── Judge0 Engine ────────────────────────────────────────────────────────────

object Judge0Engine {

    private const val BASE_URL = "https://ce.judge0.com"
    private const val SUBMIT_URL = "$BASE_URL/submissions?base64_encoded=false&wait=false"
    private const val RESULT_URL = "$BASE_URL/submissions"
    private const val POLL_INTERVAL_MS = 800L
    private const val MAX_POLLS = 20

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = HttpClient {
        install(ContentNegotiation) { json(json) }
    }

    suspend fun execute(
        code: String,
        language: Language,
        stdin: String = ""
    ): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val langId = JUDGE0_LANG_IDS[language]
            ?: return ExecutionResult("", "Language not supported in Judge0", 1, 0)

        return try {
            // Step 1: Submit code → get token
            val token = submitCode(code, langId, stdin)
                ?: return ExecutionResult("", "Failed to submit to Judge0 — no token returned", 1,
                    System.currentTimeMillis() - startTime)

            // Step 2: Poll until execution finishes
            val result = pollResult(token, startTime)
            result

        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            // Try local system compilers as fallback
            val local = LocalExecutor.tryExecute(code, language, stdin, startTime)
            local ?: ExecutionResult(
                stdout = "",
                stderr = buildNetworkErrorMessage(e),
                exitCode = 1,
                durationMs = elapsed,
                engine = "offline"
            )
        }
    }

    private suspend fun submitCode(code: String, langId: Int, stdin: String): String? {
        val response: Judge0SubmitResponse = client.post(SUBMIT_URL) {
            contentType(ContentType.Application.Json)
            setBody(Judge0SubmitRequest(
                source_code = code,
                language_id = langId,
                stdin = stdin
            ))
        }.body()
        return response.token.ifBlank { null }
    }

    private suspend fun pollResult(token: String, startTime: Long): ExecutionResult {
        repeat(MAX_POLLS) { attempt ->
            delay(POLL_INTERVAL_MS)

            val result: Judge0Result = client.get(
                "$RESULT_URL/$token?base64_encoded=false&fields=stdout,stderr,compile_output,status,time,exit_code,message"
            ).body()

            val statusId = result.status?.id ?: 0

            // Status 1 (In Queue) or 2 (Processing) → keep polling
            if (statusId <= 2) return@repeat

            val elapsed = System.currentTimeMillis() - startTime
            val stdout = result.stdout?.trim() ?: ""
            val compileErr = result.compile_output?.trim() ?: ""
            val runtimeErr = result.stderr?.trim() ?: ""
            val statusDesc = result.status?.description ?: ""

            // Combine all error output
            val fullError = listOf(compileErr, runtimeErr, result.message?.trim() ?: "")
                .filter { it.isNotBlank() }
                .joinToString("\n")

            val exitCode = when (statusId) {
                3    -> result.exit_code ?: 0   // Accepted
                5    -> -1                        // TLE
                6    -> 1                         // Compilation Error
                else -> result.exit_code ?: 1    // Runtime errors
            }

            return ExecutionResult(
                stdout = stdout,
                stderr = fullError,
                exitCode = exitCode,
                durationMs = elapsed,
                timedOut = statusId == 5,
                engine = "judge0",
                statusDescription = statusDesc
            )
        }

        // Timed out polling
        return ExecutionResult(
            stdout = "",
            stderr = "Execution timed out — server took too long to respond",
            exitCode = -1,
            durationMs = System.currentTimeMillis() - startTime,
            timedOut = true,
            engine = "judge0"
        )
    }

    private fun buildNetworkErrorMessage(e: Exception): String = buildString {
        appendLine("❌ Cannot reach Judge0 compiler service.")
        appendLine("Please check your internet connection.")
        appendLine()
        appendLine("Technical: ${e.message}")
        appendLine()
        appendLine("Tip: Make sure the app has INTERNET permission and you're online.")
    }

    fun dispose() { client.close() }
}
