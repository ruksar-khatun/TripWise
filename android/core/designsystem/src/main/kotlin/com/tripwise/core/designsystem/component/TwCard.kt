package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.tripwise.core.designsystem.theme.TripWiseTheme

enum class TwCardStyle { Filled, Elevated, Outlined }

/** Content card with the standard inner padding. Clickable when [onClick] is set. */
@Composable
fun TwCard(
    modifier: Modifier = Modifier,
    style: TwCardStyle = TwCardStyle.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = TripWiseTheme.shapes.card
    val inner: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(TripWiseTheme.spacing.lg), content = content)
    }
    when (style) {
        TwCardStyle.Filled -> {
            val colors = CardDefaults.cardColors(containerColor = TripWiseTheme.colors.surfaceContainerLow)
            if (onClick != null) Card(onClick, modifier, shape = shape, colors = colors, content = inner)
            else Card(modifier, shape = shape, colors = colors, content = inner)
        }
        TwCardStyle.Elevated ->
            if (onClick != null) ElevatedCard(onClick, modifier, shape = shape, content = inner)
            else ElevatedCard(modifier, shape = shape, content = inner)
        TwCardStyle.Outlined ->
            if (onClick != null) OutlinedCard(onClick, modifier, shape = shape, content = inner)
            else OutlinedCard(modifier, shape = shape, content = inner)
    }
}

@PreviewLightDark
@Composable
private fun TwCardPreview() = TripWiseTheme {
    TwCard(style = TwCardStyle.Elevated) {
        Text("Card title", style = TripWiseTheme.typography.titleMedium)
        Text("Supporting text", style = TripWiseTheme.typography.bodyMedium)
    }
}
