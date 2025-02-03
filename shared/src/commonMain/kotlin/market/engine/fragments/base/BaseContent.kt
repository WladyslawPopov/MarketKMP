package market.engine.fragments.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.ToastItem
import market.engine.widgets.exceptions.ToastTypeMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    toastItem: MutableState<ToastItem>? = null,
    isHideContent: Boolean = true,
    isLoading : Boolean = false,
    onRefresh: () -> Unit = {},
    topBar: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit) = {},
    content: @Composable BoxScope.() -> Unit,
){
    val pullToRefreshState : PullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = topBar ?: {},
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize().padding(
                    top = if (topBar != null)
                        innerPadding.calculateTopPadding()
                    else 0.dp
                ),
            onRefresh = onRefresh,
            isRefreshing = isLoading,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .size(dimens.mediumIconSize),
                    isRefreshing = isLoading,
                    state = pullToRefreshState,
                    color = colors.notifyTextColor,
                    containerColor = colors.white
                )

            }
        ){
            AnimatedVisibility(
                modifier = modifier,
                visible = if (isHideContent) !isLoading else true,
                enter = expandIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = modifier,
                ) {
                    noFound?.invoke()
                    error?.invoke()
                    content()

                    if(toastItem != null) {
                        AnimatedVisibility(
                            toastItem.value.isVisible,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = dimens.mediumPadding)
                                .zIndex(100f),
                            enter = expandIn(),
                            exit = fadeOut()
                        ) {
                            ToastTypeMessage(
                                message = toastItem.value.message,
                                toastType = toastItem.value.type,
                            )
                        }
                    }
                }
            }
        }
    }
}

