package market.engine.widgets.rows

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import market.engine.common.ScrollBarsProvider
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun LazyColumnWithScrollBars(
    heightMod : Modifier = Modifier.fillMaxSize(),
    modifierList: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding : Dp = dimens.zero,
    verticalArrangement : Arrangement.Vertical = Arrangement.spacedBy(dimens.mediumPadding),
    horizontalAlignment : Alignment.Horizontal = Alignment.Start,
    reverseLayout : Boolean = false,
    flingBehavior : FlingBehavior = ScrollableDefaults.flingBehavior(),
    content: LazyListScope.() -> Unit
) {
    Box(
       modifier = heightMod
    ) {
        LazyColumn(
            state = state,
            modifier = modifierList,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(contentPadding),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            reverseLayout = reverseLayout,
            content = content
        )

        ScrollBarsProvider().getVerticalScrollbar(
            state,
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
        )
    }
}
