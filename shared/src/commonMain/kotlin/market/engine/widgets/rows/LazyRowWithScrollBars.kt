package market.engine.widgets.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import market.engine.common.ScrollBarsProvider
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun LazyRowWithScrollBars(
    heightMod : Modifier = Modifier.fillMaxSize(),
    modifierList: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding : Dp = dimens.smallPadding,
    horizontalArrangement : Arrangement.Horizontal = Arrangement.spacedBy(dimens.mediumPadding),
    verticalAlignment : Alignment.Vertical = Alignment.CenterVertically,
    content: LazyListScope.() -> Unit
) {
    Box(
        modifier = heightMod
    ) {
        LazyRow(
            state = state,
            modifier = modifierList,
            contentPadding = PaddingValues(contentPadding),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )

        ScrollBarsProvider().getHorizontalScrollbar(
            state,
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}
