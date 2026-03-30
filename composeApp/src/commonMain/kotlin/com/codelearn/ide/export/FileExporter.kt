package com.codelearn.ide.export

import com.codelearn.ide.model.CodeFile
import com.codelearn.ide.model.Language
import com.codelearn.ide.model.Project
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// ─── Export Result ────────────────────────────────────────────────────────────

data class ExportResult(
    val success: Boolean,
    val path: String = "",
    val message: String = ""
)

// ─── File Exporter ────────────────────────────────────────────────────────────
// Saves code files to device so they can be opened in VS Code / IntelliJ / etc.

object FileExporter {

    // ── Export single file ────────────────────────────────────────────────────
    // Saves to Downloads/CodeLearnIDE/<filename>

    fun exportFile(file: CodeFile, customDir: String? = null): ExportResult {
        return try {
            val dir = getExportDir(customDir)
            val outputFile = File(dir, file.name)
            outputFile.writeText(file.content)
            ExportResult(
                success = true,
                path = outputFile.absolutePath,
                message = "✅ Saved to ${outputFile.absolutePath}"
            )
        } catch (e: Exception) {
            ExportResult(false, message = "❌ Export failed: ${e.message}")
        }
    }

    // ── Export project as .zip ─────────────────────────────────────────────────
    // Creates a proper project structure that opens in VS Code / IntelliJ

    fun exportProjectAsZip(project: Project, customDir: String? = null): ExportResult {
        return try {
            val dir = getExportDir(customDir)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm").format(Date())
            val zipFile = File(dir, "${project.name.replace(" ", "_")}_$timestamp.zip")

            ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
                // Add each source file
                project.files.forEach { file ->
                    zip.putNextEntry(ZipEntry("${project.name}/${file.name}"))
                    zip.write(file.content.toByteArray())
                    zip.closeEntry()
                }

                // Add README
                zip.putNextEntry(ZipEntry("${project.name}/README.md"))
                zip.write(buildReadme(project).toByteArray())
                zip.closeEntry()

                // Add VS Code settings if applicable
                val vscodeSettings = buildVSCodeSettings(project.language)
                if (vscodeSettings != null) {
                    zip.putNextEntry(ZipEntry("${project.name}/.vscode/settings.json"))
                    zip.write(vscodeSettings.toByteArray())
                    zip.closeEntry()
                }
            }

            ExportResult(
                success = true,
                path = zipFile.absolutePath,
                message = "✅ Project exported to ${zipFile.absolutePath}"
            )
        } catch (e: Exception) {
            ExportResult(false, message = "❌ Export failed: ${e.message}")
        }
    }

    // ── Export all files in a flat folder ──────────────────────────────────────

    fun exportProjectFiles(project: Project, customDir: String? = null): ExportResult {
        return try {
            val baseDir = File(getExportDir(customDir), project.name.replace(" ", "_"))
            baseDir.mkdirs()

            project.files.forEach { file ->
                File(baseDir, file.name).writeText(file.content)
            }
            File(baseDir, "README.md").writeText(buildReadme(project))

            ExportResult(
                success = true,
                path = baseDir.absolutePath,
                message = "✅ ${project.files.size} file(s) saved to ${baseDir.absolutePath}"
            )
        } catch (e: Exception) {
            ExportResult(false, message = "❌ Export failed: ${e.message}")
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun getExportDir(customDir: String?): File {
        val dir = if (customDir != null) {
            File(customDir)
        } else {
            // Try user home Downloads first, then temp dir
            val downloads = File(System.getProperty("user.home"), "Downloads/CodeLearnIDE")
            if (downloads.parentFile?.canWrite() == true) downloads
            else File(System.getProperty("java.io.tmpdir"), "CodeLearnIDE")
        }
        dir.mkdirs()
        return dir
    }

    private fun buildReadme(project: Project): String = """
# ${project.name}

**Language:** ${project.language.displayName}  
**Created with:** CodeLearn IDE  
**Files:** ${project.files.size}

## Files
${project.files.joinToString("\n") { "- `${it.name}`" }}

## How to open in VS Code
1. Install VS Code from https://code.visualstudio.com
2. Install the ${project.language.displayName} extension
3. Open this folder in VS Code: `File → Open Folder`

## How to open in IntelliJ / Android Studio
1. Open IntelliJ IDEA or Android Studio
2. `File → Open` → select this folder
""".trimIndent()

    private fun buildVSCodeSettings(language: Language): String? = when (language) {
        Language.KOTLIN -> """
{
  "editor.formatOnSave": true,
  "kotlin.languageServer.enabled": true,
  "[kotlin]": {
    "editor.defaultFormatter": "fwcd.kotlin"
  }
}""".trimIndent()
        Language.JAVA -> """
{
  "java.home": "",
  "editor.formatOnSave": true,
  "[java]": {
    "editor.defaultFormatter": "redhat.java"
  }
}""".trimIndent()
        Language.CPP, Language.C -> """
{
  "C_Cpp.default.cppStandard": "c++17",
  "editor.formatOnSave": true
}""".trimIndent()
        else -> null
    }
}
