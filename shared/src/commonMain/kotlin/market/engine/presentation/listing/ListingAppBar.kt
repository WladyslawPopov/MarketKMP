package market.engine.presentation.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.items.NavigationItem
import market.engine.widgets.texts.TitleText
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.buttons.SmallIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingAppBar(
    title : String,
    modifier: Modifier = Modifier,
    isShowNav: Boolean,
    isOpenCategory: Boolean,
    closeCategory: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
) {
    val isVisible = rememberUpdatedState(isOpenCategory)
    val listItems = listOf(
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
                    closeCategory()
                },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TitleText(title)

                SmallIconButton(
                    if (isVisible.value)
                        drawables.iconArrowUp
                    else
                        drawables.iconArrowDown,
                    colors.black,
                    modifierIconSize = Modifier.size(dimens.mediumIconSize),
                    modifier = Modifier.size(dimens.mediumIconSize),
                ){
                    closeCategory()
                }
            }
        },
        navigationIcon = {
            AnimatedVisibility (isShowNav) {
                NavigationArrowButton {
                    onBackClick()
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
                            modifier = modifier.size(dimens.smallIconSize),
                            onClick = { item.onClick() }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                }
            }
        }
    )
}
