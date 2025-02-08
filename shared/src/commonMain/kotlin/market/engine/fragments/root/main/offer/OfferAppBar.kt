package market.engine.fragments.root.main.offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.ToastItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.ToastType
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferAppBar(
    offer: Offer,
    baseViewModel: BaseViewModel,
    modifier: Modifier = Modifier,
    onBeakClick: () -> Unit,
) {
    val showMenu = remember { mutableStateOf(false) }
    val onClose = {
        showMenu.value = false
    }

    val isFavorite = remember { mutableStateOf(offer.isWatchedByMe) }

    val listItems = listOf(
        NavigationItem(
            title = strings.favoritesTitle,
            icon = if (isFavorite.value) drawables.favoritesIconSelected else drawables.favoritesIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            onClick = {
                if (UserData.token != "") {
                    baseViewModel.addToFavorites(offer){
                        offer.isWatchedByMe = it
                        isFavorite.value = it
                    }
                }else{
                    goToLogin()
                }
            }
        ),
        NavigationItem(
            title = strings.menuTitle,
            icon = drawables.menuIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                showMenu.value = true
            }
        )
    )

    val menuList = listOf(
        "copyId" to stringResource(strings.copyOfferId),
        "share" to stringResource(strings.shareOffer),
        "calendar" to stringResource(strings.addToCalendar)
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextAppBar(offer.name ?: stringResource(strings.defaultOfferTitle))
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
                    horizontalArrangement = Arrangement.End
                ) {
                    listItems.forEachIndexed { _, item ->
                        if (item.isVisible) {
                            IconButton(
                                modifier = modifier.size(50.dp),
                                onClick = { item.onClick() }
                            ) {
                                getBadgedBox(modifier = modifier, item)
                            }
                        }
                    }
                }

                DropdownMenu(
                    modifier = modifier.widthIn(max = 350.dp).heightIn(max = 400.dp),
                    expanded = showMenu.value,
                    onDismissRequest = { onClose() },
                    containerColor = colors.white,
                    offset = DpOffset(0.dp, 0.dp)
                ) {
                    menuList.forEach { operation ->
                        val idString = stringResource(strings.idCopied)
                        var icon: Painter? = null
                        when(operation.first){
                            "copyId" -> {
                                icon = painterResource(drawables.copyIcon)
                            }
                            "share" -> {
                                icon = painterResource(drawables.shareIcon)
                            }
                            "calendar" -> {
                                icon = painterResource(drawables.calendarIcon)
                            }
                        }

                        DropdownMenuItem(
                            leadingIcon = {
                                icon?.let {
                                    Icon(
                                        it,
                                        contentDescription = stringResource(strings.shareOffer),
                                        modifier = Modifier.size(dimens.smallIconSize),
                                        tint = colors.steelBlue
                                    )
                                }
                            },
                            text = {
                                Text(
                                    text = operation.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.black
                                )
                            },
                            onClick = {
                                when(operation.first){
                                    "copyId" -> {
                                        clipBoardEvent(offer.id.toString())

                                        baseViewModel.showToast(
                                            ToastItem(
                                                isVisible = true,
                                                message = idString,
                                                type = ToastType.SUCCESS
                                            )
                                        )

                                        onClose()
                                    }
                                    "share" -> {
                                        offer.publicUrl?.let { openShare(it) }
                                    }
                                    "calendar" -> {
                                        offer.publicUrl?.let { openCalendarEvent(it) }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}
