package market.engine.widgets.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.ScrollBarsProvider
import market.engine.common.SwipeRefreshContent

@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    isLoading: State<Boolean>,
    showVerticalScrollbar: Boolean = true,
    error: (@Composable () -> Unit)? = null,
    noFound : (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
){
    SwipeRefreshContent(
        isRefreshing = isLoading.value,
        onRefresh = {
            onRefresh()
        }
    ) {

        Box(modifier = Modifier.fillMaxSize())
        {
            if (showVerticalScrollbar){
                val scrollState = rememberScrollState()
                Box(modifier = modifier.fillMaxSize()
                    .verticalScroll(scrollState)
                ) {
                    AnimatedVisibility(
                        modifier = modifier.align(Alignment.Center),
                        visible = !isLoading.value,
                        enter = expandIn(),
                        exit = fadeOut()
                    ) {
                        if(noFound != null){
                            noFound()
                        }else{
                            if (error == null) {
                                content()
                            }else{
                                error()
                            }
                        }
                    }
                }

                ScrollBarsProvider().getVerticalScrollbar(
                    scrollState,
                    modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }else{
                AnimatedVisibility(
                    modifier = modifier.align(Alignment.Center),
                    visible = !isLoading.value,
                    enter = expandIn(),
                    exit = fadeOut()
                ) {
                    if(noFound != null){
                        noFound()
                    }else{
                        if (error == null) {
                            content()
                        }else{
                            error()
                        }
                    }
                }
            }
        }
    }
}


