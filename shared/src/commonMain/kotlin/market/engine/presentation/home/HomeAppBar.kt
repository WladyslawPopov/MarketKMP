package market.engine.presentation.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.MenuHamburgerButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
) {
    val userInfo = UserData.userInfo

    val listItems = listOf(
        NavigationItem(
            title = strings.proposalTitle,
            icon = drawables.currencyIcon,
            tint = colors.notifyTextColor,
            hasNews = false,
            badgeCount = userInfo?.countUnreadPriceProposals,
            isVisible = (userInfo?.countUnreadPriceProposals ?: 0) > 0
        ),
        NavigationItem(
            title = strings.messageTitle,
            icon = drawables.mail,
            tint = colors.brightBlue,
            hasNews = false,
            badgeCount = if ((userInfo?.countUnreadMessages?:0) > 0) (userInfo?.countUnreadMessages?:0) else null,
        ),
        NavigationItem(
            title = strings.notificationTitle,
            icon = drawables.notification,
            tint = colors.titleTextColor,
            hasNews = false,
            badgeCount = null
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth().wrapContentHeight(),
        title = {
            Icon(
                painter = painterResource(drawables.logo),
                contentDescription = stringResource(strings.homeTitle),
                modifier = modifier,
                tint = colors.titleTextColor,
            )
        },
        navigationIcon = {
            MenuHamburgerButton(
                drawerState
            )
        },
        actions = {
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        var modIB = modifier
                        if(item.badgeCount != null){
                            val dynamicFontSize = (30 + (item.badgeCount / 10)).coerceAtMost(35).dp
                            modIB = modifier.size(dimens.smallIconSize + dynamicFontSize)
                        }
                        IconButton(
                            modifier = modIB,
                            onClick = {

                            }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                }
            }
        }
    )
}
