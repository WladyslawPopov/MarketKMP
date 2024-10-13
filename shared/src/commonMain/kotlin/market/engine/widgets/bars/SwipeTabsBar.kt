package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.LD
import market.engine.core.items.Tab
import market.engine.core.types.TabTypeListing
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwipeTabsBar(
    listingData: State<LD>,
    scrollState: LazyGridState
) {
    val itemsPerPage = listingData.value.pageCountItems
    val totalPages = listingData.value.totalPages

    var previousIndex by remember { mutableStateOf(0) }

    val currentPage by remember {
        derivedStateOf {
            (scrollState.firstVisibleItemIndex / itemsPerPage) + 1
        }
    }
    var selectedTab by remember { mutableStateOf(TabTypeListing.ALL) }
    var isTabsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex < previousIndex) {
                    isTabsVisible = true
                } else if (currentIndex > previousIndex) {
                    isTabsVisible = false
                }

                if (currentIndex == 0) {
                    isTabsVisible = true
                }

                if (currentPage == totalPages) {
                    isTabsVisible = true
                }

                previousIndex = currentIndex
            }
    }

    val tabs = listOf(
        Tab(
            type = TabTypeListing.ALL,
            title = stringResource(strings.allOffers),
            onClick = {
                selectedTab = TabTypeListing.ALL
            }
        ),
        Tab(
            type = TabTypeListing.AUCTION,
            title = stringResource(strings.ordinaryAuction),
            onClick = {
                selectedTab = TabTypeListing.AUCTION
            }
        ),
        Tab(
            type = TabTypeListing.BUY_NOW,
            title = stringResource(strings.buyNow),
            onClick = {
                selectedTab = TabTypeListing.BUY_NOW
            }
        ),
    )

    AnimatedVisibility(
        visible = isTabsVisible,
        enter = fadeIn() ,
        exit = fadeOut() ,
        modifier = Modifier.animateContentSize()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            items(tabs) { tab ->
                FilterChip(
                    modifier = Modifier.padding(dimens.smallPadding),
                    selected = tab.type == selectedTab,
                    onClick = {tab.onClick() },
                    label = {
                        Text(tab.title, style = MaterialTheme.typography.bodySmall)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.white,
                        labelColor = colors.black,
                        selectedContainerColor = colors.selected,
                        selectedLabelColor = colors.black
                    ),
                    border = null
                )
            }
        }
    }
}
