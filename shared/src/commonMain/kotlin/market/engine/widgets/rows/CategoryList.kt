package market.engine.widgets.rows

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.TopCategory
import market.engine.widgets.items.CategoryItem

@Composable
fun CategoryList(categories: List<TopCategory>, onCategoryClick: (TopCategory) -> Unit) {

    LazyRowWithScrollBars(
        heightMod = Modifier.fillMaxSize().padding(bottom = dimens.largePadding, top = dimens.mediumPadding)
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
}


