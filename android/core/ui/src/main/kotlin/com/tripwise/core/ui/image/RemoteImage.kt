package com.tripwise.core.ui.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tripwise.core.designsystem.component.TwImagePlaceholder
import com.tripwise.core.model.ImageRef

/**
 * Loads a remote image over the branded gradient placeholder. The placeholder stays visible
 * underneath while loading, when offline, or if the URL fails, so layouts never show a blank hole.
 * Pass [contentDescription] = null for decorative images (the card's text already names the place).
 */
@Composable
fun RemoteImage(
    image: ImageRef?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showPlaceholderIcon: Boolean = true,
) {
    Box(modifier) {
        TwImagePlaceholder(Modifier.fillMaxSize(), showIcon = showPlaceholderIcon)
        if (image != null && !LocalInspectionMode.current) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image.url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
