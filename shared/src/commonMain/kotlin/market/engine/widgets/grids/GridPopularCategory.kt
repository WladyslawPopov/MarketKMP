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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.TopCategory
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.widgets.items.PopularCategoryItem
import market.engine.widgets.common.TitleText
import org.jetbrains.compose.resources.stringResource

@Composable
fun GridPopularCategory(categoryList : List<TopCategory>, onCategoryClick: (TopCategory) -> Unit) {

    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    Spacer(modifier = Modifier.heightIn(dimens.mediumPadding))

    TitleText(stringResource( strings.homeTopCategory))

    LazyVerticalGrid(
        columns = GridCells.Fixed( if (showNavigationRail) 5 else 3),
        modifier = Modifier
            .heightIn(100.dp, (250*categoryList.size).dp)
            .padding(dimens.smallPadding)
            .wrapContentHeight(),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        items(categoryList) { category ->
            PopularCategoryItem(modifier = Modifier.padding(dimens.smallPadding), category) { onCategoryClick(category) }
        }
    }

    Spacer(modifier = Modifier.heightIn(dimens.smallSpacer))
}
