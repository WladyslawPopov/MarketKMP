package market.engine.fragments.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import market.engine.core.data.constants.LocalBottomBarHeight
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.ToastItem
import market.engine.widgets.rows.LazyColumnWithScrollBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeToEdgeScaffold(
    modifier: Modifier = Modifier,
    toastItem: ToastItem? = null,
    isLoading : Boolean = false,
    onRefresh: () -> Unit = {},
    showContentWhenLoading: Boolean = false,
    topBar: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit) = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val density = LocalDensity.current

    val pullToRefreshState: PullToRefreshState = rememberPullToRefreshState()

    val bottomBarHeight = LocalBottomBarHeight.dp

    var topBarHeight by remember { mutableStateOf(dimens.zero) }

    var contentPadding by remember { mutableStateOf(PaddingValues()) }

    LaunchedEffect(topBarHeight, bottomBarHeight){
        contentPadding = PaddingValues(
            top = dimens.smallPadding + topBarHeight,
            bottom = if(bottomBarHeight > dimens.largePadding){
                bottomBarHeight + dimens.smallPadding
            } else dimens.largePadding,
            start = dimens.smallPadding,
            end = dimens.smallPadding
        )
    }

    Box(modifier = modifier.background(colors.primaryColor)) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter)
                .zIndex(300f)
                .onSizeChanged {
                    val newHeight = with(density) { it.height.toDp() }
                    topBarHeight = newHeight
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            topBar?.invoke()
        }

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            onRefresh = onRefresh,
            isRefreshing = isLoading,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.padding(top = topBarHeight)
                        .align(Alignment.TopCenter)
                        .size(dimens.mediumIconSize),
                    isRefreshing = isLoading,
                    state = pullToRefreshState,
                    color = colors.inactiveBottomNavIconColor,
                    containerColor = colors.white
                )
            }
        )
        {
            AnimatedVisibility(
                visible = !isLoading || showContentWhenLoading,
                enter = fadeIn(),
                exit = fadeOut()
            )
            {
                when {
                    noFound != null -> {
                        LazyColumnWithScrollBars(
                            contentPadding = contentPadding
                        ) {
                            item {
                                noFound()
                            }
                        }
                    }

                    error != null -> {
                        LazyColumnWithScrollBars(
                                contentPadding = contentPadding
                        ) {
                            item {
                                error()
                            }
                        }
                    }

                    else -> {
                        content(contentPadding)
                    }
                }
            }
        }

        Box(
            Modifier.align(Alignment.BottomEnd)
                .padding(bottom = bottomBarHeight)
                .padding(dimens.mediumPadding)
        ) {
            floatingActionButton()
        }

        if (toastItem != null) {
            AnimatedVisibility(
                toastItem.isVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomBarHeight)
                    .padding(dimens.mediumPadding)
                    .zIndex(100f),
                enter = expandIn(),
                exit = fadeOut()
            ) {
                ToastTypeMessage(
                    message = toastItem.message,
                    toastType = toastItem.type,
                )
            }
        }
    }
}
