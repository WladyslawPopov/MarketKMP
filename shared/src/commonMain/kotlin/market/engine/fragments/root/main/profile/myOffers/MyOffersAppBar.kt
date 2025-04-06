package market.engine.fragments.root.main.profile.myOffers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.PlatformWindowType
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersAppBar(
    currentTab : LotsType,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    showMenu : Boolean? = null,
    openMenu : ((CoroutineScope) -> Unit)? = null,
    navigationClick : (LotsType) -> Unit,
    onRefresh: () -> Unit
) {
    val active = stringResource(strings.activeTab)
    val inactive = stringResource(strings.inactiveTab)
    val future = stringResource(strings.futureTab)

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
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            MenuHamburgerButton(
                showMenu = showMenu,
                drawerState = drawerState,
                openMenu = openMenu
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                item {
                    SimpleTextButton(
                        active,
                        backgroundColor = if (currentTab == LotsType.MYLOT_ACTIVE) colors.rippleColor else colors.white,
                        textStyle = MaterialTheme.typography.bodySmall
                    ) {
                        navigationClick(LotsType.MYLOT_ACTIVE)
                    }
                }
                item {
                    SimpleTextButton(
                        inactive,
                        if (currentTab == LotsType.MYLOT_UNACTIVE) colors.rippleColor else colors.white,
                        textStyle = MaterialTheme.typography.bodySmall
                    ) {
                        navigationClick(LotsType.MYLOT_UNACTIVE)
                    }
                }
                item {
                    SimpleTextButton(
                        future,
                        if (currentTab == LotsType.MYLOT_FUTURE) colors.rippleColor else colors.white,
                        textStyle = MaterialTheme.typography.bodySmall
                    ) {
                        navigationClick(LotsType.MYLOT_FUTURE)
                    }
                }
            }
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
