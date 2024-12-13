package market.engine.presentation.listing.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.baseFilters.SD
import market.engine.core.items.NavigationItem
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TitleText
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.SmallIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAppBar(
    isShowNav : Boolean,
    modifier: Modifier = Modifier,
    title: String,
    searchData: SD,
    onSearchClick: () -> Unit,
    onClearSearchClick: () -> Unit,
    onBeakClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val listItems = listOf(
        NavigationItem(
            title = strings.resetLabel,
            icon = drawables.cancelIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null,
            isVisible = searchData.searchCategoryID != 1L,
            onClick = { onClearSearchClick() }
        ),
        NavigationItem(
            title = strings.searchTitle,
            icon = drawables.searchIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null,
            onClick = { onSearchClick() }
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            Row(
                modifier = modifier.fillMaxWidth().clickable {
                    onCloseClick()
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TitleText(title)

                SmallIconButton(drawables.iconArrowDown, colors.black){
                    onCloseClick()
                }
            }
        },
        navigationIcon = {
            if(isShowNav) {
                NavigationArrowButton {
                    onBeakClick()
                }
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        IconButton(
                            modifier = modifier.size(dimens.mediumIconSize),
                            onClick = {
                                item.onClick()
                            }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                    Spacer(modifier = Modifier.padding(dimens.smallPadding))
                }
            }
        },
    )
}
