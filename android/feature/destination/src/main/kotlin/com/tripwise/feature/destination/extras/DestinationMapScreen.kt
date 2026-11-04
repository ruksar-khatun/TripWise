package com.tripwise.feature.destination.extras

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.common.result.LoadState
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwOverlayIconButton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.maps.MapPin
import com.tripwise.core.maps.PinKind
import com.tripwise.core.maps.TwFullMap
import com.tripwise.core.maps.openDirections
import com.tripwise.core.ui.label.ui
import com.tripwise.core.ui.state.ErrorState
import com.tripwise.core.ui.state.LoadingState
import com.tripwise.feature.destination.R

/** Full map: destination, curated attractions and (if available) nearby Google Places, with directions. */
@Composable
internal fun DestinationMapScreen(destinationId: String, onBack: () -> Unit) {
    val vm = hiltViewModel<DestinationExtrasViewModel, DestinationExtrasViewModel.Factory>(key = "extras-$destinationId") { it.create(destinationId) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val d = (state.destination as? LoadState.Ready)?.data
    LaunchedEffect(d?.id) { if (d != null) vm.onAction(DestinationExtrasAction.LoadNearby) }
    var focus by remember { mutableStateOf<MapPin?>(null) }

    Box(Modifier.fillMaxSize()) {
        when (val dest = state.destination) {
            LoadState.Loading -> LoadingState()
            is LoadState.Failed -> ErrorState(dest.error, onRetry = { vm.onAction(DestinationExtrasAction.Retry) })
            is LoadState.Ready -> dest.data?.let { destination ->
                val nearby = (state.nearby as? LoadState.Ready)?.data.orEmpty()
                val pins = buildList {
                    add(MapPin(destination.id, destination.name, destination.location, PinKind.DESTINATION, destination.country))
                    destination.attractions.forEach { add(MapPin(it.id, it.name, it.location, PinKind.ATTRACTION)) }
                    nearby.forEach { add(MapPin("place-${it.id}", it.name, it.location, PinKind.NEARBY, it.category)) }
                }
                TwFullMap(pins, destination.location, Modifier.fillMaxSize(), focus = focus, onPinClick = { focus = it })
                LazyRow(
                    Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                    contentPadding = PaddingValues(TripWiseTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md),
                ) {
                    items(destination.attractions, key = { it.id }) { a ->
                        Card(
                            onClick = { focus = pins.first { it.id == a.id } },
                            modifier = Modifier.width(240.dp),
                            colors = CardDefaults.cardColors(containerColor = TripWiseTheme.colors.surfaceContainerLowest),
                            shape = TripWiseTheme.shapes.card,
                        ) {
                            Column(Modifier.padding(TripWiseTheme.spacing.md)) {
                                Text(a.name, style = TripWiseTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(stringResource(a.category.ui.label), style = TripWiseTheme.typography.labelMedium, color = TripWiseTheme.colors.secondary)
                                TwButton(stringResource(com.tripwise.core.maps.R.string.core_maps_directions), { openDirections(context, a.location, a.name) },
                                    Modifier.padding(top = TripWiseTheme.spacing.sm))
                            }
                        }
                    }
                }
            }
        }
        TwOverlayIconButton(TwIcons.Back, stringResource(R.string.feature_destination_back), onBack,
            Modifier.statusBarsPadding().padding(TripWiseTheme.spacing.sm))
    }
}
