package market.engine.fragments.base.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import market.engine.fragments.base.CoreViewModel

@Composable
fun rememberLazyScrollState(
    viewModel: CoreViewModel
): ScrollState {

    val scrollStateData by viewModel.scrollState.collectAsState()

    val state = remember(viewModel) {
        ScrollState(
            onScroll = viewModel::updateScroll,
            initialFirstVisibleItemIndex = scrollStateData.scrollItem,
            initialFirstVisibleItemScrollOffset = scrollStateData.offsetScrollItem
        )
    }

    LaunchedEffect(state.scrollState) {
        snapshotFlow {
            state.scrollState.firstVisibleItemIndex to state.scrollState.firstVisibleItemScrollOffset
        }.collect {
            state.handleScroll()
        }
    }

    return state
}
