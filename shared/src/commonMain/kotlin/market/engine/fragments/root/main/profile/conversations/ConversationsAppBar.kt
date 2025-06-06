package market.engine.fragments.root.main.profile.conversations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.PlatformWindowType
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsAppBar(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    showMenu : Boolean? = null,
    openMenu : ((CoroutineScope) -> Unit)? = null,
    onRefresh: () -> Unit
) {
    val listItems = remember {
        listOf(
            NavigationItem(
                title = "",
                icon = drawables.recycleIcon,
                tint = colors.inactiveBottomNavIconColor,
                hasNews = false,
                isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                badgeCount = null,
                onClick = onRefresh
            ),
        )
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            MenuHamburgerButton(
                drawerState = drawerState,
                openMenu = openMenu,
                showMenu = showMenu
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            TextAppBar(
                stringResource(strings.messageTitle)
            )
        },
        actions = {
            Column {
                Row(
                    modifier = modifier.padding(end = dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        alignment = Alignment.End
                    )
                ) {
                    listItems.forEachIndexed { _, item ->
                        if(item.isVisible){
                            BadgedButton(item)
                        }
                    }
                }
            }
        }
    )
}
