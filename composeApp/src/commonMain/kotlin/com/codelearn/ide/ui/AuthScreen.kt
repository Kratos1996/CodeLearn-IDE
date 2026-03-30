package com.codelearn.ide.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.auth.AuthState

@Composable
fun AuthScreenContent(
    authVm: AuthViewModel,
    onAuthSuccess: () -> Unit = {}
) {
    val authState by authVm.authState.collectAsState()
    val isLoading by authVm.isLoading.collectAsState()
    val errorMessage by authVm.errorMessage.collectAsState()

    // Navigate to home as soon as login/signup succeeds
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn) {
            onAuthSuccess()
        }
    }

    var isSignUp by remember { mutableStateOf(false) }
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(IDEColors.bg0, IDEColors.bg1, Color(0xFF0A0D18))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(IDEColors.accent, IDEColors.blue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("</> ", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
            Text("CodeLearn IDE", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                color = IDEColors.textPrimary)
            Text(
                if (isSignUp) "Create your account to start learning"
                else "Welcome back! Sign in to continue",
                fontSize = 14.sp, color = IDEColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Card ──────────────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IDEColors.bg2,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text(
                        if (isSignUp) "Sign Up" else "Sign In",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        color = IDEColors.textPrimary
                    )

                    // Name field (sign up only)
                    AnimatedVisibility(visible = isSignUp) {
                        AuthTextField(
                            value = name, onValueChange = { name = it },
                            label = "Full Name",
                            placeholder = "e.g. Ishant Sharma",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                    }

                    // Email
                    AuthTextField(
                        value = email, onValueChange = { email = it; authVm.clearError() },
                        label = "Email",
                        placeholder = "your@email.com",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Password
                    AuthTextField(
                        value = password, onValueChange = { password = it; authVm.clearError() },
                        label = "Password",
                        placeholder = if (isSignUp) "At least 6 characters" else "Your password",
                        isPassword = true,
                        showPassword = showPass,
                        onTogglePassword = { showPass = !showPass },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (isSignUp) authVm.signUp(name, email, password)
                            else authVm.signIn(email, password)
                        })
                    )

                    // Error message
                    AnimatedVisibility(visible = errorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(IDEColors.red.copy(alpha = 0.12f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(errorMessage ?: "", color = IDEColors.red, fontSize = 13.sp)
                        }
                    }

                    // Submit button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (isSignUp) authVm.signUp(name, email, password)
                            else authVm.signIn(email, password)
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IDEColors.accent,
                            disabledContainerColor = IDEColors.accent.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White, strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isSignUp) "Create Account" else "Sign In",
                                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Toggle sign in / sign up ───────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    color = IDEColors.textSecondary, fontSize = 14.sp
                )
                Text(
                    if (isSignUp) "Sign In" else "Sign Up",
                    color = IDEColors.accent, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        isSignUp = !isSignUp
                        authVm.clearError()
                        name = ""; email = ""; password = ""
                    }
                )
            }

            // ── Guest mode ────────────────────────────────────────────────────
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onAuthSuccess) {
                Text("Continue as Guest (no sync)", color = IDEColors.textMuted, fontSize = 13.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Reusable text field ────────────────────────────────────────────────────────

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = IDEColors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = IDEColors.textMuted, fontSize = 14.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) ({
                Text(
                    if (showPassword) "🙈" else "👁",
                    fontSize = 18.sp,
                    modifier = Modifier.clickable(onClick = onTogglePassword).padding(8.dp)
                )
            }) else null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = IDEColors.accent,
                unfocusedBorderColor = IDEColors.bg4,
                focusedTextColor     = IDEColors.textPrimary,
                unfocusedTextColor   = IDEColors.textPrimary,
                cursorColor          = IDEColors.accent,
                focusedContainerColor   = IDEColors.bg3,
                unfocusedContainerColor = IDEColors.bg3
            )
        )
    }
}