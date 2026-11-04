package com.tripwise.feature.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.error.isRetryable
import com.tripwise.core.designsystem.component.TwInfoPill
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwTextButton
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.ui.component.MarkdownText
import com.tripwise.core.ui.error.messageRes

@Composable
internal fun AssistantScreen(onBack: () -> Unit, viewModel: AssistantViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AssistantScreen(state, viewModel::onAction, onBack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AssistantScreen(state: AssistantUiState, onAction: (AssistantAction) -> Unit, onBack: () -> Unit) {
    val listState = rememberLazyListState()
    val lastText = state.messages.lastOrNull()?.text
    LaunchedEffect(state.messages.size, lastText?.length) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1 + (if (state.error != null) 1 else 0))
    }
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = stringResource(R.string.feature_assistant_title),
                onNavigateBack = onBack,
                navigateBackContentDescription = stringResource(R.string.feature_assistant_back),
            )
        },
        bottomBar = { InputBar(state, onAction) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            state.contextTitle?.let {
                TwInfoPill(
                    stringResource(R.string.feature_assistant_context, it), icon = TwIcons.Place, tone = TwPillTone.Accent,
                    modifier = Modifier.padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.sm),
                )
            }
            if (state.messages.isEmpty()) {
                Intro(onSuggestion = { onAction(AssistantAction.SendSuggestion(it)) }, hasContext = state.contextTitle != null)
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.messages, key = { it.id }) { Bubble(it, streaming = it.id == state.streamingId) }
                    state.error?.let { error -> item(key = "error") { ErrorRow(error, onRetry = { onAction(AssistantAction.Retry) }) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Intro(onSuggestion: (String) -> Unit, hasContext: Boolean) {
    Column(
        Modifier.fillMaxSize().padding(TripWiseTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md, Alignment.CenterVertically),
    ) {
        Icon(TwIcons.Assistant, contentDescription = null, Modifier.size(56.dp), tint = TripWiseTheme.colors.secondary)
        Text(stringResource(R.string.feature_assistant_intro_title), style = TripWiseTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.feature_assistant_intro_body),
            style = TripWiseTheme.typography.bodyMedium,
            color = TripWiseTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 420.dp),
        )
        val suggestions = if (hasContext) {
            listOf(R.string.feature_assistant_suggestion_1, R.string.feature_assistant_suggestion_2, R.string.feature_assistant_suggestion_3, R.string.feature_assistant_suggestion_4)
        } else {
            listOf(R.string.feature_assistant_suggestion_general_1, R.string.feature_assistant_suggestion_general_2, R.string.feature_assistant_suggestion_general_3)
        }.map { stringResource(it) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
            modifier = Modifier.widthIn(max = 480.dp),
        ) {
            suggestions.forEach { s -> SuggestionChip(onClick = { onSuggestion(s) }, label = { Text(s) }) }
        }
    }
}

@Composable
private fun Bubble(message: ChatMessage, streaming: Boolean) {
    val mine = message.role == ChatRole.USER
    val thinking = stringResource(R.string.feature_assistant_thinking)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(20.dp, 20.dp, if (mine) 4.dp else 20.dp, if (mine) 20.dp else 4.dp),
            color = if (mine) TripWiseTheme.colors.primary else TripWiseTheme.colors.tertiaryContainer,
            contentColor = if (mine) TripWiseTheme.colors.onPrimary else TripWiseTheme.colors.onTertiaryContainer,
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(if (mine) 0.85f else 0.92f),
        ) {
            Box(Modifier.padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.md)) {
                if (message.text.isEmpty() && streaming) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics { contentDescription = thinking }) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(thinking, style = TripWiseTheme.typography.bodyMedium, modifier = Modifier.padding(start = TripWiseTheme.spacing.sm))
                    }
                } else if (mine) {
                    Text(message.text, style = TripWiseTheme.typography.bodyLarge)
                } else {
                    MarkdownText(message.text, TripWiseTheme.typography.bodyLarge, Modifier.semantics { if (!streaming) liveRegion = LiveRegionMode.Polite })
                }
            }
        }
    }
}


@Composable
private fun ErrorRow(error: AppError, onRetry: () -> Unit) {
    Surface(shape = TripWiseTheme.shapes.card, color = TripWiseTheme.colors.errorContainer, contentColor = TripWiseTheme.colors.onErrorContainer) {
        Row(Modifier.fillMaxWidth().padding(TripWiseTheme.spacing.md).semantics { liveRegion = LiveRegionMode.Polite }, verticalAlignment = Alignment.CenterVertically) {
            Icon(TwIcons.Error, contentDescription = null)
            Text(stringResource(error.messageRes()), style = TripWiseTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(horizontal = TripWiseTheme.spacing.md))
            if (error.isRetryable) TwTextButton(stringResource(R.string.feature_assistant_retry), onRetry)
        }
    }
}

@Composable
private fun InputBar(state: AssistantUiState, onAction: (AssistantAction) -> Unit) {
    Surface(shadowElevation = 6.dp) {
        Row(
            Modifier.navigationBarsPadding().imePadding().padding(TripWiseTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = { onAction(AssistantAction.InputChanged(it)) },
                placeholder = { Text(stringResource(R.string.feature_assistant_input)) },
                modifier = Modifier.weight(1f),
                maxLines = 5,
                shape = TripWiseTheme.shapes.field,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onAction(AssistantAction.Send) }),
            )
            if (state.sending) {
                FilledIconButton(onClick = { onAction(AssistantAction.Stop) }, modifier = Modifier.padding(start = TripWiseTheme.spacing.sm).size(52.dp)) {
                    Icon(TwIcons.Clear, stringResource(R.string.feature_assistant_stop))
                }
            } else {
                FilledIconButton(
                    onClick = { onAction(AssistantAction.Send) }, enabled = state.canSend,
                    modifier = Modifier.padding(start = TripWiseTheme.spacing.sm).size(52.dp),
                ) { Icon(TwIcons.Send, stringResource(R.string.feature_assistant_send)) }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AssistantPreview() = TripWiseTheme {
    AssistantScreen(
        AssistantUiState(
            contextTitle = "Kyoto in Spring",
            messages = listOf(
                ChatMessage("1", ChatRole.USER, "What should I eat in Kyoto?"),
                ChatMessage("2", ChatRole.ASSISTANT, "Try **kaiseki**, yudofu and matcha sweets:\n- Nishiki Market for snacks\n- Pontocho for dinner"),
            ),
            error = AppError.Ai(com.tripwise.core.common.error.AiFailure.UNAVAILABLE),
        ),
        {}, {},
    )
}
