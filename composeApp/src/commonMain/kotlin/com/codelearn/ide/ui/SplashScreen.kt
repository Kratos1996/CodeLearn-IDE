package com.codelearn.ide.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SplashScreenContent() {
    val infiniteTransition = rememberInfiniteTransition()

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    val fadeIn by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        IDEColors.bg3,
                        IDEColors.bg1,
                        IDEColors.bg0
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background rings
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .alpha(0.1f)
                .clip(CircleShape)
                .background(IDEColors.accent),
            contentAlignment = Alignment.Center
        ) {}

        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseScale * 0.98f)
                .alpha(0.15f)
                .clip(CircleShape)
                .background(IDEColors.accent),
        ) {}

        Column(
            modifier = Modifier.alpha(fadeIn),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(IDEColors.accent, IDEColors.blue, IDEColors.cyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "</>",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "CodeLearn IDE",
                color = IDEColors.textPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Master every language",
                color = IDEColors.textSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha)
                            .clip(CircleShape)
                            .background(IDEColors.accent)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Kotlin • Java • C# • C++ • C • Ruby • Dart • VB",
                color = IDEColors.textMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
