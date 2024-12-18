package market.engine.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.TextFieldScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors

actual class ScrollBarsProvider {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    actual fun getVerticalScrollbar(
        scrollState: Any,
        modifier: Modifier
    ) {
        val state = when (scrollState) {
            is ScrollState -> rememberScrollbarAdapter(scrollState)
            is LazyListState -> rememberScrollbarAdapter(scrollState)
            is LazyGridState -> rememberScrollbarAdapter(scrollState)
            is TextFieldScrollState -> rememberScrollbarAdapter(scrollState)
            else -> rememberScrollbarAdapter(rememberScrollState())
        }

        VerticalScrollbar(
            adapter = state,
            modifier = modifier,
            style = ScrollbarStyle(
                unhoverColor = colors.steelBlue,
                hoverColor = colors.rippleColor,
                hoverDurationMillis = 30,
                minimalHeight = 50.dp,
                shape = MaterialTheme.shapes.medium,
                thickness = 8.dp
            )
        )

    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    actual fun getHorizontalScrollbar(
        scrollState: Any,
        modifier: Modifier
    ) {
        val state = when (scrollState) {
            is ScrollState -> rememberScrollbarAdapter(scrollState)
            is LazyListState -> rememberScrollbarAdapter(scrollState)
            is LazyGridState -> rememberScrollbarAdapter(scrollState)
            is TextFieldScrollState -> rememberScrollbarAdapter(scrollState)
            else -> rememberScrollbarAdapter(rememberScrollState())
        }

        HorizontalScrollbar(
            adapter = state,
            modifier = modifier,
            style = ScrollbarStyle(
                unhoverColor = colors.steelBlue,
                hoverColor = colors.rippleColor,
                hoverDurationMillis = 30,
                minimalHeight = 50.dp,
                shape = MaterialTheme.shapes.medium,
                thickness = 8.dp
            )
        )
    }
}
