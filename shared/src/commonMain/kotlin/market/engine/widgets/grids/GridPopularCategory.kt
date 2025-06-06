package market.engine.widgets.grids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.TopCategory
import market.engine.widgets.items.PopularCategoryItem
import market.engine.widgets.texts.SeparatorLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun GridPopularCategory(categoryList : List<TopCategory>, onCategoryClick: (TopCategory) -> Unit) {

    Spacer(modifier = Modifier.heightIn(dimens.mediumPadding))

    SeparatorLabel(
        stringResource(strings.homeTopCategory))

    LazyVerticalGrid(
        columns = GridCells.Fixed( if (isBigScreen.value) 5 else 3),
        modifier = Modifier
            .heightIn(max = ((if(isBigScreen.value)400 else 250)*categoryList.size).dp)
            .padding(dimens.smallPadding)
            .wrapContentHeight(),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        items(categoryList, key = { it.id }) { category ->
            PopularCategoryItem(category) { onCategoryClick(category) }
        }
    }

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))
}
