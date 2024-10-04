package market.engine.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors

@Composable
actual fun SwipeRefreshContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = isRefreshing,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colors.inactiveBottomNavIconColor
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = !isRefreshing,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            content()
        }
    }
}
