package com.tripwise.core.maps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.GeoPoint

enum class PinKind { DESTINATION, ATTRACTION, NEARBY }

data class MapPin(val id: String, val title: String, val position: GeoPoint, val kind: PinKind, val snippet: String? = null)

private fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

private val PinKind.hue: Float
    get() = when (this) {
        PinKind.DESTINATION -> BitmapDescriptorFactory.HUE_AZURE
        PinKind.ATTRACTION -> BitmapDescriptorFactory.HUE_CYAN
        PinKind.NEARBY -> BitmapDescriptorFactory.HUE_ORANGE
    }

/** True when the app was built with a Maps API key (manifest meta-data is non-empty). */
fun isMapsConfigured(context: Context): Boolean = runCatching {
    context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("com.google.android.geo.API_KEY").orEmpty().isNotBlank()
}.getOrDefault(false)

/** Opens Google Maps (or any maps app / the browser) with directions to [target]. */
fun openDirections(context: Context, target: GeoPoint, label: String) {
    val nav = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${target.latitude},${target.longitude}")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, nav))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${target.latitude},${target.longitude}?q=${Uri.encode(label)}")))
    }
}

@Composable
private fun MapUnavailable(modifier: Modifier) {
    Surface(modifier, color = TripWiseTheme.colors.surfaceContainerHigh, shape = TripWiseTheme.shapes.card) {
        Row(Modifier.padding(TripWiseTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(TwIcons.Map, contentDescription = null, tint = TripWiseTheme.colors.secondary)
            Column(Modifier.padding(start = TripWiseTheme.spacing.md)) {
                Text(stringResource(R.string.core_maps_unavailable_title), style = TripWiseTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.core_maps_unavailable_body),
                    style = TripWiseTheme.typography.bodySmall,
                    color = TripWiseTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Small, non-interactive map (lite mode: a static, cheap bitmap) that opens the full map on tap.
 */
@Composable
fun TwMapPreview(center: GeoPoint, pins: List<MapPin>, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if (LocalInspectionMode.current || !isMapsConfigured(context)) return MapUnavailable(modifier)
    val openLabel = stringResource(R.string.core_maps_open_map)
    Box(modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(center.toLatLng(), 11f) },
            googleMapOptionsFactory = { GoogleMapOptions().liteMode(true).mapToolbarEnabled(false) },
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
            onMapClick = { onOpen() },
        ) {
            pins.forEach { pin -> key(pin.id) { Marker(state = remember { MarkerState(pin.position.toLatLng()) }, title = pin.title) } }
        }
        // Transparent overlay: one large, accessible tap target for the whole preview.
        Box(
            Modifier.fillMaxSize().clickable(onClick = onOpen).semantics {
                role = Role.Button
                contentDescription = openLabel
            },
        )
    }
}

/** Interactive map that frames all [pins]; [focus] animates the camera to a pin. */
@Composable
fun TwFullMap(
    pins: List<MapPin>,
    fallbackCenter: GeoPoint,
    modifier: Modifier = Modifier,
    focus: MapPin? = null,
    onPinClick: (MapPin) -> Unit = {},
    cameraState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackCenter.toLatLng(), 11f)
    },
) {
    val context = LocalContext.current
    if (LocalInspectionMode.current || !isMapsConfigured(context)) return MapUnavailable(modifier.padding(TripWiseTheme.spacing.lg))
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        onMapLoaded = {
            if (pins.size > 1) {
                val bounds = LatLngBounds.builder().apply { pins.forEach { include(it.position.toLatLng()) } }.build()
                cameraState.move(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            }
        },
    ) {
        pins.forEach { pin ->
            key(pin.id) {
                Marker(
                    state = remember { MarkerState(pin.position.toLatLng()) },
                    title = pin.title,
                    snippet = pin.snippet,
                    icon = BitmapDescriptorFactory.defaultMarker(pin.kind.hue),
                    onClick = { onPinClick(pin); false },
                )
            }
        }
    }
    LaunchedEffect(focus) {
        focus?.let { cameraState.animate(CameraUpdateFactory.newLatLngZoom(it.position.toLatLng(), 14f), 600) }
    }
}
