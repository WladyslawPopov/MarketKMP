package market.engine.fragments.base.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import market.engine.core.data.states.ScrollDataState

@Composable
fun rememberListingState(
    onScroll: (ScrollDataState) -> Unit,
    scrollStateData: ScrollDataState = ScrollDataState(0, 0)
): ListingState {

    val state = remember(onScroll) {
        ListingState(
            onScroll = onScroll,
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
