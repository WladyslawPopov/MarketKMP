package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.Tab
import market.engine.core.types.TabTypeListing
import market.engine.presentation.listing.SCROLL_DELTA_THRESHOLD
import org.jetbrains.compose.resources.stringResource

@Composable
fun SwipeTabsBar(
    scrollState: LazyGridState
) {
    var selectedTab by remember { mutableStateOf(TabTypeListing.ALL) }
    var previousScrollIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }
    var isTabsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.firstVisibleItemScrollOffset, scrollState.firstVisibleItemIndex) {
        val currentOffset = scrollState.firstVisibleItemScrollOffset
        val currentIndex = scrollState.firstVisibleItemIndex

        val totalOffsetChange = (currentIndex - previousScrollIndex) * 1000 + (currentOffset - previousScrollOffset)

        if (totalOffsetChange > SCROLL_DELTA_THRESHOLD) {
            isTabsVisible = false
        } else if (totalOffsetChange < -SCROLL_DELTA_THRESHOLD) {
            isTabsVisible = true
        }
        previousScrollIndex = currentIndex
        previousScrollOffset = currentOffset
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
