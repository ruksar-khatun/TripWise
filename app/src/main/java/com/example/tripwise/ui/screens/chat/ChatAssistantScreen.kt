package com.example.tripwise.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripwise.data.AppState
import com.example.tripwise.data.chat.ChatMessage
import com.example.tripwise.ui.theme.AzureMist
import com.example.tripwise.ui.theme.DuskBlue
import com.example.tripwise.ui.theme.SeaBlue
import com.example.tripwise.ui.theme.SeaSalt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAssistantScreen(
    appState: AppState,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SeaBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SeaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TripWise AI",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuskBlue
                            )
                            Text(
                                text = "Smart Travel Assistant",
                                fontSize = 12.sp,
                                color = DuskBlue.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DuskBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onClearChat() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = DuskBlue.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureMist)
            )
        },
        containerColor = AzureMist
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SeaBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "TripWise AI is thinking...",
                                fontSize = 13.sp,
                                color = DuskBlue.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Network / API Error Bar if applicable
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Network Connection Error",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.onRetry(appState) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Suggested Prompt Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.suggestedPrompts) { prompt ->
                    SuggestionChip(
                        onClick = { viewModel.onSuggestedPromptClick(prompt, appState) },
                        label = { Text(text = prompt, fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color.White,
                            labelColor = DuskBlue
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = SeaBlue.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Bottom Input Row
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputTextChange(it) },
                        placeholder = {
                            Text(
                                text = "Ask anything about Asian travel...",
                                fontSize = 14.sp,
                                color = DuskBlue.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AzureMist.copy(alpha = 0.5f),
                            unfocusedContainerColor = AzureMist.copy(alpha = 0.3f),
                            focusedBorderColor = SeaBlue,
                            unfocusedBorderColor = DuskBlue.copy(alpha = 0.2f)
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = { viewModel.onSendMessage(appState) },
                        enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (uiState.inputText.isNotBlank() && !uiState.isLoading) SeaBlue else DuskBlue.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = when {
                message.isError -> MaterialTheme.colorScheme.errorContainer
                isUser -> SeaBlue
                else -> Color.White
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            shadowElevation = if (isUser) 1.dp else 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = when {
                        message.isError -> MaterialTheme.colorScheme.onErrorContainer
                        isUser -> Color.White
                        else -> DuskBlue
                    },
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = when {
                        message.isError -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        isUser -> Color.White.copy(alpha = 0.75f)
                        else -> DuskBlue.copy(alpha = 0.45f)
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
