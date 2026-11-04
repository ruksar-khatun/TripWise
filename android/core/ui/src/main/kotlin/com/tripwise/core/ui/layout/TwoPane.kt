package com.tripwise.core.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Width from which list + detail are shown side by side (Material "expanded" window class). */
val TwoPaneMinWidth: Dp = 840.dp

/** Detail content shown next to a list on wide screens; supplied by :app so features stay independent. */
typealias DetailPane = @Composable (id: String, onClose: () -> Unit) -> Unit

/**
 * List/detail layout. Narrow: only [list]; item clicks call [onOpenDetail] (navigate).
 * Wide (≥ [TwoPaneMinWidth]) with a [detailPane]: list and detail side by side; clicks select in place.
 */
@Composable
fun ListDetailLayout(
    detailPane: DetailPane?,
    onOpenDetail: (String) -> Unit,
    emptyDetail: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    list: @Composable (selectedId: String?, onItemClick: (String) -> Unit, modifier: Modifier) -> Unit,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (detailPane != null && maxWidth >= TwoPaneMinWidth) {
            Row(Modifier.fillMaxSize()) {
                list(selectedId, { selectedId = it }, Modifier.weight(1f))
                VerticalDivider()
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    val id = selectedId
                    if (id != null) detailPane(id) { selectedId = null } else emptyDetail()
                }
            }
        } else {
            list(null, onOpenDetail, Modifier)
        }
    }
}
