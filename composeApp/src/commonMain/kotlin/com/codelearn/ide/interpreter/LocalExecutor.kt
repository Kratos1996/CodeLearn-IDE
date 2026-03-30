package com.codelearn.ide.interpreter

import com.codelearn.ide.model.Language
import java.io.File
import java.util.concurrent.TimeUnit

// ─── Local Executor ───────────────────────────────────────────────────────────
// Used ONLY when Piston API is unreachable (no internet).
// Tries system-installed compilers (kotlinc, javac, gcc, g++, ruby, dart).

object LocalExecutor {

    private const val TIMEOUT_MS = 12_000L

    fun tryExecute(
        code: String,
        language: Language,
        stdin: String,
        startTime: Long
    ): ExecutionResult? {
        val tmpDir = File(System.getProperty("java.io.tmpdir"), "codelearnide_${System.currentTimeMillis()}")
        tmpDir.mkdirs()
        return try {
            when (language) {
                Language.KOTLIN     -> runIfAvailable("kotlinc") { tryKotlin(code, stdin, tmpDir, startTime) }
                Language.JAVA       -> runIfAvailable("javac")   { tryJava(code, stdin, tmpDir, startTime) }
                Language.PYTHON     -> runIfAvailable("python3") { tryPython(code, stdin, tmpDir, startTime) }
                Language.JAVASCRIPT -> runIfAvailable("node")    { tryNode(code, stdin, tmpDir, startTime) }
                Language.CPP        -> runIfAvailable("g++")     { tryCpp(code, stdin, tmpDir, startTime) }
                Language.C          -> runIfAvailable("gcc")     { tryC(code, stdin, tmpDir, startTime) }
                Language.RUBY       -> runIfAvailable("ruby")    { tryRuby(code, stdin, tmpDir, startTime) }
                Language.DART       -> runIfAvailable("dart")    { tryDart(code, stdin, tmpDir, startTime) }
                else -> null
            }
        } finally {
            tmpDir.deleteRecursively()
        }
    }

    private fun runIfAvailable(cmd: String, block: () -> ExecutionResult?): ExecutionResult? {
        return if (isAvailable(cmd)) block() else null
    }

    private fun isAvailable(cmd: String): Boolean = try {
        ProcessBuilder("which", cmd).start().waitFor(2, TimeUnit.SECONDS) &&
        ProcessBuilder("which", cmd).start().exitValue() == 0
    } catch (_: Exception) { false }

    private fun run(cmds: List<String>, dir: File, stdin: String, start: Long): ExecutionResult {
        return try {
            val proc = ProcessBuilder(cmds).directory(dir).redirectErrorStream(false).start()
            if (stdin.isNotEmpty()) proc.outputStream.bufferedWriter().use { it.write(stdin) }
            else proc.outputStream.close()

            val timedOut = !proc.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (timedOut) proc.destroyForcibly()

            ExecutionResult(
                stdout = proc.inputStream.bufferedReader().readText().trim(),
                stderr = proc.errorStream.bufferedReader().readText().trim(),
                exitCode = if (timedOut) -1 else proc.exitValue(),
                durationMs = System.currentTimeMillis() - start,
                timedOut = timedOut,
                engine = "local"
            )
        } catch (e: Exception) {
            ExecutionResult("", e.message ?: "Error", 1, System.currentTimeMillis() - start, engine = "local")
        }
    }

    private fun tryKotlin(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val file = File(dir, "Main.kts"); file.writeText(code)
        return run(listOf("kotlinc", "-script", file.absolutePath), dir, stdin, start)
    }

    private fun tryJava(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val className = Regex("""public\s+class\s+(\w+)""").find(code)?.groupValues?.get(1) ?: "Main"
        val file = File(dir, "$className.java"); file.writeText(code)
        val compile = run(listOf("javac", file.absolutePath), dir, "", start)
        if (compile.exitCode != 0) return compile
        return run(listOf("java", "-cp", dir.absolutePath, className), dir, stdin, start)
    }

    private fun tryCpp(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val src = File(dir, "main.cpp"); src.writeText(code)
        val exe = File(dir, "main")
        val compile = run(listOf("g++", "-o", exe.absolutePath, src.absolutePath, "-std=c++17"), dir, "", start)
        if (compile.exitCode != 0) return compile
        return run(listOf(exe.absolutePath), dir, stdin, start)
    }

    private fun tryC(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val src = File(dir, "main.c"); src.writeText(code)
        val exe = File(dir, "main")
        val compile = run(listOf("gcc", "-o", exe.absolutePath, src.absolutePath), dir, "", start)
        if (compile.exitCode != 0) return compile
        return run(listOf(exe.absolutePath), dir, stdin, start)
    }

    private fun tryRuby(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val file = File(dir, "main.rb"); file.writeText(code)
        return run(listOf("ruby", file.absolutePath), dir, stdin, start)
    }

    private fun tryDart(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val file = File(dir, "main.dart"); file.writeText(code)
        return run(listOf("dart", "run", file.absolutePath), dir, stdin, start)
    }

    private fun tryPython(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val file = File(dir, "main.py"); file.writeText(code)
        return run(listOf("python3", file.absolutePath), dir, stdin, start)
    }

    private fun tryNode(code: String, stdin: String, dir: File, start: Long): ExecutionResult? {
        val file = File(dir, "main.js"); file.writeText(code)
        return run(listOf("node", file.absolutePath), dir, stdin, start)
    }
}
