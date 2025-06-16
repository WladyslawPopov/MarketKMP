package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.states.SwipeTabsBarState


@Composable
fun SwipeTabsBar(
    uiState: SwipeTabsBarState,
    scrollState: LazyListState,
) {
    var previousIndex by remember { mutableStateOf(3) }

    val currentPage by remember {
        derivedStateOf {
            (scrollState.firstVisibleItemIndex / PAGE_SIZE) + 1
        }
    }

    val isTabsVisible = remember(uiState.isTabsVisible) { mutableStateOf(uiState.isTabsVisible) }

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

    AnimatedVisibility(
        visible = isTabsVisible.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            items(uiState.tabs) { tab ->
                FilterChip(
                    modifier = Modifier.padding(dimens.smallPadding),
                    selected = tab.title == uiState.currentTab,
                    onClick = {
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
