package com.codelearn.ide.git

import com.codelearn.ide.model.GitCommit
import com.codelearn.ide.model.GitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ─── Git Service (simulated for offline/learning use) ─────────────────────────
class GitService {
    private val _status = MutableStateFlow(GitStatus())
    val status: StateFlow<GitStatus> = _status

    private val commitHistory = mutableListOf<GitCommit>()

    fun initRepository(projectName: String) {
        commitHistory.clear()
        _status.value = GitStatus(
            branch = "main",
            isInitialized = true,
            commits = listOf()
        )
    }

    fun stageFile(fileName: String) {
        val current = _status.value
        val newModified = current.modifiedFiles.filter { it != fileName }
        val newStaged = current.stagedFiles + fileName
        _status.value = current.copy(
            stagedFiles = newStaged,
            modifiedFiles = newModified
        )
    }

    fun unstageFile(fileName: String) {
        val current = _status.value
        val newStaged = current.stagedFiles.filter { it != fileName }
        val newModified = current.modifiedFiles + fileName
        _status.value = current.copy(
            stagedFiles = newStaged,
            modifiedFiles = newModified
        )
    }

    fun stageAll() {
        val current = _status.value
        val allFiles = current.modifiedFiles + current.untrackedFiles
        _status.value = current.copy(
            stagedFiles = current.stagedFiles + allFiles,
            modifiedFiles = emptyList(),
            untrackedFiles = emptyList()
        )
    }

    fun commit(message: String, author: String = "Developer") {
        val current = _status.value
        if (current.stagedFiles.isEmpty()) return

        val commit = GitCommit(
            hash = generateHash(),
            message = message,
            author = author,
            timestamp = System.currentTimeMillis()
        )
        commitHistory.add(0, commit)

        _status.value = current.copy(
            stagedFiles = emptyList(),
            commits = commitHistory.toList()
        )
    }

    fun createBranch(branchName: String) {
        val current = _status.value
        _status.value = current.copy(branch = branchName)
    }

    fun markFileModified(fileName: String) {
        val current = _status.value
        if (!current.modifiedFiles.contains(fileName) && !current.stagedFiles.contains(fileName)) {
            _status.value = current.copy(
                modifiedFiles = current.modifiedFiles + fileName
            )
        }
    }

    fun markFileNew(fileName: String) {
        val current = _status.value
        if (!current.untrackedFiles.contains(fileName)) {
            _status.value = current.copy(
                untrackedFiles = current.untrackedFiles + fileName
            )
        }
    }

    private fun generateHash(): String {
        val chars = "0123456789abcdef"
        return (1..7).map { chars.random() }.joinToString("")
    }

    fun getLog(): List<GitCommit> = commitHistory.toList()
    
    fun getDiff(fileName: String): String {
        return "--- a/$fileName\n+++ b/$fileName\n@@ -1,3 +1,4 @@\n// Changes in $fileName\n+// New line added"
    }
}
