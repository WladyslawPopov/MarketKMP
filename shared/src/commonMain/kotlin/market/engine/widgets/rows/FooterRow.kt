package market.engine.widgets.rows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.TopCategory
import market.engine.common.ScrollBarsProvider
import market.engine.widgets.items.FooterItem

@Composable
fun FooterRow(items : List<TopCategory>) {
    val lazyListState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize())
    {
        LazyRow(
            state = lazyListState,
            modifier = Modifier.background(color = colors.white)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            items(items) { item ->
                run {
                    Spacer(modifier = Modifier.width(dimens.mediumSpacer))
                    FooterItem(modifier = Modifier.padding(dimens.smallPadding), category = item) {}
                }
            }
        }

        ScrollBarsProvider().getHorizontalScrollbar(
            lazyListState,
            Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
        )
    }
}
