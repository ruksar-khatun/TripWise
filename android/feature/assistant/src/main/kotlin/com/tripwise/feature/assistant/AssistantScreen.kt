package com.tripwise.feature.assistant

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.error.AiFailure
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.error.isRetryable
import com.tripwise.core.designsystem.component.TwInfoPill
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwTextButton
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.maps.openSearch
import com.tripwise.core.maps.placeLabel
import com.tripwise.core.model.BudgetLevel
import com.tripwise.core.model.ChatMessage
import com.tripwise.core.model.ChatRole
import com.tripwise.core.model.PlaceKind
import com.tripwise.core.model.PlaceSuggestion
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.model.TripPreferences
import com.tripwise.core.ui.component.MarkdownText
import com.tripwise.core.ui.error.messageRes
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.state.LoadingState
import com.tripwise.feature.assistant.navigation.AssistantNavigation

@Composable
internal fun AssistantScreen(navigation: AssistantNavigation, viewModel: AssistantViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AssistantScreen(state, viewModel::onAction, navigation)
}

/** A quick action: either a prompt sent to the chat, structured cards, or the itinerary planner. */
private enum class QuickAction(val label: Int, val icon: ImageVector) {
    ITINERARY(R.string.feature_assistant_action_itinerary, TwIcons.Calendar),
    DESTINATIONS(R.string.feature_assistant_action_destinations, TwIcons.Explore),
    ACTIVITIES(R.string.feature_assistant_action_activities, TwIcons.Activity),
    STAYS(R.string.feature_assistant_action_stays, TwIcons.Lodging),
    FOOD(R.string.feature_assistant_action_food, TwIcons.Food),
}

@Composable
internal fun AssistantScreen(state: AssistantUiState, onAction: (AssistantAction) -> Unit, navigation: AssistantNavigation) {
    var editingPreferences by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val lastText = state.messages.lastOrNull()?.text
    val extraRows = (if (state.recommending != null) 1 else 0) + (if (state.error != null) 1 else 0)
    LaunchedEffect(state.messages.size, lastText?.length, extraRows) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1 + extraRows)
    }
    val quickPrompt = quickPrompts(state)
    val runQuickAction: (QuickAction) -> Unit = { action ->
        when (action) {
            QuickAction.STAYS -> onAction(AssistantAction.Recommend(PlaceKind.STAY))
            QuickAction.FOOD -> onAction(AssistantAction.Recommend(PlaceKind.FOOD))
            else -> onAction(AssistantAction.SendSuggestion(quickPrompt.getValue(action)))
        }
    }

    Scaffold(
        topBar = {
            TwTopAppBar(
                title = stringResource(R.string.feature_assistant_title),
                onNavigateBack = navigation.onBack,
                navigateBackContentDescription = stringResource(R.string.feature_assistant_back),
                actions = {
                    IconButton(onClick = { onAction(AssistantAction.NewChat) }, enabled = state.messages.isNotEmpty()) {
                        Icon(TwIcons.NewChat, stringResource(R.string.feature_assistant_new_chat))
                    }
                    IconButton(onClick = navigation.onOpenHistory) { Icon(TwIcons.History, stringResource(R.string.feature_assistant_history)) }
                },
            )
        },
        bottomBar = {
            Column {
                if (state.messages.isNotEmpty()) {
                    QuickActionRow(
                        enabled = state.ready && !state.busy,
                        showPlanner = state.destinationId != null || state.tripId != null,
                        onAction = runQuickAction,
                        onOpenPlanner = { navigation.onOpenPlanner(state.destinationId, state.tripId) },
                    )
                }
                InputBar(state, onAction)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ContextRow(state, onEditPreferences = { editingPreferences = true })
            when {
                !state.ready -> LoadingState()
                state.messages.isEmpty() -> Intro(state, runQuickAction, onSuggestion = { onAction(AssistantAction.SendSuggestion(it)) })
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.messages, key = { it.id }) { m ->
                        Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                            if (m.text.isNotEmpty() || m.id == state.streamingId) Bubble(m, streaming = m.id == state.streamingId)
                            if (m.places.isNotEmpty()) PlaceCards(m.places, state.destinationName)
                        }
                    }
                    state.recommending?.let { kind -> item(key = "recommending") { Working(kind) } }
                    state.error?.let { error -> item(key = "error") { ErrorRow(error, onRetry = { onAction(AssistantAction.Retry) }) } }
                }
            }
        }
    }
    if (editingPreferences) {
        PreferencesSheet(
            initial = state.preferences,
            onDismiss = { editingPreferences = false },
            onApply = { onAction(AssistantAction.UpdatePreferences(it)); editingPreferences = false },
        )
    }
}

@Composable
private fun quickPrompts(state: AssistantUiState): Map<QuickAction, String> {
    val name = state.destinationName
    val days = state.preferences.days ?: 3
    return mapOf(
        QuickAction.ITINERARY to if (name != null) stringResource(R.string.feature_assistant_suggest_dest_plan, days, name)
        else stringResource(R.string.feature_assistant_prompt_itinerary_general),
        QuickAction.DESTINATIONS to stringResource(R.string.feature_assistant_prompt_destinations),
        QuickAction.ACTIVITIES to if (name != null) stringResource(R.string.feature_assistant_prompt_activities, name)
        else stringResource(R.string.feature_assistant_prompt_activities_general),
    )
}

@Composable
private fun suggestedPrompts(state: AssistantUiState): List<String> {
    val name = state.destinationName
    return when {
        state.tripId != null -> listOf(
            R.string.feature_assistant_suggest_trip_review, R.string.feature_assistant_suggest_trip_gaps,
            R.string.feature_assistant_suggest_trip_budget, R.string.feature_assistant_suggest_trip_pack,
        ).map { stringResource(it) }
        name != null -> listOf(
            stringResource(R.string.feature_assistant_suggest_dest_plan, state.preferences.days ?: 3, name),
            stringResource(R.string.feature_assistant_suggest_dest_budget, name),
            stringResource(R.string.feature_assistant_suggest_dest_food, name),
            stringResource(R.string.feature_assistant_suggest_dest_when, name),
        )
        else -> listOf(
            R.string.feature_assistant_suggestion_general_1, R.string.feature_assistant_suggestion_general_2,
            R.string.feature_assistant_suggestion_general_3, R.string.feature_assistant_suggestion_general_4,
        ).map { stringResource(it) }
    }
}

@Composable
private fun ContextRow(state: AssistantUiState, onEditPreferences: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.contextTitle?.let {
            TwInfoPill(stringResource(R.string.feature_assistant_context, it), icon = if (state.tripId != null) TwIcons.Trips else TwIcons.Place, tone = TwPillTone.Accent)
        }
        AssistChip(
            onClick = onEditPreferences,
            enabled = state.ready,
            label = { Text(preferencesSummary(state.preferences) ?: stringResource(R.string.feature_assistant_preferences_hint)) },
            leadingIcon = { Icon(TwIcons.Tune, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
        )
    }
}

@Composable
internal fun preferencesSummary(p: TripPreferences): String? {
    if (p.isEmpty) return null
    val parts = buildList {
        p.budgetLevel?.let { add(stringResource(it.label)) }
        p.days?.let { add(pluralStringResource(R.plurals.feature_assistant_days_count, it, it)) }
        p.travellers?.let { add(pluralStringResource(R.plurals.feature_assistant_travellers_count, it, it)) }
        val interests = p.interests.sortedBy { it.ordinal }
        interests.take(2).forEach { add(stringResource(it.ui.label)) }
        if (interests.size > 2) add("+${interests.size - 2}")
    }
    return parts.joinToString(" · ")
}

internal val BudgetLevel.label: Int
    get() = when (this) {
        BudgetLevel.BUDGET -> R.string.feature_assistant_budget_low
        BudgetLevel.MID -> R.string.feature_assistant_budget_mid
        BudgetLevel.LUXURY -> R.string.feature_assistant_budget_luxury
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Intro(state: AssistantUiState, onQuickAction: (QuickAction) -> Unit, onSuggestion: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(TripWiseTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
    ) {
        Surface(shape = CircleShape, color = TripWiseTheme.colors.secondaryContainer, modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(TwIcons.Assistant, contentDescription = null, Modifier.size(40.dp), tint = TripWiseTheme.colors.onSecondaryContainer)
            }
        }
        Text(
            stringResource(R.string.feature_assistant_intro_title),
            style = TripWiseTheme.typography.headlineSmall, textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            state.contextTitle?.let { stringResource(R.string.feature_assistant_intro_context, it) } ?: stringResource(R.string.feature_assistant_intro_body),
            style = TripWiseTheme.typography.bodyMedium,
            color = TripWiseTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 420.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
            modifier = Modifier.widthIn(max = 480.dp),
        ) {
            QuickAction.entries.forEach { a -> QuickActionChip(a, enabled = state.ready, onClick = { onQuickAction(a) }) }
        }
        Text(
            stringResource(R.string.feature_assistant_suggested),
            style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary,
            modifier = Modifier.padding(top = TripWiseTheme.spacing.md),
        )
        Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm), horizontalAlignment = Alignment.CenterHorizontally) {
            suggestedPrompts(state).forEach { s -> SuggestionChip(onClick = { onSuggestion(s) }, enabled = state.ready, label = { Text(s) }) }
        }
    }
}

@Composable
private fun QuickActionChip(action: QuickAction, enabled: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = { Text(stringResource(action.label)) },
        leadingIcon = { Icon(action.icon, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = TripWiseTheme.colors.secondary),
    )
}

@Composable
private fun QuickActionRow(enabled: Boolean, showPlanner: Boolean, onAction: (QuickAction) -> Unit, onOpenPlanner: () -> Unit) {
    Surface(color = TripWiseTheme.colors.surface) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = TripWiseTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
        ) {
            if (showPlanner) {
                AssistChip(
                    onClick = onOpenPlanner,
                    label = { Text(stringResource(R.string.feature_assistant_plan_in_planner)) },
                    leadingIcon = { Icon(TwIcons.Assistant, contentDescription = null, Modifier.size(AssistChipDefaults.IconSize)) },
                )
            }
            QuickAction.entries.filter { it != QuickAction.DESTINATIONS }.forEach { a -> QuickActionChip(a, enabled, onClick = { onAction(a) }) }
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

/** Stays / restaurants from TripMate AI, each linked to a Google Maps search (no Maps key needed). */
@Composable
private fun PlaceCards(places: List<PlaceSuggestion>, destinationName: String?) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
        places.forEach { p ->
            Surface(shape = TripWiseTheme.shapes.card, color = TripWiseTheme.colors.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (p.kind == PlaceKind.STAY) TwIcons.Lodging else TwIcons.Food, contentDescription = null, tint = TripWiseTheme.colors.secondary)
                        Column(Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md)) {
                            Text(p.name, style = TripWiseTheme.typography.titleMedium)
                            listOf(p.type, p.area).filter(String::isNotBlank).joinToString(" · ").takeIf { it.isNotEmpty() }?.let {
                                Text(it, style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant)
                            }
                        }
                    }
                    if (p.priceRange.isNotBlank()) TwInfoPill(p.priceRange, icon = TwIcons.Wallet, tone = TwPillTone.Success)
                    if (p.description.isNotBlank()) Text(p.description, style = TripWiseTheme.typography.bodyMedium)
                    val label = stringResource(R.string.feature_assistant_open_maps_for, p.name)
                    TwTextButton(
                        stringResource(R.string.feature_assistant_open_maps),
                        onClick = { openSearch(context, placeLabel(p.name, p.area, destinationName)) },
                        modifier = Modifier.semantics { contentDescription = label },
                    )
                }
            }
        }
        Text(
            stringResource(R.string.feature_assistant_place_disclaimer),
            style = TripWiseTheme.typography.bodySmall, color = TripWiseTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun Working(kind: PlaceKind) {
    val text = stringResource(if (kind == PlaceKind.STAY) R.string.feature_assistant_finding_stays else R.string.feature_assistant_finding_food)
    Row(
        Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }.padding(vertical = TripWiseTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
        Text(text, style = TripWiseTheme.typography.bodyMedium, modifier = Modifier.padding(start = TripWiseTheme.spacing.md))
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
            if (state.busy) {
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

private val previewNavigation = AssistantNavigation({}, {}, { _, _ -> })

@PreviewLightDark
@Composable
private fun AssistantPreview() = TripWiseTheme {
    AssistantScreen(
        AssistantUiState(
            ready = true,
            destinationId = "jaipur",
            destinationName = "Jaipur",
            preferences = TripPreferences(BudgetLevel.MID, days = 3, travellers = 2, interests = setOf(TravelInterest.FOOD)),
            messages = listOf(
                ChatMessage("1", ChatRole.USER, "Where should I eat in Jaipur?"),
                ChatMessage(
                    "2", ChatRole.ASSISTANT, "Here are places to eat in Jaipur worth trying.",
                    places = listOf(PlaceSuggestion("Laxmi Misthan Bhandar", PlaceKind.FOOD, "Johari Bazaar", "Sweets and thali", "₹400-800 for two", "Old-city classic for ghewar and thali.")),
                ),
            ),
            error = AppError.Ai(AiFailure.UNAVAILABLE),
        ),
        {}, previewNavigation,
    )
}

@PreviewLightDark
@Composable
private fun AssistantIntroPreview() = TripWiseTheme {
    AssistantScreen(AssistantUiState(ready = true), {}, previewNavigation)
}
