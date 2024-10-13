package market.engine.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.shared.SearchHistory
import market.engine.widgets.buttons.ActionTextButton
import market.engine.widgets.exceptions.dismissBackground
import market.engine.widgets.items.historyItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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
            .fillMaxWidth()
            .padding(vertical = dimens.smallPadding),
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

        ActionTextButton(
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
            .wrapContentHeight()
            .background(color = colors.primaryColor),
        contentPadding = PaddingValues(dimens.extraSmallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        horizontalAlignment = Alignment.Start
    ) {
        // List Items

        items(historyItems.reversed(), key = { it.id }) { historyItem ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteItem(historyItem.id)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    dismissBackground(dismissState)
                },
            ){
                historyItem(historyItem, onItemClick, goToListing)
            }
        }
    }
}
