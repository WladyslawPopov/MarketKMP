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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.TabWithIcon
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.rows.LazyRowWithScrollBars
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderTabRow(
    tabs: List<TabWithIcon>,
    selectedTab: Int,
    lazyListState: LazyListState,
    menuList: List<MenuItem>,
    isDragMode: Boolean,
    onTabsReordered: (List<TabWithIcon>) -> Unit, // Callback when tabs are reordered
    onClick: (Long) -> Unit = {},
    onLongClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (isDragMode) {
            val newList = tabs.toMutableList().apply {
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
            horizontalArrangement = Arrangement.spacedBy(if (isDragMode) dimens.extraLargePadding else dimens.smallPadding),
        ) {
            itemsIndexed(tabs, key = { _, item -> item.id }) { index, item ->
                ReorderableItem(reorderableState, key = item.id) {
                    val interactionSource = remember { MutableInteractionSource() }
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
                                        onClick(item.id)
                                    }
                                },
                                onLongClick = {
                                    if (!isDragMode) {
                                        onLongClick(item.id)
                                        isOpenMenu.value = true
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
                        val selectedTabLayoutInfo =
                            lazyListState.layoutInfo.visibleItemsInfo.find { it.index == selectedTab }
                        val targetWidthDp =
                            selectedTabLayoutInfo?.size?.let { with(density) { it.toDp() } }
                                ?: 0.dp
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
                        menuList = menuList,
                        onClosed = {
                            isOpenMenu.value = false
                        }
                    )
                }
            }
        }
    }
}
