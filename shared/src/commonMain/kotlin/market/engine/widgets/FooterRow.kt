package market.engine.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.items.TopCategory

@Composable
fun FooterRow(items : List<TopCategory>) {
    LazyRow(
        modifier = Modifier.wrapContentSize()
    ){
        items(items) { item ->
            run {
                PopularCategoryItem(modifier = Modifier.padding(dimens.smallPadding), category = item) {}
            }
        }
    }
}
