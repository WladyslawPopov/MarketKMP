package market.engine.presentation.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.items.ToastItem
import market.engine.widgets.badges.ToastTypeMessage

@Composable
fun BaseContent(
    modifier: Modifier = Modifier,
    toastItem: ToastItem,
    topBar: (@Composable () -> Unit) = {},
    bottomBar: (@Composable () -> Unit) = {},
    error: (@Composable () -> Unit)? = null,
    noFound: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit) = {},
    content: @Composable () -> Unit,
){
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        Box(modifier = modifier.padding(innerPadding).fillMaxSize()) {

            if (noFound != null) {
                noFound()
            }

            if (error != null) {
                error()
            }

            content()

            if (toastItem.isVisible) {
                ToastTypeMessage(
                    toastItem.isVisible,
                    message = toastItem.message,
                    modifier = Modifier.align(Alignment.TopCenter),
                    toastType = toastItem.type
                )
            }
        }
    }
}

