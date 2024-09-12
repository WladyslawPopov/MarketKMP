package market.engine.common

import androidx.compose.runtime.Composable

@Composable
expect fun SwipeRefreshContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
)
