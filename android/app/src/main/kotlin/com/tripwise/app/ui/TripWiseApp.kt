package com.tripwise.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.tripwise.app.R
import com.tripwise.app.navigation.TripWiseNavHost
import com.tripwise.core.designsystem.theme.TripWiseTheme

/** App shell. Bottom navigation / top-level destinations are added with the first features. */
@Composable
fun TripWiseApp() {
    Scaffold { innerPadding ->
        TripWiseNavHost(contentPadding = innerPadding)
    }
}

/** Temporary start destination proving theme, navigation and DI are wired. Removed once Home exists. */
@Composable
internal fun FoundationScreen(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(TripWiseTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.foundation_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.foundation_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoundationScreenPreview() {
    TripWiseTheme { FoundationScreen(contentPadding = PaddingValues()) }
}
