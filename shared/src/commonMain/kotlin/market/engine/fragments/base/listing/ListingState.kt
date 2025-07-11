package market.engine.fragments.base.listing

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.states.ScrollDataState

@Stable
class ListingState(
    internal val onScroll: (ScrollDataState) -> Unit,
    initialFirstVisibleItemIndex: Int,
    initialFirstVisibleItemScrollOffset: Int
) {

    val scrollState: LazyListState = LazyListState(
        initialFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset
    )

    val areBarsVisible: State<Boolean>
        get() = _areBarsVisible
    private val _areBarsVisible = mutableStateOf(true)
    private var previousIndex = 3
    private val currentPage by derivedStateOf {
        (scrollState.firstVisibleItemIndex / PAGE_SIZE) + 1
    }

    internal fun handleScroll() {
        val index = scrollState.firstVisibleItemIndex
        
        if (index < previousIndex) {
            _areBarsVisible.value = true
        } else if (index > previousIndex) {
            _areBarsVisible.value = false
        }
        
        if (currentPage == 0) {
            _areBarsVisible.value = true
        }
        
        if (index != previousIndex) {
            previousIndex = index
        }

        onScroll(ScrollDataState(index, scrollState.firstVisibleItemScrollOffset))
    }
}
