package market.engine.presentation.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import market.engine.common.SwipeRefreshContent
import market.engine.core.items.ToastItem
import market.engine.widgets.exceptions.ToastTypeMessage

@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    toastItem: MutableState<ToastItem>? = null,
    isLoading : Boolean,
    onRefresh: () -> Unit,
    topBar: (@Composable () -> Unit)? = null,
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit) = {},
    content: @Composable BoxScope.() -> Unit,
){
    Scaffold(
        modifier,
        topBar = topBar ?: {},
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        SwipeRefreshContent(
            isRefreshing = isLoading,
            modifier = Modifier.fillMaxSize()
                .padding(top = if (topBar != null)
                                    innerPadding.calculateTopPadding()
                                else 0.dp
                        ),
            onRefresh = onRefresh,
        ) {
            AnimatedVisibility(
                visible = !isLoading,
                enter = expandIn(),
                exit = fadeOut()
            ) {
                Box {
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
                    if(toastItem != null) {
                        AnimatedVisibility(
                            toastItem.value.isVisible,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(100f)
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

