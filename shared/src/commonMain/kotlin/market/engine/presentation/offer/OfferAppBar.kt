package market.engine.presentation.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import market.engine.common.clipBoardEvent
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.core.items.ToastItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.types.ToastType
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.main.UIMainEvent
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferAppBar(
    offer: Offer,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavClick: () -> Unit = {},
    onCartClick: () -> Unit,
    onBeakClick: () -> Unit,
) {
    val mainViewModel : MainViewModel = koinViewModel()

    val showMenu = remember { mutableStateOf(false) }
    val onClose = {
        showMenu.value = false
    }

    val listItems = listOf(
        NavigationItem(
            title = "",
            icon = if (isFavorite) drawables.favoritesIconSelected else drawables.favoritesIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            onClick = { onFavClick() }
        ),
        NavigationItem(
            title = "",
            icon = drawables.basketIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = UserData.userInfo?.countOffersInCart,
            onClick = { onCartClick() }
        ),
        NavigationItem(
            title = "",
            icon = drawables.menuIcon,
            tint = colors.steelBlue,
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
            TitleText(offer.name ?: stringResource(strings.defaultOfferTitle))
        },
        navigationIcon = {
            IconButton(
                modifier = modifier,
                onClick = {
                    onBeakClick()
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(strings.menuTitle),
                    modifier = modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
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
            }
        }
    )

    AnimatedVisibility(showMenu.value, modifier = modifier.fillMaxWidth()) {
        Popup(
            alignment = Alignment.TopEnd,
            offset = IntOffset(0, 200),
            onDismissRequest = {
                onClose()
            }
        ) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .heightIn(max = 400.dp)
                    .wrapContentSize()
                    .shadow(4.dp, MaterialTheme.shapes.medium, true)
                    .background(colors.white, MaterialTheme.shapes.medium)
                    .padding(dimens.smallPadding)
            ) {
                LazyColumn {
                    items(menuList.size) { index ->
                        val idString = stringResource(strings.idCopied)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when(menuList[index].first){
                                        "copyId" -> {
                                            clipBoardEvent(offer.id.toString())
                                            mainViewModel.sendEvent(
                                                UIMainEvent.UpdateToast(
                                                    ToastItem(isVisible = true, message = idString, type = ToastType.SUCCESS)
                                                ))
                                            onClose()
                                        }
                                        "share" -> {

                                        }
                                        "calendar" -> {

                                        }
                                    }

                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            var icon: Painter? = null
                            when(menuList[index].first){
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

                            icon?.let {
                                Icon(
                                    icon,
                                    contentDescription = stringResource(strings.shareOffer),
                                    modifier = Modifier.size(dimens.smallIconSize),
                                    tint = colors.steelBlue
                                )
                            }

                            Text(
                                menuList[index].second,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
