package market.engine.fragments.root.main.listing.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.shared.SearchHistory
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.exceptions.dismissBackground
import market.engine.widgets.items.historyItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryLayout(
    historyItems: List<SearchHistory>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    goToListing: (String) -> Unit
) {
    if (historyItems.isEmpty()) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(drawables.historyIcon),
                contentDescription = stringResource(strings.searchHistory),
                tint = colors.black,
                modifier = Modifier.size(dimens.smallIconSize)
            )
            Text(
                text = stringResource(strings.searchHistory),
                color = colors.black,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = dimens.extraSmallPadding)
            )
        }

        ActionButton(
            strings.clear,
            Modifier.wrapContentWidth(),
            MaterialTheme.typography.bodySmall.fontSize,
            alignment = Alignment.BottomEnd
        ){
            onClearHistory()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(50.dp, 250.dp)
            .background(color = colors.primaryColor),
        contentPadding = PaddingValues(dimens.extraSmallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        horizontalAlignment = Alignment.Start
    ) {
        // List Items

        items(historyItems.reversed(), key = { it.id }) { historyItem ->
            val dismissState = rememberDismissState(
                confirmStateChange = { dismissValue ->
                    if (dismissValue == DismissValue.DismissedToStart) {
                        onDeleteItem(historyItem.id)
                        false
                    } else {
                        false
                    }
                }
            )
            AnimatedVisibility(
                dismissState.currentValue != DismissValue.DismissedToStart,
                enter = expandIn(),
            ) {
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = { dismissBackground() },
                    dismissContent = {
                        historyItem(historyItem, onItemClick, goToListing)
                    }
                )
            }
        }
    }
}
