package com.tripwise.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** Editable pill-shaped search field with a clear button. */
@Composable
fun TwSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    clearContentDescription: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester(),
    onSearch: () -> Unit = {},
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(TwIcons.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            { IconButton(onClick = { onQueryChange("") }) { Icon(TwIcons.Clear, clearContentDescription) } }
        } else {
            null
        },
        singleLine = true,
        shape = TripWiseTheme.shapes.pill,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = TripWiseTheme.colors.surfaceContainerLowest,
            unfocusedContainerColor = TripWiseTheme.colors.surfaceContainerLowest,
            unfocusedBorderColor = TripWiseTheme.colors.outlineVariant,
        ),
    )
}

/** Non-editable search "launcher" (Home): looks like the field, acts as a button. */
@Composable
fun TwSearchLauncher(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { role = Role.Button },
        shape = TripWiseTheme.shapes.pill,
        color = TripWiseTheme.colors.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, TripWiseTheme.colors.outlineVariant),
    ) {
        Row(
            Modifier.clickable(onClick = onClick).padding(horizontal = TripWiseTheme.spacing.lg, vertical = TripWiseTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(TwIcons.Search, contentDescription = null, tint = TripWiseTheme.colors.onSurfaceVariant)
            Text(
                placeholder,
                Modifier.padding(start = TripWiseTheme.spacing.md),
                style = TripWiseTheme.typography.bodyLarge,
                color = TripWiseTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TwSearchPreview() = TripWiseTheme {
    Surface {
        androidx.compose.foundation.layout.Column(Modifier.padding(16.dp)) {
            TwSearchLauncher("Search destinations", onClick = {})
            TwSearchField("Kyo", {}, "Search destinations", "Clear", Modifier.padding(top = 16.dp))
        }
    }
}
