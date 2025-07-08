package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.states.FilterBarUiState
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.rows.LazyRowWithScrollBars

@Composable
fun FiltersBar(
    uiFilterBarUiState: FilterBarUiState
) {
    val swipeTabsBarState = remember(uiFilterBarUiState.swipeTabsBarState) { uiFilterBarUiState.swipeTabsBarState }
    val listNavigation = remember(uiFilterBarUiState.listNavigation) {  uiFilterBarUiState.listNavigation }

    AnimatedVisibility(
        listNavigation.isNotEmpty() && listNavigation.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    )
    {
        Column {
            if (swipeTabsBarState != null) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(swipeTabsBarState.tabs) { tab ->
                        FilterChip(
                            modifier = Modifier.padding(dimens.smallPadding),
                            selected = tab.title == swipeTabsBarState.currentTab,
                            onClick = {
                                tab.onClick()
                            },
                            label = {
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.white,
                                labelColor = colors.black,
                                selectedContainerColor = colors.rippleColor,
                                selectedLabelColor = colors.black
                            ),
                            border = null
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(dimens.smallSpacer),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            )
            {
                LazyRowWithScrollBars(
                    heightMod = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                ) {
                    items(uiFilterBarUiState.listFiltersButtons, key = { it.text }) { item ->
                        ActiveFilterListingItem(
                            item
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        alignment = Alignment.End
                    )
                ) {
                    listNavigation.forEach { item ->
                        if (item.isVisible) {
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(colors.grayLayout, MaterialTheme.shapes.medium)
                                    .padding(dimens.smallSpacer)
                            ) {
                                BadgedButton(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

