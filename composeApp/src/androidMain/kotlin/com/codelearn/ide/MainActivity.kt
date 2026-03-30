package com.codelearn.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.course.CourseViewModel
import com.codelearn.ide.ui.ChatViewModel
import com.codelearn.ide.ui.ChallengeViewModel
import com.codelearn.ide.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private var vm: AppViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) { vm?.onAppPause() }
            override fun onStop(owner: LifecycleOwner)  { vm?.onAppPause() }
        })
        setContent {
            val appVm       = remember { AppViewModel() }
            val authVm      = remember { AuthViewModel() }
            val courseVm    = remember { CourseViewModel(authVm) }
            val chatVm      = remember { ChatViewModel(authVm) }
            val challengeVm = remember { ChallengeViewModel(authVm) }
            vm = appVm
            App(viewModel = appVm, authViewModel = authVm, courseViewModel = courseVm,
                chatViewModel = chatVm, challengeViewModel = challengeVm)
        }
    }
}
