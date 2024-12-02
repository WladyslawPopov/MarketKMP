package market.engine.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.SwipeRefreshContent
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.items.ToastItem
import market.engine.widgets.badges.ToastTypeMessage

@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    toastItem: ToastItem? = null,
    isLoading : Boolean,
    onRefresh: () -> Unit,
    topBar: (@Composable () -> Unit) = {},
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit) = {},
    content: @Composable () -> Unit,
){
    Scaffold(
        topBar = topBar,
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        SwipeRefreshContent(
            isRefreshing = isLoading,
            modifier = modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
            onRefresh = onRefresh,
        ) {
            AnimatedVisibility(
                modifier = modifier,
                visible = !isLoading,
                enter = expandIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                ) {
                    when {
                        noFound != null -> {
                            noFound()
                        }
                        error != null -> {
                            error()
                        }
                        else -> {
                            content()
                        }
                    }

                    if (toastItem?.isVisible == true) {
                        ToastTypeMessage(
                            toastItem.isVisible,
                            message = toastItem.message,
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .padding(bottom = dimens.largePadding),
                            toastType = toastItem.type
                        )
                    }
                }
            }
        }
    }
}

