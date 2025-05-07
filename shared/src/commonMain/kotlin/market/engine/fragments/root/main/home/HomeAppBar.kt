package market.engine.fragments.root.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.PlatformWindowType
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.MenuHamburgerButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    goToMessenger: () -> Unit,
    goToMyProposals: () -> Unit,
    goToNotifications: () -> Unit,
    onRefresh: () -> Unit
) {
    val userInfo = UserData.userInfo

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
            title = stringResource(strings.proposalTitle),
            icon = drawables.currencyIcon,
            tint = colors.titleTextColor,
            hasNews = false,
            badgeCount = userInfo?.countUnreadPriceProposals,
            isVisible = (userInfo?.countUnreadPriceProposals ?: 0) > 0,
            onClick = {
                goToMyProposals()
            }
        ),
        NavigationItem(
            title = stringResource(strings.messageTitle),
            icon = drawables.mail,
            tint = colors.brightBlue,
            hasNews = false,
            badgeCount = if ((userInfo?.countUnreadMessages?:0) > 0) (userInfo?.countUnreadMessages?:0) else null,
            onClick = {
                goToMessenger()
            }
        ),
        NavigationItem(
            title = stringResource(strings.notificationTitle),
            icon = drawables.notification,
            tint = colors.titleTextColor,
            isVisible = true,
            hasNews = false,
            badgeCount = null,
            onClick = {
                goToNotifications()
            }
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth().wrapContentHeight(),
        title = {
            Image(
                painter = painterResource(drawables.logo),
                contentDescription = stringResource(strings.homeTitle),
                modifier = Modifier.size(140.dp, 68.dp),
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
