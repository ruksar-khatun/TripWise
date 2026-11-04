package com.tripwise.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.component.TwInfoPill
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwTextButton
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.model.Attraction
import com.tripwise.core.model.CurrencyCode
import com.tripwise.core.model.GeoPoint
import com.tripwise.core.model.Money
import com.tripwise.core.ui.R
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.core.ui.label.ui

@Composable
fun durationLabel(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> stringResource(R.string.core_ui_duration_minutes, m)
        m == 0 -> stringResource(R.string.core_ui_duration_hours, h)
        else -> stringResource(R.string.core_ui_duration_hours_minutes, h, m)
    }
}

@Composable
fun costLabel(cost: Money): String =
    if (cost.amountMinor == 0L) stringResource(R.string.core_ui_free) else stringResource(R.string.core_ui_approx_cost, Formatters.money(cost, currentLocale))

/** Curated attraction: category, name, description, duration and typical cost. */
@Composable
fun AttractionCard(
    attraction: Attraction,
    modifier: Modifier = Modifier,
    onShowOnMap: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val category = attraction.category.ui
    Surface(modifier.fillMaxWidth(), shape = TripWiseTheme.shapes.card, color = TripWiseTheme.colors.surfaceContainerLowest) {
        Column(Modifier.padding(TripWiseTheme.spacing.lg).semantics(mergeDescendants = true) {}) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = TripWiseTheme.shapes.chip, color = TripWiseTheme.colors.secondaryContainer) {
                    Icon(category.icon, contentDescription = null, Modifier.padding(6.dp).size(20.dp), tint = TripWiseTheme.colors.onSecondaryContainer)
                }
                Column(Modifier.weight(1f).padding(start = TripWiseTheme.spacing.md)) {
                    Text(attraction.name, style = TripWiseTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(category.label), style = TripWiseTheme.typography.labelMedium, color = TripWiseTheme.colors.secondary)
                }
            }
            Text(
                attraction.description,
                style = TripWiseTheme.typography.bodyMedium,
                color = TripWiseTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(vertical = TripWiseTheme.spacing.sm),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                TwInfoPill(durationLabel(attraction.durationMinutes), icon = TwIcons.Time)
                TwInfoPill(costLabel(attraction.estimatedCost), icon = TwIcons.Wallet, tone = TwPillTone.Success)
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                    if (onShowOnMap != null) TwTextButton(stringResource(com.tripwise.core.ui.R.string.core_ui_show_on_map), onShowOnMap)
                    if (actionLabel != null && onAction != null) TwTextButton(actionLabel, onAction)
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AttractionCardPreview() = TripWiseTheme {
    AttractionCard(
        Attraction("k1", "kyoto", "Fushimi Inari Taisha", ActivityCategory.SIGHTSEEING, GeoPoint(0.0, 0.0), 150,
            Money(0, CurrencyCode("USD")), "Thousands of vermilion torii gates winding up Mount Inari."),
        onShowOnMap = {},
    )
}
