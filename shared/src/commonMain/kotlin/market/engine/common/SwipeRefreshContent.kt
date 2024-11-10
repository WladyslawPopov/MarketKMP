package market.engine.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun SwipeRefreshContent(
    isRefreshing: Boolean,
    modifier: Modifier,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit
)
