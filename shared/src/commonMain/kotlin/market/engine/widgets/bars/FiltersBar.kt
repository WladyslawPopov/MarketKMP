package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.FilterBarUiState
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.rows.LazyRowWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun FiltersBar(
    uiFilterBarUiState: FilterBarUiState,
    isVisible : Boolean = true,
    listingType : Int = 0,
) {
    val filterString = stringResource(strings.filter)
    val sortString = stringResource(strings.sort)
    val chooseAction = stringResource(strings.chooseAction)

    val swipeTabsBarState = uiFilterBarUiState.swipeTabsBarState
    val listNavigation = uiFilterBarUiState.listNavigation.map {
        it.copy(
            icon = when (it.title) {
                filterString -> drawables.filterIcon
                sortString -> drawables.sortIcon
                chooseAction -> if (listingType == 0) drawables.iconWidget else drawables.iconSliderHorizontal
                else -> null
            },
            tint = colors.black,
            onClick = {
                uiFilterBarUiState.onClick(it)
            }
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    )
    {
        Column(
            modifier = Modifier.background(colors.primaryColor),
        ) {
            if (swipeTabsBarState != null) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(swipeTabsBarState.tabs) { tab ->
                        FilterChip(
                            modifier = Modifier.padding(dimens.extraSmallPadding),
                            selected = tab.title == swipeTabsBarState.currentTab,
                            onClick = {
                                swipeTabsBarState.onClick(tab.title)
                            },
                            label = {
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.black
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
                    contentPadding = 0.dp
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
                            BadgedButton(item, colorBackground = colors.grayLayout)
                        }
                    }
                }
            }
        }
    }
}

