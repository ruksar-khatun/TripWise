package com.tripwise.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Single-choice segmented control (e.g. Upcoming | Past). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwSegmentedButtons(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = TripWiseTheme.colors.secondaryContainer,
                    activeContentColor = TripWiseTheme.colors.onSecondaryContainer,
                ),
            ) { Text(label) }
        }
    }
}

@PreviewLightDark
@Composable
private fun TwSegmentedButtonsPreview() = TripWiseTheme {
    TwSegmentedButtons(listOf("Upcoming", "Past"), 0, {})
}
