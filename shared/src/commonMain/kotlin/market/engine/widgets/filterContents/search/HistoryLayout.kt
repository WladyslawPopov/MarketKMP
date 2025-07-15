package market.engine.widgets.filterContents.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.buildAnnotatedString
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.SearchHistoryItem
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.dialogs.AccessDialog
import market.engine.widgets.ilustrations.dismissBackground
import market.engine.widgets.items.historyItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryLayout(
    historyItems: List<SearchHistoryItem>,
    modifier: Modifier = Modifier,
    onItemClick: (SearchHistoryItem) -> Unit,
    onClearHistory: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    goToListing: (SearchHistoryItem) -> Unit
) {
    val showClearHistory = remember { mutableStateOf(false) }
    val showClearHistoryItem = remember { mutableStateOf(1L) }

    if (historyItems.isEmpty()) {
        return
    }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                stringResource(strings.clear),
                Modifier.wrapContentWidth(),
                MaterialTheme.typography.bodySmall.fontSize,
                alignment = Alignment.BottomEnd
            ) {
                showClearHistory.value = true
            }
        }

        LazyColumnWithScrollBars(
            heightMod = Modifier.fillMaxWidth()
                .background(color = colors.primaryColor),
            modifierList = Modifier.fillMaxWidth(),
//            contentPadding = dimens.mediumPadding,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            // List Items
            items(historyItems.reversed(), key = { it.id }) { historyItem ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            showClearHistoryItem.value = historyItem.id
                            false
                        } else {
                            false
                        }
                    }
                )
                AnimatedVisibility(
                    dismissState.currentValue != SwipeToDismissBoxValue.EndToStart,
                    enter = expandIn(),
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = { dismissBackground() },
                        modifier = Modifier.fillMaxWidth(),
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        gesturesEnabled = true,
                    ){
                        historyItem(historyItem, onItemClick, goToListing)
                    }
                }
            }
        }

        AccessDialog(
            showClearHistory.value,
            title = buildAnnotatedString {
                append(stringResource(strings.warningDeleteHistory))
            },
            onSuccess = {
                onClearHistory()
            },
            onDismiss = {
                showClearHistory.value = false
            }
        )

        AccessDialog(
            showClearHistoryItem.value != 1L,
            title = buildAnnotatedString {
                append(stringResource(strings.warningDeleteHistory))
            },
            onSuccess = {
                onDeleteItem(showClearHistoryItem.value)
            },
            onDismiss = {
                showClearHistoryItem.value = 1L
            }
        )
    }
}
