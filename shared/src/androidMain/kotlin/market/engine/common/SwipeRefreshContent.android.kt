package market.engine.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import market.engine.business.constants.ThemeResources.colors

@Composable
actual fun SwipeRefreshContent(
    isRefreshing: Boolean,
    onRefresh:() -> Unit,
    content: @Composable () -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { onRefresh() },
        content = content,
        indicator = { state, trigger ->
            if (state.isRefreshing) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ){
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.inactiveBottomNavIconColor
                    )
                }
            }else{
                SwipeRefreshIndicator(
                    state,
                    trigger,
                    contentColor = colors.inactiveBottomNavIconColor,
                    backgroundColor = colors.white
                )
            }
        }
    )
}

