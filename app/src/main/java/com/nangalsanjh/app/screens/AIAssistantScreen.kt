package com.nangalsanjh.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nangalsanjh.app.data.OpenRouterService
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
)

@Composable
fun AIAssistantDialog(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
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
    val suggestions = listOf("Plumber chahiye", "Light nahi hai", "App help")

    // Keyboard detection
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val isImeVisible by remember {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }

    // Auto-scroll when keyboard opens
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return

        val userMsg = ChatMessage(text = text, isUser = true)
        messages = messages + userMsg
        chatHistory.add(Pair("user", text))
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            try {
                val response = OpenRouterService.sendMessage(
                    userMessage = text,
                    chatHistory = chatHistory.dropLast(1)
                )
                chatHistory.add(Pair("model", response))
                messages = messages + ChatMessage(text = response, isUser = false)
            } catch (e: Exception) {
                messages = messages + ChatMessage(
                    text = "❌ Error: ${e.localizedMessage ?: "Something went wrong. Please try again."}",
                    isUser = false
                )
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Nangal AI Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Powered by AI",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Suggestions
                if (messages.size == 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { suggestion ->
                            OutlinedButton(
                                onClick = { sendMessage(suggestion) },
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                            ) {
                                Text(suggestion, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message)
                    }
                    if (isLoading) {
                        item {
                            Row(horizontalArrangement = Arrangement.Start) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🤖", fontSize = 14.sp)
                                        Text(
                                            "Soch raha hoon...",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .imePadding()  // ✅ Keyboard pushes this up
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Kuch bhi poocho...", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isLoading) {
                                    sendMessage(inputText.trim())
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(
                                alpha = 0.3f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (inputText.isNotBlank() && !isLoading)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { sendMessage(inputText.trim()) },
                            enabled = inputText.isNotBlank() && !isLoading,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser)
            Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Text(
                "🤖",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp, top = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = if (message.isUser) 16.dp else 4.dp,
                        topEnd = if (message.isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(
                    if (message.isUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surface
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = if (message.isUser)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}