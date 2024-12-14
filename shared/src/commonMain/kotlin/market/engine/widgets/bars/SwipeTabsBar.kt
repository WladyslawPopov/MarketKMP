package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.baseFilters.LD
import market.engine.core.items.Tab
import market.engine.core.types.TabTypeListing
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwipeTabsBar(
    isVisibility: Boolean,
    listingData : LD,
    scrollState: LazyListState,
    onRefresh: () -> Unit
) {
    val itemsPerPage = listingData.pageCountItems
    val totalPages = listingData.totalPages

    var previousIndex by remember { mutableStateOf(0) }

    val currentPage by remember {
        derivedStateOf {
            (scrollState.firstVisibleItemIndex / itemsPerPage) + 1
        }
    }

    val curTab = when (listingData.filters.find { filter-> filter.key == "sale_type" }?.value){
        "auction" -> TabTypeListing.AUCTION
        "buynow" -> TabTypeListing.BUY_NOW
        else -> TabTypeListing.ALL
    }
    val isTabsVisible = remember { mutableStateOf(true) }

    if (isVisibility) {
        isTabsVisible.value = true
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex < previousIndex) {
                    isTabsVisible.value = true
                } else if (currentIndex > previousIndex) {
                    isTabsVisible.value = false
                }

                if (currentIndex == 0) {
                    isTabsVisible.value = true
                }

                if (currentPage == totalPages) {
                    isTabsVisible.value = true
                }

                previousIndex = currentIndex
            }
    }

    val auctionString = stringResource(strings.ordinaryAuction)
    val buyNowString = stringResource(strings.buyNow)
    val allString = stringResource(strings.allOffers)

    val tabs = listOf(
        Tab(
            type = TabTypeListing.ALL,
            title = allString,
            onClick = {
                listingData.filters.find { filter-> filter.key == "sale_type" }?.value = ""
                listingData.filters.find { filter-> filter.key == "sale_type" }?.interpritation = null

                listingData.filters.find { filter-> filter.key == "starting_price" }?.value = ""
                listingData.filters.find { filter-> filter.key == "starting_price" }?.interpritation = null
                listingData.filters.find { filter-> filter.key == "discount_price" }?.value = ""
                listingData.filters.find { filter-> filter.key == "discount_price" }?.interpritation = null

                onRefresh()
            }
        ),
        Tab(
            type = TabTypeListing.AUCTION,
            title = auctionString,
            onClick = {
                listingData.filters.find { filter-> filter.key == "sale_type" }?.value = "auction"
                listingData.filters.find { filter-> filter.key == "sale_type" }?.interpritation = ""

                listingData.filters.find { filter-> filter.key == "starting_price" }?.value = ""
                listingData.filters.find { filter-> filter.key == "starting_price" }?.interpritation = null
                listingData.filters.find { filter-> filter.key == "discount_price" }?.value = ""
                listingData.filters.find { filter-> filter.key == "discount_price" }?.interpritation = null

                onRefresh()
            }
        ),
        Tab(
            type = TabTypeListing.BUY_NOW,
            title = buyNowString,
            onClick = {
                listingData.filters.find { filter-> filter.key == "sale_type" }?.value = "buynow"
                listingData.filters.find { filter-> filter.key == "sale_type" }?.interpritation = ""

                listingData.filters.find { filter-> filter.key == "starting_price" }?.value = ""
                listingData.filters.find { filter-> filter.key == "starting_price" }?.interpritation = null
                listingData.filters.find { filter-> filter.key == "discount_price" }?.value = ""
                listingData.filters.find { filter-> filter.key == "discount_price" }?.interpritation = null

                onRefresh()
            }
        ),
    )

    AnimatedVisibility(
        visible = isTabsVisible.value,
        enter = fadeIn(),
        exit = fadeOut(),
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
                    selected = tab.type == curTab,
                    onClick = { tab.onClick() },
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
