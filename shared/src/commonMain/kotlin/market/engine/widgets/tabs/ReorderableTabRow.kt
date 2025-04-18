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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.Tab
import market.engine.widgets.rows.LazyRowWithScrollBars
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderTabRow(
    tabs: List<Tab>, // Your data class with id and title
    selectedTab: Int, // Index of the selected tab
    onTabSelected: (Int) -> Unit, // Callback when a tab is clicked
    onTabsReordered: (List<Tab>) -> Unit, // Callback when tabs are reordered
    isDragMode: MutableState<Boolean>,
    lazyListState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    val currentTabsState = rememberUpdatedState(tabs)
    val listState = rememberUpdatedState(tabs)


    LaunchedEffect(selectedTab) {
        val visible = lazyListState.layoutInfo.visibleItemsInfo.any { it.index == selectedTab }
        if (!visible) {
            delay(50)
            lazyListState.scrollToItem(selectedTab)
        }
    }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (isDragMode.value) {
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
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        ) {
            itemsIndexed(currentTabsState.value, key = { _, item -> item.title }) { index, item ->
                ReorderableItem(reorderableState, key = item.title) {
                    val interactionSource = remember { MutableInteractionSource() }
                    PageTab(
                        tab = item,
                        selectedTab = selectedTab,
                        currentIndex = index,
                        isDragMode = isDragMode.value,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    onTabSelected(index)
                                },
                                onLongClick = {
                                    isDragMode.value = true
                                }
                            )
                            .draggableHandle(
                            enabled = isDragMode.value,
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
                    )
                    if (selectedTab == index) {
                        val density = LocalDensity.current

                        val selectedTabLayoutInfo =
                            remember(selectedTab, lazyListState.layoutInfo.visibleItemsInfo) {
                                lazyListState.layoutInfo.visibleItemsInfo.find { it.index == selectedTab }
                            }

                        val targetWidthDp = remember(selectedTabLayoutInfo) {
                            selectedTabLayoutInfo?.size?.let { with(density) { it.toDp() } } ?: 0.dp
                        }

                        val animatedIndicatorWidth by animateDpAsState(
                            targetValue = targetWidthDp,
                            animationSpec = tween(durationMillis = 300),
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
                }
            }
        }
    }
}

