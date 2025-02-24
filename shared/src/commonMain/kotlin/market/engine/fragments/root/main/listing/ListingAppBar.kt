package market.engine.fragments.root.main.listing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingAppBar(
    title : String,
    modifier: Modifier = Modifier,
    isOpenCategory: Boolean,
    isShowSubscribes : Boolean = false,
    closeCategory: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSubscribesClick: () -> Unit = {},
) {
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.subscribersLabel),
            icon = drawables.newLotIcon,
            tint = colors.positiveGreen,
            hasNews = false,
            badgeCount = null,
            isVisible = isShowSubscribes,
            onClick = {
                onSubscribesClick()
            }
        ),
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.searchIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = { onSearchClick() }
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                closeCategory()
            },
        title = {
            Row(
                modifier = modifier.fillMaxWidth().padding(dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TextAppBar(title, modifier = Modifier.fillMaxWidth(0.9f))

                SmallIconButton(
                    if (isOpenCategory)
                        drawables.iconArrowUp
                    else
                        drawables.iconArrowDown,
                    colors.black,
                    modifierIconSize = Modifier.size(dimens.smallIconSize),
                    modifier = Modifier.size(dimens.smallIconSize),
                ){
                    closeCategory()
                }
            }
        },
        navigationIcon = {
            NavigationArrowButton {
                onBackClick()
            }
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, alignment = Alignment.End)
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        BadgedButton(item)
                    }
                }
            }
        }
    )
}
