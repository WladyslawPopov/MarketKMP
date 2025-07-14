package market.engine.fragments.base.listing

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import market.engine.core.data.states.ScrollDataState

@Stable
class ScrollState(
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

    private var previousIndex = 1

    internal fun handleScroll() {
        val index = scrollState.firstVisibleItemIndex
        
        if (index < previousIndex) {
            _areBarsVisible.value = true
        } else if (index > previousIndex) {
            _areBarsVisible.value = false
        }
        
        if (index != previousIndex) {
            previousIndex = index
        }

        onScroll(ScrollDataState(index, scrollState.firstVisibleItemScrollOffset))
    }
}
