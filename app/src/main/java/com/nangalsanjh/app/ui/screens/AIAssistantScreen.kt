package com.nangalsanjh.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nangalsanjh.app.data.OpenRouterService
import com.nangalsanjh.app.theme.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.height

// ─── NEW IMPORTS FOR KEYBOARD CONTROL ───
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.activity.compose.BackHandler

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
)

@Composable
fun AIAssistantDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(value = false) }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    text = "Sat Sri Akaal! 🙏 Main Nangal Seva ka AI Assistant hoon!\n\nMain help kar sakta hoon:\n• Civic complaints file karne mein\n• Local workers dhundhne mein\n• App use karne mein\n\nKya chahiye aapko?",
                    isUser = false,
                )
            )
        )
    }
    val chatHistory = remember { mutableListOf<Pair<String, String>>() }
    val suggestions = listOf("Plumber chahiye", "Light nahi hai", "Complaint kaise karein?", "App help")

    // ─── NEW: Focus & Keyboard controllers ───
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto‑focus & pop up keyboard when dialog appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return
        messages += ChatMessage(text = text, isUser = true)
        chatHistory.add(Pair("user", text))
        inputText = ""
        isLoading = true
        scope.launch {
            val response = OpenRouterService.sendMessage(text, chatHistory.dropLast(1))
            chatHistory.add(Pair("model", response))
            messages += ChatMessage(text = response, isUser = false)
            isLoading = false
        }
    }

    // Rendered as an in-app full-screen overlay (not a Dialog window) so that status bar,
    // navigation bar and keyboard insets are handled reliably and the input stays visible.
    BackHandler {
        keyboardController?.hide()
        onDismiss()
    }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {

                // ── Header ───────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AiAvatar(size = 44)
                            Spacer(modifier = Modifier.width(Spacing.md))
                            Column {
                                Text(
                                    "Nangal AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(NangalColors.Green, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Online  ·  Hindi, Punjabi, English",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close assistant",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    NSDivider()
                }

                // ── Messages ─────────────────────────────────────
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message)
                    }

                    if (messages.size == 1) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                Text(
                                    "TRY ASKING  ·  ਪੁੱਛ ਕੇ ਦੇਖੋ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.2.sp
                                )
                                suggestions.forEach { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .clip(Radius.lg)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, Radius.lg)
                                            .clickable(onClickLabel = "Ask: $suggestion") { sendMessage(suggestion) }
                                            .padding(horizontal = Spacing.lg),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            suggestion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item { TypingIndicator() }
                    }
                }

                // ── Input Bar ─────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                    NSDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Ask anything about Nangal…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(26.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        val canSend = inputText.isNotBlank() && !isLoading
                        Surface(
                            shape = Radius.full,
                            color = if (canSend) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(52.dp)
                                .align(Alignment.Bottom),
                            onClick = { if (canSend) sendMessage(inputText.trim()) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = "Send message",
                                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun AiAvatar(size: Int = 32) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(Radius.md)
            .background(MaterialTheme.colorScheme.onBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.size((size * 0.5f).dp)
        )
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(verticalAlignment = Alignment.Bottom) {
        AiAvatar(size = 28)
        Spacer(modifier = Modifier.width(Spacing.sm))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                )
                .padding(horizontal = Spacing.lg, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { i ->
                val alpha by transition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = i * 150),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot$i"
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .alpha(alpha)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val shape = RoundedCornerShape(
        topStart = if (message.isUser) 18.dp else 4.dp,
        topEnd = if (message.isUser) 4.dp else 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 18.dp
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            AiAvatar(size = 28)
            Spacer(modifier = Modifier.width(Spacing.sm))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(
                    if (message.isUser) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.surface
                )
                .then(
                    if (message.isUser) Modifier
                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                )
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isUser) MaterialTheme.colorScheme.background
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                lineHeight = 21.sp
            )
        }
    }
}
