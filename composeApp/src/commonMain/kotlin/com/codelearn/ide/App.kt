package com.codelearn.ide

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.course.CourseViewModel
import com.codelearn.ide.navigation.SplashScreen
import com.codelearn.ide.ui.*
import com.codelearn.ide.viewmodel.AppViewModel
import com.codelearn.ide.viewmodel.LocalAppViewModel

val LocalAuthViewModel      = staticCompositionLocalOf<AuthViewModel>      { error("No AuthViewModel") }
val LocalCourseViewModel    = staticCompositionLocalOf<CourseViewModel>     { error("No CourseViewModel") }
val LocalChatViewModel      = staticCompositionLocalOf<ChatViewModel>       { error("No ChatViewModel") }
val LocalChallengeViewModel = staticCompositionLocalOf<ChallengeViewModel>  { error("No ChallengeViewModel") }

@Composable
fun App(
    viewModel: AppViewModel,
    authViewModel: AuthViewModel,
    courseViewModel: CourseViewModel,
    chatViewModel: ChatViewModel,
    challengeViewModel: ChallengeViewModel
) {
    val savePrompt by viewModel.savePrompt.collectAsState()
    val langSwitch by viewModel.langSwitch.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    CompositionLocalProvider(
        LocalAppViewModel       provides viewModel,
        LocalAuthViewModel      provides authViewModel,
        LocalCourseViewModel    provides courseViewModel,
        LocalChatViewModel      provides chatViewModel,
        LocalChallengeViewModel provides challengeViewModel
    ) {
        IDEThemeWrapper {
            Navigator(screen = SplashScreen) { navigator -> SlideTransition(navigator) }
            SavePromptBottomSheet(state = savePrompt, onDismiss = { viewModel.dismissSavePrompt() })
            LangSwitchDialog(
                state = langSwitch, currentLanguage = selectedLanguage,
                onSaveAndSwitch    = { viewModel.confirmLangSwitch(saveFirst = true) },
                onDiscardAndSwitch = { viewModel.confirmLangSwitch(saveFirst = false) },
                onCancel           = { viewModel.cancelLangSwitch() }
            )
        }
    }
}
