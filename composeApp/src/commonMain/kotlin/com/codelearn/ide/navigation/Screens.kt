package com.codelearn.ide.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.codelearn.ide.LocalAuthViewModel
import com.codelearn.ide.LocalCourseViewModel
import com.codelearn.ide.LocalChatViewModel
import com.codelearn.ide.LocalChallengeViewModel
import com.codelearn.ide.auth.AuthState
import com.codelearn.ide.ui.*
import com.codelearn.ide.viewmodel.LocalAppViewModel

// ─── Splash ───────────────────────────────────────────────────────────────────
object SplashScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val vm = LocalAppViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        val splashReady by vm.splashReady.collectAsState()
        LaunchedEffect(splashReady) {
            if (splashReady) navigator.replace(HomeScreen)
        }
        SplashScreenContent()
    }
}

// ─── Home ─────────────────────────────────────────────────────────────────────
object HomeScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val vm = LocalAppViewModel.current
        val authVm = LocalAuthViewModel.current
        val courseVm = LocalCourseViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        // Auth guard — redirects to AuthScreen if not logged in
        val authState by authVm.authState.collectAsState()
        val isLoggedIn = authState is com.codelearn.ide.auth.AuthState.LoggedIn

        fun requireAuth(action: () -> Unit) {
            if (isLoggedIn) action()
            else navigator.push(AuthScreen)
        }

        HomeScreenContent(
            vm = vm,
            authVm = authVm,
            courseVm = courseVm,
            onOpenIDE        = { requireAuth { navigator.push(IDEScreen) } },
            onOpenQuiz       = { requireAuth { navigator.push(QuizListScreen) } },
            onOpenCourses    = { requireAuth { navigator.push(CoursesScreen) } },
            onOpenProfile    = { navigator.push(ProfileScreen) },
            onOpenChat       = { requireAuth { navigator.push(ChatListScreen) } },
            onOpenChallenge  = { requireAuth { navigator.push(ChallengeScreenObj) } },
            onOpenProblem    = { problem ->
                requireAuth {
                    vm.selectProblem(problem)
                    navigator.push(QuizDetailScreen)
                }
            },
            onCreateProject  = { lang, name ->
                requireAuth {
                    vm.createNewProject(name, lang)
                    navigator.push(IDEScreen)
                }
            }
        )
    }
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
object AuthScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val authVm = LocalAuthViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        AuthScreenContent(
            authVm = authVm,
            onAuthSuccess = {
                // If came from Profile (pushed), pop back so Profile shows updated data
                // If at root (splash replaced to auth), go to Home
                if (navigator.canPop) navigator.pop()
                else navigator.replace(HomeScreen)
            }
        )
    }
}

// ─── Courses ──────────────────────────────────────────────────────────────────
object CoursesScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val courseVm = LocalCourseViewModel.current
        val authVm   = LocalAuthViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        val activeCourse by courseVm.activeCourse.collectAsState()
        val activeStage  by courseVm.activeStage.collectAsState()
        val examActive   by courseVm.examActive.collectAsState()

        // Key determines which sub-screen is shown — drives smooth AnimatedContent transition
        val screenKey = when {
            examActive && activeStage != null -> "exam"
            activeStage != null               -> "lesson"
            activeCourse != null              -> "stages"
            else                              -> "home"
        }

        AnimatedContent(
            targetState = screenKey,
            transitionSpec = {
                fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) togetherWith
                        fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
            },
            label = "CourseNavigation"
        ) { key ->
            when (key) {
                "exam"   -> ExamScreen(
                    stage    = activeStage!!,
                    courseVm = courseVm,
                    onBack   = { courseVm.clearActiveStage() }
                )
                "lesson" -> LessonScreen(
                    stage        = activeStage!!,
                    lessonIndex  = courseVm.activeLessonIndex.collectAsState().value,
                    courseVm     = courseVm,
                    onBack       = { courseVm.clearActiveStage() }
                )
                "stages" -> StageListScreen(
                    course   = activeCourse!!,
                    courseVm = courseVm,
                    onBack   = { courseVm.clearActiveCourse() }
                )
                else     -> CourseHomeScreen(
                    courseVm = courseVm,
                    authVm   = authVm,
                    onBack   = { navigator.pop() }
                )
            }
        }
    }
}

// ─── Profile ──────────────────────────────────────────────────────────────────
object ProfileScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val authVm   = LocalAuthViewModel.current
        val courseVm = LocalCourseViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        ProfileScreenContent(
            authVm    = authVm,
            courseVm  = courseVm,
            onBack    = { navigator.pop() },
            onSignIn  = { navigator.push(AuthScreen) },
            onSignOut = {
                authVm.signOut()
                navigator.replaceAll(AuthScreen)
            }
        )
    }
}

// ─── Chat ─────────────────────────────────────────────────────────────────────
object ChatListScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val chatVm   = LocalChatViewModel.current
        val authVm   = LocalAuthViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        ChatRoomListScreen(
            chatVm   = chatVm,
            authVm   = authVm,
            onOpenRoom = { navigator.push(ChatRoomScreenObj) },
            onBack   = { navigator.pop() }
        )
    }
}

object ChatRoomScreenObj : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val chatVm   = LocalChatViewModel.current
        val authVm   = LocalAuthViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        val activeRoom by chatVm.activeRoom.collectAsState()
        ChatRoomScreen(
            roomName  = activeRoom,
            chatVm    = chatVm,
            authVm    = authVm,
            onBack    = { navigator.pop() }
        )
    }
}

// ─── Challenge ────────────────────────────────────────────────────────────────
object ChallengeScreenObj : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val challengeVm = LocalChallengeViewModel.current
        val authVm      = LocalAuthViewModel.current
        val navigator   = LocalNavigator.currentOrThrow
        ChallengeScreen(
            challengeVm = challengeVm,
            authVm      = authVm,
            onBack      = { navigator.pop() }
        )
    }
}

// ─── IDE ──────────────────────────────────────────────────────────────────────
object IDEScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val vm = LocalAppViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        IDEScreenContent(
            vm = vm,
            onNavigateHome = {
                vm.checkUnsavedBeforeAction(
                    onClear = { navigator.popUntilRoot() },
                    onCancel = {}
                )
            },
            onNavigateQuiz = { navigator.push(QuizListScreen) }
        )
    }
}

// ─── Quiz List ────────────────────────────────────────────────────────────────
object QuizListScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val vm = LocalAppViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        QuizListContent(
            vm = vm,
            onOpenProblem = { problem ->
                vm.selectProblem(problem)
                navigator.push(QuizDetailScreen)
            },
            onBack = { navigator.pop() }
        )
    }
}

// ─── Quiz Detail ──────────────────────────────────────────────────────────────
object QuizDetailScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey
    @Composable override fun Content() {
        val vm = LocalAppViewModel.current
        val navigator = LocalNavigator.currentOrThrow
        QuizDetailContent(vm = vm, onBack = { navigator.pop() })
    }
}