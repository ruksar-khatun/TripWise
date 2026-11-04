package com.tripwise.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.component.TwInfoPill
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwProgressBar
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.BudgetSummary
import com.tripwise.core.model.Trip
import com.tripwise.core.model.TripStatus
import com.tripwise.core.ui.R
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.core.ui.image.RemoteImage
import com.tripwise.core.ui.preview.PreviewData
import java.time.LocalDate

/** Trip summary card (Home hero, My Trips list): cover photo, countdown, dates and budget. */
@Composable
fun TripCard(
    trip: Trip,
    budget: BudgetSummary?,
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = currentLocale
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = TripWiseTheme.shapes.card,
        colors = CardDefaults.cardColors(containerColor = TripWiseTheme.colors.surfaceContainerLowest),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            RemoteImage(trip.coverImage, contentDescription = null, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(TripWiseTheme.extendedColors.imageScrim))
            TripStatusPill(trip, today, Modifier.align(Alignment.TopStart).padding(TripWiseTheme.spacing.md))
            Column(Modifier.align(Alignment.BottomStart).padding(TripWiseTheme.spacing.lg)) {
                Text(
                    trip.title,
                    style = TripWiseTheme.typography.titleLarge,
                    color = TripWiseTheme.extendedColors.onImage,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    trip.destinationName,
                    style = TripWiseTheme.typography.bodyMedium,
                    color = TripWiseTheme.extendedColors.onImage.copy(alpha = 0.9f),
                )
            }
        }
        Column(
            Modifier.padding(TripWiseTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Formatters.dateRange(trip.startDate, trip.endDate, locale, today),
                    style = TripWiseTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    pluralStringResource(R.plurals.core_ui_travellers, trip.travellers, trip.travellers),
                    style = TripWiseTheme.typography.bodyMedium,
                    color = TripWiseTheme.colors.onSurfaceVariant,
                )
            }
            if (budget != null) BudgetProgress(budget)
        }
    }
}

@Composable
fun TripStatusPill(trip: Trip, today: LocalDate, modifier: Modifier = Modifier) {
    val text = when (trip.statusOn(today)) {
        TripStatus.UPCOMING -> {
            val days = trip.daysUntilStart(today).toInt()
            pluralStringResource(R.plurals.core_ui_days_until, days, days)
        }
        TripStatus.ONGOING -> stringResource(R.string.core_ui_trip_ongoing)
        TripStatus.PAST -> stringResource(R.string.core_ui_trip_completed)
    }
    TwInfoPill(text, modifier, tone = if (trip.statusOn(today) == TripStatus.PAST) TwPillTone.OnImage else TwPillTone.Success)
}

/** "¥82,000 of ¥130,000" with a bar that turns red when over budget. */
@Composable
fun BudgetProgress(budget: BudgetSummary, modifier: Modifier = Modifier) {
    val locale = currentLocale
    val spent = Formatters.money(budget.spent, locale)
    val total = Formatters.money(budget.total, locale)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs)) {
        TwProgressBar(budget.spentFraction, stringResource(R.string.core_ui_budget_progress_description, spent, total))
        Text(
            stringResource(R.string.core_ui_budget_progress, spent, total),
            style = TripWiseTheme.typography.labelMedium,
            color = if (budget.spentFraction > 1f) TripWiseTheme.colors.error else TripWiseTheme.colors.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun TripCardPreview() = TripWiseTheme {
    TripCard(PreviewData.trip, PreviewData.budget, PreviewData.today, onClick = {}, modifier = Modifier.padding(16.dp))
}
