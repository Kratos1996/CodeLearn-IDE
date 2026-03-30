package com.codelearn.ide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.firebase.ChatMessage
import com.codelearn.ide.firebase.FirebaseService
import com.codelearn.ide.model.Language
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ─── Chat ViewModel ───────────────────────────────────────────────────────────

class ChatViewModel(private val authVm: AuthViewModel) : ScreenModel {

    private val _messages    = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isSending   = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _activeRoom  = MutableStateFlow("general")
    val activeRoom: StateFlow<String> = _activeRoom

    // Offline queue: messages sent while offline, delivered when back online
    private val _pendingQueue = mutableListOf<ChatMessage>()

    fun openRoom(roomId: String) {
        _activeRoom.value = roomId
        _messages.value = emptyList()
        startPolling(roomId)
    }

    private fun startPolling(roomId: String) {
        screenModelScope.launch {
            while (isActive) {
                loadMessages(roomId)
                delay(5000L) // Poll every 5 seconds
            }
        }
    }

    private suspend fun loadMessages(roomId: String) {
        val token = authVm.idToken
        if (token.isBlank()) return
        val fetched = FirebaseService.loadChatMessages(roomId, token, limit = 50)
        if (fetched.isNotEmpty()) _messages.value = fetched
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        val user  = authVm.currentUser ?: return
        val token = authVm.idToken

        val msg = ChatMessage(uid = user.uid, name = user.name, text = trimmed,
            ts = System.currentTimeMillis())

        // Optimistic update — show immediately
        _messages.value = _messages.value + msg

        screenModelScope.launch {
            _isSending.value = true
            if (token.isNotBlank()) {
                FirebaseService.sendChatMessage(
                    roomId = _activeRoom.value, idToken = token,
                    senderUid = user.uid, senderName = user.name, text = trimmed
                )
                // Flush pending queue
                _pendingQueue.toList().forEach { pending ->
                    FirebaseService.sendChatMessage(_activeRoom.value, token,
                        pending.uid, pending.name, pending.text)
                }
                _pendingQueue.clear()
            } else {
                // Offline — queue for later
                _pendingQueue.add(msg)
            }
            _isSending.value = false
        }
    }
}

// ─── Chat Room List Screen ────────────────────────────────────────────────────

@Composable
fun ChatRoomListScreen(
    chatVm: ChatViewModel,
    authVm: AuthViewModel,
    onOpenRoom: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val rooms = listOf(
        Triple("general",    "💬", "General"),
        Triple("kt",         "🟣", "Kotlin"),
        Triple("py",         "🐍", "Python"),
        Triple("js",         "🟨", "JavaScript"),
        Triple("java",       "☕", "Java"),
        Triple("cpp",        "🔵", "C++"),
        Triple("c",          "🔷", "C"),
        Triple("cs",         "💜", "C#"),
        Triple("rb",         "🔴", "Ruby"),
        Triple("dart",       "🎯", "Dart"),
        Triple("vb",         "🟦", "Visual Basic"),
        Triple("help",       "❓", "Help & Questions"),
        Triple("showcase",   "🏆", "Code Showcase")
    )

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
            Text("💬 Community", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = IDEColors.textPrimary)
        }

        LazyColumn(contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {

            item {
                Text("Choose a room to chat with other students",
                    color = IDEColors.textSecondary, fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp))
            }

            items(rooms) { (roomId, icon, name) ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IDEColors.bg2)
                        .border(1.dp, IDEColors.bg4, RoundedCornerShape(12.dp))
                        .clickable {
                            chatVm.openRoom(roomId)
                            onOpenRoom(roomId)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(IDEColors.bg3),
                        contentAlignment = Alignment.Center
                    ) { Text(icon, fontSize = 20.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = IDEColors.textPrimary, fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold)
                        Text("Tap to join • Language discussion",
                            color = IDEColors.textMuted, fontSize = 12.sp)
                    }
                    Text("→", color = IDEColors.accent, fontSize = 16.sp)
                }
            }
            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

// ─── Chat Room Screen ─────────────────────────────────────────────────────────

@Composable
fun ChatRoomScreen(
    roomName: String,
    chatVm: ChatViewModel,
    authVm: AuthViewModel,
    onBack: () -> Unit = {}
) {
    val messages  by chatVm.messages.collectAsState()
    val isSending by chatVm.isSending.collectAsState()
    val currentUid = authVm.currentUid
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 4.dp))
            Text(roomName, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                color = IDEColors.textPrimary, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape)
                    .background(IDEColors.green)
            )
            Text("Live", color = IDEColors.green, fontSize = 11.sp)
        }

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💬", fontSize = 40.sp)
                            Text("No messages yet", color = IDEColors.textSecondary, fontSize = 15.sp)
                            Text("Be the first to say hello!",
                                color = IDEColors.textMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
            items(messages, key = { "${it.uid}_${it.ts}" }) { msg ->
                val isMe = msg.uid == currentUid
                ChatBubble(msg = msg, isMe = isMe)
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Message...", color = IDEColors.textMuted, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        chatVm.sendMessage(inputText)
                        inputText = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = IDEColors.accent,
                    unfocusedBorderColor = IDEColors.bg4,
                    focusedTextColor     = IDEColors.textPrimary,
                    unfocusedTextColor   = IDEColors.textPrimary,
                    cursorColor          = IDEColors.accent,
                    focusedContainerColor   = IDEColors.bg2,
                    unfocusedContainerColor = IDEColors.bg2
                )
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) IDEColors.accent else IDEColors.bg3)
                    .clickable(enabled = inputText.isNotBlank() && !isSending) {
                        chatVm.sendMessage(inputText)
                        inputText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("▶", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage, isMe: Boolean) {
    val initials = msg.name.take(2).uppercase()
    val time = remember(msg.ts) {
        val diff = System.currentTimeMillis() - msg.ts
        when {
            diff < 60_000   -> "just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            else -> "${diff / 3_600_000}h ago"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            // Avatar
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(IDEColors.accent.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) { Text(initials, fontSize = 12.sp, color = IDEColors.accent, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isMe) {
                Text(msg.name, color = IDEColors.accent, fontSize = 11.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        )
                    )
                    .background(if (isMe) IDEColors.accent else IDEColors.bg2)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(msg.text,
                    color = if (isMe) Color.White else IDEColors.textPrimary,
                    fontSize = 14.sp, lineHeight = 20.sp)
            }
            Text(time, color = IDEColors.textMuted, fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp))
        }

        if (isMe) Spacer(Modifier.width(6.dp))
    }
}
