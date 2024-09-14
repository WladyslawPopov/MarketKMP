package market.engine.widgets.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import application.market.auction_mobile.business.networkObjects.Category
import market.engine.business.constants.ThemeResources.dimens
import market.engine.common.ScrollBarsProvider
import market.engine.widgets.items.CategoryItem

@Composable
fun CategoryList(categories: List<Category>, onCategoryClick: (Category) -> Unit) {

    val lazyListState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().padding(bottom = dimens.largePadding, top = dimens.mediumPadding) )
    {
        LazyRow(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth().padding(bottom = dimens.mediumPadding).wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
        ) {
            items(categories) { category ->
                if (category.id == categories.first().id) {
                    Spacer(modifier = Modifier.padding(start = dimens.smallPadding))
                }

                CategoryItem(category = category, onClick = onCategoryClick)

                if (category.id == categories.last().id) {
                    Spacer(modifier = Modifier.padding(end = dimens.smallPadding))
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


