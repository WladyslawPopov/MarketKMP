package market.engine.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun WebView(
    modifier: Modifier,
    url: String,
    title: String,
    onBack: () -> Unit
)
