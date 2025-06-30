package market.engine.widgets.tabs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.Tab
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.rows.LazyRowWithScrollBars
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderTabRow(
    tabs: List<Tab>,
    selectedTab: Int,
    lazyListState: LazyListState,
    onTabSelected: (Int) -> Unit, // Callback when a tab is clicked
    onTabsReordered: (List<Tab>) -> Unit, // Callback when tabs are reordered
    getOperations: (Long, (List<MenuItem>) -> Unit) -> Unit,
    isDragMode: Boolean,
    modifier: Modifier = Modifier
) {
    val currentTabsState = rememberUpdatedState(tabs)
    val listState = rememberUpdatedState(tabs)
    val previousSelectedTab = remember { mutableStateOf(selectedTab) } // Track previous tab

    // Programmatic scroll to position selectedTab based on direction
    LaunchedEffect(selectedTab) {
        delay(30) // Smooth UX delay
        if (tabs.isEmpty()) return@LaunchedEffect // Handle empty tabs

        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val firstVisibleIndex = ((visibleItems.firstOrNull()?.index ?: 0) + 1).coerceAtMost(tabs.size)
        val lastVisibleIndex = ((visibleItems.lastOrNull()?.index ?: 0) - 1).coerceAtLeast(0)

        // Determine scroll direction
        val isScrollingForward = selectedTab > previousSelectedTab.value

        if (isScrollingForward) {
            // Forward: Make selectedTab the first visible item
            // Only scroll if selectedTab is not close to firstVisibleIndex (avoid flickering)
            if (selectedTab != firstVisibleIndex && selectedTab !in firstVisibleIndex..lastVisibleIndex) {
                lazyListState.animateScrollToItem(selectedTab, scrollOffset = 1)
            }
        } else {
            // Backward: Make selectedTab the last visible item
            // Only scroll if selectedTab is not close to lastVisibleIndex
            if (selectedTab !in lastVisibleIndex..firstVisibleIndex && selectedTab != lastVisibleIndex) {
                lazyListState.animateScrollToItem((selectedTab - 1).coerceAtLeast(0))
            }
        }

        // Update previous tab
        previousSelectedTab.value = selectedTab
    }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (isDragMode) {
            val newList = listState.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            onTabsReordered(newList)
        }
    }

    Box(modifier = modifier) {
        LazyRowWithScrollBars(
            state = lazyListState,
            heightMod = Modifier,
            modifierList = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if(isDragMode) dimens.extraLargePadding else dimens.smallPadding),
        ) {
            itemsIndexed(currentTabsState.value, key = { _, item -> item.id }) { index, item ->
                ReorderableItem(reorderableState, key = item.id) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val menuList = remember {
                        mutableStateOf(emptyList<MenuItem>())
                    }

                    val isOpenMenu = remember { mutableStateOf(false) }

                    PageTab(
                        tab = item,
                        selectedTab = selectedTab,
                        currentIndex = index,
                        isDragMode = isDragMode,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    if (!isDragMode) {
                                        onTabSelected(index)
                                    }
                                },
                                onLongClick = {
                                    if (!isDragMode) {
                                        getOperations(item.id) { menuItems ->
                                            menuList.value = menuItems
                                            isOpenMenu.value = true
                                        }
                                    }
                                }
                            )
                            .draggableHandle(
                                enabled = isDragMode,
                                onDragStarted = {
    //                                    ViewCompat.performHapticFeedback(
    //                                        view,
    //                                        HapticFeedbackConstantsCompat.GESTURE_START
    //                                    )
                                },
                                onDragStopped = {
    //                                    ViewCompat.performHapticFeedback(
    //                                        view,
    //                                        HapticFeedbackConstantsCompat.GESTURE_END
    //                                    )
                                },
                                interactionSource = interactionSource,
                            )
                            .background(
                                if (isDragMode) colors.steelBlue.copy(alpha = 0.2f) else colors.transparent,
                                MaterialTheme.shapes.small
                            )
                    )

                    if (selectedTab == index && !isDragMode) {
                        val density = LocalDensity.current
                        val selectedTabLayoutInfo = lazyListState.layoutInfo.visibleItemsInfo.find { it.index == selectedTab }
                        val targetWidthDp = selectedTabLayoutInfo?.size?.let { with(density) { it.toDp() } } ?: 0.dp
                        val animatedIndicatorWidth by animateDpAsState(
                            targetValue = targetWidthDp,
                            animationSpec = tween(durationMillis = 200),
                            label = "IndicatorWidth"
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width(animatedIndicatorWidth)
                                .height(dimens.extraSmallPadding)
                                .background(
                                    color = colors.steelBlue,
                                    shape = RoundedCornerShape(
                                        topStart = dimens.extraSmallPadding,
                                        topEnd = dimens.extraSmallPadding
                                    )
                                )
                                .padding(horizontal = dimens.smallPadding)
                        )
                    }

                    PopUpMenu(
                        openPopup = isOpenMenu.value,
                        menuList = menuList.value,
                        onClosed = {
                            isOpenMenu.value = false
                        }
                    )
                }
            }
        }
    }
}
