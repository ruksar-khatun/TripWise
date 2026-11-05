package com.tripwise.core.maps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.annotation.StringRes
import com.tripwise.core.designsystem.component.TwButton
import com.tripwise.core.designsystem.component.TwOutlinedButton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

/**
 * Google Maps without an API key: every map feature hands off to the Google Maps app
 * (or google.com/maps in the browser) through public Maps URLs.
 * https://developers.google.com/maps/documentation/urls/get-started
 */
object GoogleMapsLinks {
    private const val BASE = "https://www.google.com/maps"

    /** Search results / a single place for a free-text [query], e.g. "Amber Fort, Jaipur, Rajasthan". */
    fun searchUrl(query: String): String = "$BASE/search/?api=1&query=${Uri.encode(query)}"

    /** Turn-by-turn directions from the user's location to [destination] (a place name). */
    fun directionsUrl(destination: String): String = "$BASE/dir/?api=1&destination=${Uri.encode(destination)}"

    /** "Restaurants near Jaipur, Rajasthan, India". */
    fun nearbyQuery(what: String, place: String): String = "$what near $place"
}

/** Human-readable place string used in Maps queries: "Jaipur, Rajasthan, India". */
fun placeLabel(name: String, region: String? = null, country: String? = null): String =
    listOfNotNull(name, region?.takeIf { it.isNotBlank() && it != name }, country?.takeIf { it.isNotBlank() }).joinToString(", ")

/** Opens [url] in Google Maps (app if installed, otherwise the browser). Returns false if nothing can open it. */
fun openInGoogleMaps(context: Context, url: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        context.startActivity(intent.setPackage(GOOGLE_MAPS_PACKAGE))
        true
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(intent.setPackage(null))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}

private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

fun openSearch(context: Context, query: String) = openInGoogleMaps(context, GoogleMapsLinks.searchUrl(query))
fun openDirections(context: Context, destination: String) = openInGoogleMaps(context, GoogleMapsLinks.directionsUrl(destination))

/** Things to look up around a destination; each opens a Google Maps search. */
enum class MapsShortcut(@StringRes val label: Int, val query: String, val icon: ImageVector) {
    ATTRACTIONS(R.string.core_maps_attractions, "Tourist attractions", TwIcons.Activity),
    RESTAURANTS(R.string.core_maps_restaurants, "Restaurants", TwIcons.Food),
    HOTELS(R.string.core_maps_hotels, "Hotels", TwIcons.Lodging),
    CAFES(R.string.core_maps_cafes, "Cafes", TwIcons.Food),
    SHOPPING(R.string.core_maps_shopping, "Markets and shopping", TwIcons.Shopping),
    TRANSPORT(R.string.core_maps_transport, "Railway station and bus stand", TwIcons.Transport),
}

/**
 * Card that replaces an embedded map: open the destination in Google Maps, get directions,
 * or search what is around it. [place] is a [placeLabel].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleMapsCard(name: String, place: String, modifier: Modifier = Modifier, shortcuts: List<MapsShortcut> = MapsShortcut.entries) {
    val context = LocalContext.current
    Surface(modifier.fillMaxWidth(), color = TripWiseTheme.colors.surfaceContainerLow, shape = TripWiseTheme.shapes.card) {
        Column(Modifier.padding(TripWiseTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(TwIcons.Map, contentDescription = null, tint = TripWiseTheme.colors.secondary)
                Column(Modifier.padding(start = TripWiseTheme.spacing.md)) {
                    Text(stringResource(R.string.core_maps_card_title, name), style = TripWiseTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.core_maps_card_body),
                        style = TripWiseTheme.typography.bodySmall,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                TwButton(
                    stringResource(R.string.core_maps_open_in_maps),
                    onClick = { openSearch(context, place) },
                    leadingIcon = TwIcons.Map,
                    modifier = Modifier.weight(1f),
                )
                TwOutlinedButton(
                    stringResource(R.string.core_maps_directions),
                    onClick = { openDirections(context, place) },
                    leadingIcon = TwIcons.Place,
                )
            }
            if (shortcuts.isNotEmpty()) {
                Text(stringResource(R.string.core_maps_find_nearby), style = TripWiseTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm)) {
                    shortcuts.forEach { s -> MapsShortcutChip(s, place) }
                }
            }
        }
    }
}

@Composable
fun MapsShortcutChip(shortcut: MapsShortcut, place: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AssistChip(
        onClick = { openSearch(context, GoogleMapsLinks.nearbyQuery(shortcut.query, place)) },
        label = { Text(stringResource(shortcut.label)) },
        leadingIcon = { Icon(shortcut.icon, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) },
        colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = TripWiseTheme.colors.secondary),
        modifier = modifier,
    )
}

@PreviewLightDark
@Composable
private fun GoogleMapsCardPreview() = TripWiseTheme {
    GoogleMapsCard("Jaipur", placeLabel("Jaipur", "Rajasthan", "India"), Modifier.padding(TripWiseTheme.spacing.lg))
}
