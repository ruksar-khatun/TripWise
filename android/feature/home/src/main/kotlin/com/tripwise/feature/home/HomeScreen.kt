package com.tripwise.feature.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.error.AppError
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwCard
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.component.TwSearchLauncher
import com.tripwise.core.designsystem.component.TwSectionHeader
import com.tripwise.core.designsystem.component.TwSkeleton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.SaveableDestination
import com.tripwise.core.model.TravelInterest
import com.tripwise.core.ui.component.DestinationCarouselCard
import com.tripwise.core.ui.component.TripCard
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.preview.PreviewData
import com.tripwise.core.ui.state.SectionErrorCard

/** Navigation out of Home; wired by :app. */
data class HomeNavigation(
    val onDestinationClick: (String) -> Unit,
    val onTripClick: (String) -> Unit,
    val onSearchClick: () -> Unit,
    val onInterestClick: (TravelInterest) -> Unit,
    val onSeeAllDestinations: () -> Unit,
    val onSeeAllSaved: () -> Unit,
    val onPlanTrip: () -> Unit,
    val onAssistantClick: () -> Unit,
    val onProfileClick: () -> Unit,
)

@Composable
internal fun HomeScreen(navigation: HomeNavigation, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(state, viewModel::onAction, navigation)
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    navigation: HomeNavigation,
    modifier: Modifier = Modifier,
) {
    val gutter = TripWiseTheme.spacing.lg
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = TripWiseTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xl),
    ) {
        item(key = "header") { Header(state.greeting, navigation, Modifier.statusBarsPadding().padding(horizontal = gutter)) }

        item(key = "search") {
            TwSearchLauncher(stringResource(R.string.feature_home_search), navigation.onSearchClick, Modifier.padding(horizontal = gutter))
        }

        item(key = "upcoming") {
            Column(Modifier.padding(horizontal = gutter), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                TwSectionHeader(stringResource(R.string.feature_home_upcoming))
                when (val upcoming = state.upcoming) {
                    LoadState.Loading -> TwSkeleton(Modifier.fillMaxWidth(), height = 260.dp, shape = TripWiseTheme.shapes.card)
                    is LoadState.Failed -> SectionErrorCard(upcoming.error, { onAction(HomeAction.Retry) })
                    is LoadState.Ready -> {
                        val data = upcoming.data
                        if (data == null) {
                            NoTripCard(navigation.onPlanTrip)
                        } else {
                            TripCard(data.trip, data.budget, state.today, onClick = { navigation.onTripClick(data.trip.id) })
                        }
                    }
                }
            }
        }

        item(key = "popular") {
            DestinationRow(
                title = stringResource(R.string.feature_home_popular),
                state = state.popular,
                onSeeAll = navigation.onSeeAllDestinations,
                onClick = navigation.onDestinationClick,
                onToggle = { onAction(HomeAction.ToggleSave(it.destination.id, !it.isSaved)) },
                onRetry = { onAction(HomeAction.Retry) },
            )
        }

        item(key = "interests") {
            Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                TwSectionHeader(stringResource(R.string.feature_home_interests), Modifier.padding(horizontal = gutter))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = gutter),
                    horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                ) {
                    TravelInterest.entries.forEach { interest ->
                        TwFilterChip(
                            label = stringResource(interest.ui.label),
                            selected = false,
                            onSelectedChange = { navigation.onInterestClick(interest) },
                            leadingIcon = interest.ui.icon,
                        )
                    }
                }
            }
        }

        item(key = "assistant") { AssistantCard(navigation.onAssistantClick, Modifier.padding(horizontal = gutter)) }

        val saved = state.recentlySaved
        if (saved !is LoadState.Ready || saved.data.isNotEmpty()) {
            item(key = "saved") {
                DestinationRow(
                    title = stringResource(R.string.feature_home_recently_saved),
                    state = saved,
                    onSeeAll = navigation.onSeeAllSaved,
                    onClick = navigation.onDestinationClick,
                    onToggle = { onAction(HomeAction.ToggleSave(it.destination.id, !it.isSaved)) },
                    onRetry = { onAction(HomeAction.Retry) },
                )
            }
        }
    }
}

@Composable
private fun Header(greeting: Greeting, navigation: HomeNavigation, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(top = TripWiseTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(
                    when (greeting) {
                        Greeting.MORNING -> R.string.feature_home_greeting_morning
                        Greeting.AFTERNOON -> R.string.feature_home_greeting_afternoon
                        Greeting.EVENING -> R.string.feature_home_greeting_evening
                    },
                ),
                style = TripWiseTheme.typography.titleMedium,
                color = TripWiseTheme.colors.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.feature_home_headline),
                style = TripWiseTheme.typography.headlineLarge,
                color = TripWiseTheme.colors.primary,
                modifier = Modifier.semantics { heading() },
            )
        }
        IconButton(onClick = navigation.onAssistantClick) {
            Icon(TwIcons.Assistant, stringResource(R.string.feature_home_open_assistant), tint = TripWiseTheme.colors.secondary)
        }
        IconButton(onClick = navigation.onProfileClick) {
            Icon(TwIcons.Profile, stringResource(R.string.feature_home_open_profile), Modifier.size(32.dp), tint = TripWiseTheme.colors.primary)
        }
    }
}

@Composable
private fun DestinationRow(
    title: String,
    state: LoadState<List<SaveableDestination>>,
    onSeeAll: () -> Unit,
    onClick: (String) -> Unit,
    onToggle: (SaveableDestination) -> Unit,
    onRetry: () -> Unit,
) {
    val gutter = TripWiseTheme.spacing.lg
    Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
        TwSectionHeader(title, Modifier.padding(horizontal = gutter), stringResource(R.string.feature_home_see_all), onSeeAll)
        when (state) {
            LoadState.Loading -> Row(Modifier.padding(horizontal = gutter), horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                repeat(3) { TwSkeleton(Modifier.size(width = 168.dp, height = 210.dp), height = 210.dp, shape = TripWiseTheme.shapes.card) }
            }
            is LoadState.Failed -> SectionErrorCard(state.error, onRetry, Modifier.padding(horizontal = gutter))
            is LoadState.Ready -> LazyRow(
                contentPadding = PaddingValues(horizontal = gutter),
                horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
            ) {
                items(state.data, key = { it.destination.id }) { item ->
                    DestinationCarouselCard(item, onClick = { onClick(item.destination.id) }, onToggleSave = { onToggle(item) })
                }
            }
        }
    }
}

@Composable
private fun NoTripCard(onPlanTrip: () -> Unit) {
    TwCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.feature_home_no_trip_title), style = TripWiseTheme.typography.titleMedium)
        Text(
            stringResource(R.string.feature_home_no_trip_body),
            style = TripWiseTheme.typography.bodyMedium,
            color = TripWiseTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(vertical = TripWiseTheme.spacing.sm),
        )
        TwButton(stringResource(R.string.feature_home_plan_trip), onPlanTrip, leadingIcon = TwIcons.Add)
    }
}

@Composable
private fun AssistantCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = TripWiseTheme.shapes.card,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = TripWiseTheme.colors.tertiaryContainer,
            contentColor = TripWiseTheme.colors.onTertiaryContainer,
        ),
    ) {
        Row(Modifier.padding(TripWiseTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(TwIcons.Assistant, contentDescription = null, Modifier.size(32.dp))
            Column(Modifier.weight(1f).padding(horizontal = TripWiseTheme.spacing.md)) {
                Text(stringResource(R.string.feature_home_assistant_title), style = TripWiseTheme.typography.titleMedium)
                Text(stringResource(R.string.feature_home_assistant_body), style = TripWiseTheme.typography.bodyMedium)
            }
            Icon(TwIcons.ChevronRight, contentDescription = null)
        }
    }
}

private val previewNav = HomeNavigation({}, {}, {}, {}, {}, {}, {}, {}, {})

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun HomeReadyPreview() = TripWiseTheme {
    androidx.compose.material3.Surface {
        HomeScreen(
            HomeUiState(
                today = PreviewData.today, greeting = Greeting.EVENING,
                upcoming = LoadState.Ready(UpcomingTrip(PreviewData.trip, PreviewData.budget)),
                popular = LoadState.Ready(PreviewData.destinations.indices.map { PreviewData.saveable(it, it == 1) }),
                recentlySaved = LoadState.Ready(listOf(PreviewData.saveable(1, true))),
            ),
            onAction = {}, navigation = previewNav,
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeLoadingAndErrorPreview() = TripWiseTheme {
    androidx.compose.material3.Surface {
        HomeScreen(
            HomeUiState(
                today = PreviewData.today, greeting = Greeting.MORNING,
                upcoming = LoadState.Ready(null),
                popular = LoadState.Failed(AppError.Network),
            ),
            onAction = {}, navigation = previewNav,
        )
    }
}
