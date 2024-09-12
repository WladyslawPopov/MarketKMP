package market.engine.ui.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.SwipeRefreshContent

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val isLoading = model.isLoading.collectAsState()

    SwipeRefreshContent(
        isRefreshing = isLoading.value,
        onRefresh = {
            component.onRefresh()
        }
    ) {
        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize())
        {
            Box(modifier = modifier.fillMaxSize()
                .verticalScroll(scrollState)) {

                AnimatedVisibility(
                    modifier = modifier.align(Alignment.Center),
                    visible = !isLoading.value,
                    enter = expandIn(),
                    exit = fadeOut()
                ) {
                    Text("Listing")
                }
            }
        }
    }
}
