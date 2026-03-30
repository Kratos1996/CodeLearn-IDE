package com.codelearn.ide.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Dark Theme Colors ────────────────────────────────────────────────────────
object IDEColors {
    // Background layers
    val bg0 = Color(0xFF0D0F14)   // deepest background
    val bg1 = Color(0xFF141720)   // sidebar/panel
    val bg2 = Color(0xFF1C2030)   // editor background
    val bg3 = Color(0xFF242840)   // elevated surfaces
    val bg4 = Color(0xFF2E3250)   // hover states

    // Accent
    val accent    = Color(0xFF7C6AF7)  // purple
    val accentBright = Color(0xFF9D8FFF)
    val green     = Color(0xFF4ADE80)
    val red       = Color(0xFFFF5555)
    val orange    = Color(0xFFFFB86C)
    val yellow    = Color(0xFFFFF176)
    val blue      = Color(0xFF60B4FF)
    val cyan      = Color(0xFF67E8F9)
    val pink      = Color(0xFFFF79C6)

    // Text
    val textPrimary   = Color(0xFFF0F1F5)
    val textSecondary = Color(0xFF8892AA)
    val textMuted     = Color(0xFF4E5570)

    // Status
    val success = Color(0xFF4ADE80)
    val warning = Color(0xFFFFB86C)
    val error   = Color(0xFFFF5555)
    val info    = Color(0xFF60B4FF)

    // Line numbers
    val lineNumberBg   = Color(0xFF161A27)
    val lineNumberText = Color(0xFF3D4466)
    val lineNumberActive = Color(0xFF6272A4)

    // Breakpoint
    val breakpointColor = Color(0xFFFF5555)
    val currentLineColor = Color(0xFF2A3050)
    val currentLineBorder = Color(0xFF7C6AF7)
}

val DarkIDETheme = darkColorScheme(
    primary = IDEColors.accent,
    onPrimary = IDEColors.textPrimary,
    secondary = IDEColors.blue,
    background = IDEColors.bg0,
    surface = IDEColors.bg1,
    surfaceVariant = IDEColors.bg2,
    onBackground = IDEColors.textPrimary,
    onSurface = IDEColors.textPrimary,
    error = IDEColors.error,
    tertiary = IDEColors.green
)

@Composable
fun IDEThemeWrapper(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkIDETheme,
        content = content
    )
}
