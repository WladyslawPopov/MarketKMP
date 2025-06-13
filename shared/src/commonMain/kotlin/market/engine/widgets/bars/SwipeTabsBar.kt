package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.items.Tab
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwipeTabsBar(
    isVisibility: Boolean,
    listingData : LD,
    scrollState: LazyListState,
    onRefresh: () -> Unit
) {
    var previousIndex by remember { mutableStateOf(3) }

    val currentPage by remember {
        derivedStateOf {
            (scrollState.firstVisibleItemIndex / PAGE_SIZE) + 1
        }
    }

    val auctionString = stringResource(strings.ordinaryAuction)
    val buyNowString = stringResource(strings.buyNow)
    val allString = stringResource(strings.allOffers)

    val curTab = remember { mutableStateOf(
        when (listingData.filters.find { filter-> filter.key == "sale_type" }?.value){
            "auction" -> auctionString
            "buynow" -> buyNowString
            else -> allString
        }
    ) }

    val isTabsVisible = remember { mutableStateOf(isVisibility) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex < previousIndex) {
                    isTabsVisible.value = true
                } else if (currentIndex > previousIndex) {
                    isTabsVisible.value = false
                }

                if (currentPage == 0) {
                    isTabsVisible.value = true
                }
                if (currentIndex > (previousIndex+3) || currentIndex < (previousIndex-3))
                    previousIndex = currentIndex
            }
    }



    val tabs = listOf(
        Tab(
            title = allString,
            onClick = {
                listingData.filters.find { filter-> filter.key == "sale_type" }?.value = ""
                listingData.filters.find { filter-> filter.key == "sale_type" }?.interpretation = null
                onRefresh()
            }
        ),
        Tab(
            title = auctionString,
            onClick = {
                listingData.filters.find { filter-> filter.key == "sale_type" }?.value = "auction"
                listingData.filters.find { filter-> filter.key == "sale_type" }?.interpretation = ""
                onRefresh()
            }
        ),
        Tab(
            title = buyNowString,
            onClick = {
                listingData.filters.find { filter-> filter.key == "sale_type" }?.value = "buynow"
                listingData.filters.find { filter-> filter.key == "sale_type" }?.interpretation = ""
                onRefresh()
            }
        ),
    )

    AnimatedVisibility(
        visible = isTabsVisible.value,
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            items(tabs) { tab ->
                FilterChip(
                    modifier = Modifier.padding(dimens.smallPadding),
                    selected = tab.title == curTab.value,
                    onClick = {
                        curTab.value = tab.title
                        tab.onClick()
                    },
                    label = {
                        Text(tab.title, style = MaterialTheme.typography.bodySmall)
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
}
