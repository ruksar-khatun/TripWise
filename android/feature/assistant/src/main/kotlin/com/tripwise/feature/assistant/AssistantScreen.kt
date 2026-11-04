package com.tripwise.feature.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tripwise.core.designsystem.component.TwInfoPill
import com.tripwise.core.designsystem.component.TwPillTone
import com.tripwise.core.designsystem.component.TwTextField
import com.tripwise.core.designsystem.component.TwTopAppBar
import com.tripwise.core.designsystem.icon.TwIcons
import com.tripwise.core.designsystem.theme.TripWiseTheme

@Composable
internal fun AssistantScreen(onBack: () -> Unit, viewModel: AssistantViewModel = hiltViewModel()) {
    val context by viewModel.contextTitle.collectAsStateWithLifecycle()
    AssistantScreen(context, onBack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AssistantScreen(contextTitle: String?, onBack: () -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    Scaffold(
        topBar = {
            TwTopAppBar(
                title = stringResource(R.string.feature_assistant_title),
                onNavigateBack = onBack,
                navigateBackContentDescription = stringResource(R.string.feature_assistant_back),
            )
        },
        bottomBar = {
            Surface(shadowElevation = 6.dp) {
                Column(Modifier.navigationBarsPadding().imePadding().padding(TripWiseTheme.spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TwTextField(
                            draft, { draft = it }, stringResource(R.string.feature_assistant_input),
                            Modifier.weight(1f), singleLine = false, enabled = false,
                        )
                        FilledIconButton(
                            onClick = {}, enabled = false,
                            modifier = Modifier.padding(start = TripWiseTheme.spacing.sm).size(52.dp),
                        ) { Icon(TwIcons.Send, stringResource(R.string.feature_assistant_send)) }
                    }
                    Text(
                        stringResource(R.string.feature_assistant_later_phase),
                        style = TripWiseTheme.typography.bodySmall,
                        color = TripWiseTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = TripWiseTheme.spacing.sm),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(TripWiseTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.md, Alignment.CenterVertically),
        ) {
            if (contextTitle != null) {
                TwInfoPill(stringResource(R.string.feature_assistant_context, contextTitle), icon = TwIcons.Place, tone = TwPillTone.Accent)
            }
            Icon(TwIcons.Assistant, contentDescription = null, Modifier.size(56.dp), tint = TripWiseTheme.colors.secondary)
            Text(stringResource(R.string.feature_assistant_intro_title), style = TripWiseTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.feature_assistant_intro_body),
                style = TripWiseTheme.typography.bodyMedium,
                color = TripWiseTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(TripWiseTheme.spacing.sm),
                modifier = Modifier.widthIn(max = 480.dp),
            ) {
                listOf(
                    R.string.feature_assistant_suggestion_1, R.string.feature_assistant_suggestion_2,
                    R.string.feature_assistant_suggestion_3, R.string.feature_assistant_suggestion_4,
                ).forEach { res ->
                    SuggestionChip(onClick = {}, enabled = false, label = { Text(stringResource(res)) })
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AssistantPreview() = TripWiseTheme { AssistantScreen("Kyoto in Spring", onBack = {}) }
