package com.codelearn.ide

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.course.CourseViewModel
import com.codelearn.ide.ui.ChatViewModel
import com.codelearn.ide.ui.ChallengeViewModel
import com.codelearn.ide.viewmodel.AppViewModel

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "CodeLearn IDE") {
        val appVm       = remember { AppViewModel() }
        val authVm      = remember { AuthViewModel() }
        val courseVm    = remember { CourseViewModel(authVm) }
        val chatVm      = remember { ChatViewModel(authVm) }
        val challengeVm = remember { ChallengeViewModel(authVm) }
        App(viewModel = appVm, authViewModel = authVm, courseViewModel = courseVm,
            chatViewModel = chatVm, challengeViewModel = challengeVm)
    }
}
