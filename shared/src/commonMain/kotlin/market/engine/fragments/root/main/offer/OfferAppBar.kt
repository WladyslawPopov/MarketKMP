package market.engine.fragments.root.main.offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferAppBar(
    isSnapshot: Boolean,
    offer: Offer,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    onBeakClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val openPopup = remember { mutableStateOf(false) }

    val listItems = listOf(
        NavigationItem(
            title = "",
            icon = drawables.recycleIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
            badgeCount = null,
            onClick = onRefresh
        ),
        NavigationItem(
            title = stringResource(strings.myNotesTitle),
            icon = drawables.editNoteIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            isVisible = menuItems.find { it.id == "create_note" || it.id == "edit_note" } != null,
            onClick = {
                menuItems.find { it.id == "create_note" || it.id == "edit_note" }?.onClick?.invoke()
            }
        ),
        NavigationItem(
            title = stringResource(strings.favoritesTitle),
            icon = if (offer.isWatchedByMe) drawables.favoritesIconSelected else drawables.favoritesIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            isVisible = menuItems.find { it.id == "watch" || it.id == "unwatch" } != null,
            onClick = {
                menuItems.find { it.id == "watch" || it.id == "unwatch" }?.onClick?.invoke()
            }
        ),
        NavigationItem(
            title = stringResource(strings.menuTitle),
            icon = drawables.menuIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                openPopup.value = true
            }
        )
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextAppBar(stringResource(if(isSnapshot) strings.snapshotLabel else strings.defaultOfferTitle))
        },
        navigationIcon = {
            NavigationArrowButton {
                onBeakClick()
            }
        },
        actions = {
            Column{
                Row(
                    modifier = modifier.padding(end = dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, alignment = Alignment.End)
                ) {
                    listItems.forEachIndexed { _, item ->
                        if (item.isVisible) {
                            BadgedButton(item)
                        }
                    }
                }

                PopUpMenu(
                    openPopup = openPopup.value,
                    menuList = menuItems,
                    onClosed = {
                        openPopup.value = false
                    }
                )
            }
        }
    )
}
