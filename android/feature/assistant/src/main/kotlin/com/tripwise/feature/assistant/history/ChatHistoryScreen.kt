package com.tripwise.feature.assistant.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.ChatConversation
import com.tripwise.core.model.TripPreferences
import com.tripwise.core.ui.state.EmptyState
import com.tripwise.core.ui.state.LoadingState
import com.tripwise.feature.assistant.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun ChatHistoryScreen(
    onBack: () -> Unit,
    onOpen: (conversationId: String) -> Unit,
    onNewChat: () -> Unit,
    viewModel: ChatHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChatHistoryScreen(state, viewModel::onAction, onBack, onOpen, onNewChat)
}

@Composable
internal fun ChatHistoryScreen(
    state: ChatHistoryUiState,
    onAction: (ChatHistoryAction) -> Unit,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onNewChat: () -> Unit,
) {
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteAll by rememberSaveable { mutableStateOf(false) }
    val hasItems = (state as? ChatHistoryUiState.Ready)?.items?.isNotEmpty() == true
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = stringResource(R.string.feature_assistant_history),
                onNavigateBack = onBack,
                navigateBackContentDescription = stringResource(R.string.feature_assistant_back),
                actions = {
                    if (hasItems) IconButton(onClick = { deleteAll = true }) { Icon(TwIcons.Delete, stringResource(R.string.feature_assistant_delete_all)) }
                },
            )
        },
        floatingActionButton = {
            if (hasItems) {
                ExtendedFloatingActionButton(
                    onClick = onNewChat,
                    icon = { Icon(TwIcons.NewChat, contentDescription = null) },
                    text = { Text(stringResource(R.string.feature_assistant_new_chat)) },
                )
            }
        },
    ) { padding ->
        when (state) {
            ChatHistoryUiState.Loading -> LoadingState(Modifier.padding(padding))
            is ChatHistoryUiState.Ready -> if (state.items.isEmpty()) {
                EmptyState(
                    stringResource(R.string.feature_assistant_history_empty_title),
                    stringResource(R.string.feature_assistant_history_empty_body),
                    Modifier.padding(padding),
                    icon = TwIcons.History,
                    actionLabel = stringResource(R.string.feature_assistant_history_start),
                    onAction = onNewChat,
                )
            } else {
                LazyColumn(
                    Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                ) {
                    items(state.items, key = { it.conversation.id }) { item ->
                        HistoryRow(item, onClick = { onOpen(item.conversation.id) }, onDelete = { deleteId = item.conversation.id })
                    }
                }
            }
        }
    }
    deleteId?.let { id ->
        ConfirmDelete(
            title = stringResource(R.string.feature_assistant_delete_chat_title),
            onConfirm = { onAction(ChatHistoryAction.Delete(id)); deleteId = null },
            onDismiss = { deleteId = null },
        )
    }
    if (deleteAll) {
        ConfirmDelete(
            title = stringResource(R.string.feature_assistant_delete_all_title),
            onConfirm = { onAction(ChatHistoryAction.DeleteAll); deleteAll = false },
            onDismiss = { deleteAll = false },
        )
    }
}

private val timeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

@Composable
private fun HistoryRow(item: ChatHistoryItem, onClick: () -> Unit, onDelete: () -> Unit) {
    val c = item.conversation
    Surface(shape = TripWiseTheme.shapes.card, color = TripWiseTheme.colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(start = TripWiseTheme.spacing.lg, top = TripWiseTheme.spacing.md, bottom = TripWiseTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (c.tripId != null) TwIcons.Trips else if (c.destinationId != null) TwIcons.Place else TwIcons.Assistant, contentDescription = null, tint = TripWiseTheme.colors.secondary)
            Column(Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md)) {
                Text(c.title, style = TripWiseTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                c.preview?.takeIf { it != c.title }?.let {
                    Text(it.replace('\n', ' ').replace("*", ""), style = TripWiseTheme.typography.bodyMedium, color = TripWiseTheme.colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    listOfNotNull(item.destinationName, timeFormat.format(c.updatedAt.atZone(ZoneId.systemDefault()))).joinToString(" · "),
                    style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) { Icon(TwIcons.Delete, stringResource(R.string.feature_assistant_delete_chat)) }
        }
    }
}

@Composable
private fun ConfirmDelete(title: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(stringResource(R.string.feature_assistant_delete_body)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.feature_assistant_delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_assistant_cancel)) } },
    )
}

@PreviewLightDark
@Composable
private fun ChatHistoryPreview() = TripWiseTheme {
    ChatHistoryScreen(
        ChatHistoryUiState.Ready(
            listOf(
                ChatHistoryItem(ChatConversation("1", "Where should I stay in Jaipur?", "jaipur", null, TripPreferences(), Instant.parse("2026-11-01T10:00:00Z"), "Here are places to stay…"), "Jaipur"),
                ChatHistoryItem(ChatConversation("2", "Best hill stations for summer", null, null, TripPreferences(), Instant.parse("2026-10-28T08:00:00Z"), "Munnar, Darjeeling…"), null),
            ),
        ),
        {}, {}, {}, {},
    )
}
