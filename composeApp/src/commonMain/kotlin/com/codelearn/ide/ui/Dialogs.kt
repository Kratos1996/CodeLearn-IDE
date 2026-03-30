package com.codelearn.ide.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codelearn.ide.model.Language
import com.codelearn.ide.viewmodel.LangSwitchState
import com.codelearn.ide.viewmodel.SavePromptState

// ─── Save Prompt BottomSheet ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePromptBottomSheet(
    state: SavePromptState,
    onDismiss: () -> Unit
) {
    if (!state.visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { state.onCancel(); onDismiss() },
        sheetState = sheetState,
        containerColor = IDEColors.bg2,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IDEColors.bg4)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IDEColors.orange.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💾", fontSize = 22.sp)
                }
                Column {
                    Text(
                        "Unsaved Changes",
                        color = IDEColors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        state.fileName,
                        color = IDEColors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                "Do you want to save your changes before leaving?",
                color = IDEColors.textSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Save button
            Button(
                onClick = state.onSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("💾  Save", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))

            // Discard button
            OutlinedButton(
                onClick = state.onDiscard,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IDEColors.red),
                border = BorderStroke(1.dp, IDEColors.red.copy(0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("🗑  Discard Changes", color = IDEColors.red, fontSize = 15.sp)
            }

            Spacer(Modifier.height(10.dp))

            // Cancel button
            TextButton(
                onClick = state.onCancel,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel", color = IDEColors.textSecondary, fontSize = 15.sp)
            }
        }
    }
}

// ─── Language Switch Dialog ───────────────────────────────────────────────────
@Composable
fun LangSwitchDialog(
    state: LangSwitchState,
    currentLanguage: Language,
    onSaveAndSwitch: () -> Unit,
    onDiscardAndSwitch: () -> Unit,
    onCancel: () -> Unit
) {
    if (!state.visible || state.targetLanguage == null) return

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = IDEColors.bg2,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(currentLanguage.icon, fontSize = 22.sp)
                Text("→", color = IDEColors.textSecondary, fontSize = 16.sp)
                Text(state.targetLanguage.icon, fontSize = 22.sp)
                Text(
                    "Switch Language?",
                    color = IDEColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Switching to ${state.targetLanguage.displayName} will replace the current editor content with the ${state.targetLanguage.displayName} starter template.",
                    color = IDEColors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IDEColors.orange.copy(0.08f))
                        .border(1.dp, IDEColors.orange.copy(0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        "⚠️  Your current code will be lost unless you save first.",
                        color = IDEColors.orange,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSaveAndSwitch,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("💾  Save & Switch to ${state.targetLanguage.displayName}", color = Color.White)
                }
                OutlinedButton(
                    onClick = onDiscardAndSwitch,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IDEColors.red),
                    border = BorderStroke(1.dp, IDEColors.red.copy(0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Switch without saving", color = IDEColors.red)
                }
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = IDEColors.textSecondary)
                }
            }
        },
        dismissButton = {}
    )
}
