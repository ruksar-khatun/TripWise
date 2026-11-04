package com.tripwise.feature.trips.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.tripwise.core.designsystem.component.TwFilterChip
import com.tripwise.core.designsystem.theme.TripWiseTheme
import com.tripwise.core.model.ActivityCategory
import com.tripwise.core.ui.format.Formatters
import com.tripwise.core.ui.format.currentLocale
import com.tripwise.core.ui.label.ui
import com.tripwise.feature.trips.R

/** Add or edit one itinerary activity (manual itinerary management). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ItemEditorDialog(editor: ItemEditor, onAction: (TripDetailAction) -> Unit) {
    val titleError = editor.showErrors && editor.title.isBlank()
    val timeError = editor.showErrors && editor.time.isNotBlank() && editor.parsedTime == null
    AlertDialog(
        onDismissRequest = { onAction(TripDetailAction.DismissEditor) },
        title = {
            Text(stringResource(if (editor.id == null) R.string.feature_trips_add_activity else R.string.feature_trips_edit_activity))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md)) {
                Text(Formatters.dayLabel(editor.date, currentLocale), style = TripWiseTheme.typography.labelLarge, color = TripWiseTheme.colors.secondary)
                OutlinedTextField(
                    value = editor.title, onValueChange = { onAction(TripDetailAction.UpdateEditor(editor.copy(title = it.take(100)))) },
                    label = { Text(stringResource(R.string.feature_trips_activity_title)) }, singleLine = true, isError = titleError,
                    supportingText = if (titleError) { { Text(stringResource(R.string.feature_trips_error_required)) } } else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                OutlinedTextField(
                    value = editor.time, onValueChange = { onAction(TripDetailAction.UpdateEditor(editor.copy(time = it.take(5)))) },
                    label = { Text(stringResource(R.string.feature_trips_activity_time)) }, singleLine = true, isError = timeError,
                    placeholder = { Text(stringResource(R.string.feature_trips_time_hint)) },
                    supportingText = if (timeError) { { Text(stringResource(R.string.feature_trips_error_time)) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.xs)) {
                    ActivityCategory.entries.forEach { c ->
                        TwFilterChip(stringResource(c.ui.label), editor.category == c, { onAction(TripDetailAction.UpdateEditor(editor.copy(category = c))) })
                    }
                }
                OutlinedTextField(
                    value = editor.notes, onValueChange = { onAction(TripDetailAction.UpdateEditor(editor.copy(notes = it.take(300)))) },
                    label = { Text(stringResource(R.string.feature_trips_note)) }, minLines = 2,
                )
            }
        },
        confirmButton = { TextButton(onClick = { onAction(TripDetailAction.SaveItem) }) { Text(stringResource(R.string.feature_trips_save_short)) } },
        dismissButton = {
            Row {
                if (editor.id != null) {
                    TextButton(onClick = { onAction(TripDetailAction.DeleteItem) }) { Text(stringResource(R.string.feature_trips_delete_short), color = TripWiseTheme.colors.error) }
                }
                TextButton(onClick = { onAction(TripDetailAction.DismissEditor) }) { Text(stringResource(R.string.feature_trips_cancel)) }
            }
        },
    )
}
