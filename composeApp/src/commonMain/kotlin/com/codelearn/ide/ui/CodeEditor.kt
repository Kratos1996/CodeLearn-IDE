package com.codelearn.ide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelearn.ide.model.AutocompleteSuggestion
import com.codelearn.ide.model.Language
import com.codelearn.ide.model.SuggestionType
import com.codelearn.ide.syntax.SyntaxHighlighter

@Composable
fun CodeEditor(
    code: String,
    language: Language,
    breakpoints: Set<Int>,
    currentDebugLine: Int,
    suggestions: List<AutocompleteSuggestion>,
    showSuggestions: Boolean,
    inlineErrors: List<com.codelearn.ide.ai.InlineError> = emptyList(),
    onCodeChange: (String) -> Unit,
    onCursorChange: (Int) -> Unit,
    onToggleBreakpoint: (Int) -> Unit,
    onSuggestionSelected: (AutocompleteSuggestion) -> Unit,
    onDismissSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = code.lines()
    val highlightedCode = remember(code, language) {
        SyntaxHighlighter.highlight(code, language)
    }
    val scrollState = rememberScrollState()

    Box(modifier = modifier.background(IDEColors.bg2)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ─── Line Numbers Panel ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .fillMaxHeight()
                    .background(IDEColors.lineNumberBg)
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp)
            ) {
                lines.forEachIndexed { index, _ ->
                    val isBreakpoint = breakpoints.contains(index)
                    val isCurrent = index == currentDebugLine

                    val lineError = inlineErrors.firstOrNull { it.line == index }
                    val lineErrorColor = lineError?.let {
                        when (it.type) {
                            com.codelearn.ide.ai.InlineErrorType.ERROR   -> IDEColors.red
                            com.codelearn.ide.ai.InlineErrorType.WARNING -> IDEColors.orange
                            com.codelearn.ide.ai.InlineErrorType.INFO    -> IDEColors.blue
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                            .background(
                                when {
                                    lineError?.type == com.codelearn.ide.ai.InlineErrorType.ERROR ->
                                        IDEColors.red.copy(alpha = 0.12f)
                                    lineError?.type == com.codelearn.ide.ai.InlineErrorType.WARNING ->
                                        IDEColors.orange.copy(alpha = 0.10f)
                                    isCurrent -> IDEColors.currentLineColor
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onToggleBreakpoint(index) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isBreakpoint) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(IDEColors.breakpointColor)
                            )
                            Spacer(Modifier.width(2.dp))
                        } else if (lineErrorColor != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(lineErrorColor)
                            )
                            Spacer(Modifier.width(2.dp))
                        } else {
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrent) IDEColors.lineNumberActive else IDEColors.lineNumberText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            // ─── Editor Area ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = { newCode ->
                        onCodeChange(newCode)
                        onCursorChange(newCode.length)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 64.dp)
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                when {
                                    event.key == Key.Escape -> {
                                        onDismissSuggestions(); false
                                    }
                                    event.isCtrlPressed && event.key == Key.S -> {
                                        false
                                    }
                                    else -> false
                                }
                            } else false
                        },
                    textStyle = TextStyle(
                        color = IDEColors.textPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp,
                        letterSpacing = 0.3.sp
                    ),
                    visualTransformation = {
                        TransformedText(highlightedCode, OffsetMapping.Identity)
                    },
                    cursorBrush = SolidColor(IDEColors.accent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (code.isEmpty()) {
                                Text(
                                    "// Start coding here...",
                                    color = IDEColors.textMuted,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // ─── Autocomplete Dropdown ────────────────────────────────────
                if (showSuggestions && suggestions.isNotEmpty()) {
                    AutocompleteDropdown(
                        suggestions = suggestions,
                        onSuggestionSelected = onSuggestionSelected,
                        onDismiss = onDismissSuggestions,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 60.dp, top = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AutocompleteDropdown(
    suggestions: List<AutocompleteSuggestion>,
    onSuggestionSelected: (AutocompleteSuggestion) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(300.dp)
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = IDEColors.bg3),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionSelected(suggestion) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type icon
                    val (icon, color) = when (suggestion.type) {
                        SuggestionType.KEYWORD  -> "K" to IDEColors.pink
                        SuggestionType.FUNCTION -> "f" to IDEColors.green
                        SuggestionType.CLASS    -> "C" to IDEColors.cyan
                        SuggestionType.VARIABLE -> "v" to IDEColors.orange
                        SuggestionType.SNIPPET  -> "⊞" to IDEColors.yellow
                        SuggestionType.BUILTIN  -> "B" to IDEColors.blue
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            suggestion.text,
                            color = IDEColors.textPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (suggestion.description.isNotEmpty()) {
                            Text(
                                suggestion.description,
                                color = IDEColors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                HorizontalDivider(color = IDEColors.bg4, thickness = 0.5.dp)
            }
        }
    }
}
