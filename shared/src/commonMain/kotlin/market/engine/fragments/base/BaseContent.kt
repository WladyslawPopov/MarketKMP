package market.engine.fragments.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.ToastItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    toastItem: ToastItem? = null,
    isHideContent: Boolean = true,
    isLoading : Boolean = false,
    bottomPadding : Dp = dimens.bottomBar,
    onRefresh: () -> Unit,
    topBar: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit) = {},
    content: @Composable BoxScope.(Dp) -> Unit,
) {
    val pullToRefreshState: PullToRefreshState = rememberPullToRefreshState()

    DynamicOverlayLayout(
        modifier = modifier,
        topBar = {
            topBar?.invoke()
        }
    )
    { topPadding, _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                onRefresh = onRefresh,
                isRefreshing = isLoading,
                state = pullToRefreshState,
                indicator = {
                    Indicator(
                        modifier = Modifier.padding(top = topPadding)
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
                    modifier = modifier,
                    visible = if (isHideContent) !isLoading else true,
                    enter = expandIn(),
                    exit = fadeOut()
                )
                {
                    when {
                        noFound != null -> {
                            LazyColumn(
                                Modifier.align(Alignment.Center)
                            ) {
                                item {
                                    noFound()
                                }
                            }
                        }

                        error != null -> {
                            LazyColumn(
                                Modifier.align(Alignment.Center)
                            ) {
                                item {
                                    error()
                                }
                            }
                        }

                        else -> {
                            content(topPadding)
                        }
                    }
                }
            }

            Box(
                Modifier.align(Alignment.BottomEnd)
                    .padding(bottom = bottomPadding, end = dimens.smallPadding)
            ) {
                floatingActionButton()
            }

            if (toastItem != null) {
                AnimatedVisibility(
                    toastItem.isVisible,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = bottomPadding)
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
}
