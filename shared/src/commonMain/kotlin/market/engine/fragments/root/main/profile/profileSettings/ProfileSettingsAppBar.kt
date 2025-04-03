package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.tabs.SimpleTabs
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsAppBar(
    currentTab : Int,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    showMenu : Boolean? = null,
    openMenu : ((CoroutineScope) -> Unit)? = null,
    navigationClick : (Int) -> Unit,
) {
    val tabs = listOf(
        stringResource(strings.profileGlobalSettingsLabel),
        stringResource(strings.profileSellerSettingsLabel),
        stringResource(strings.profileAdditionalSettingsLabel),
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.white,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            MenuHamburgerButton(
                drawerState,
                showMenu = showMenu,
                openMenu = openMenu
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            SimpleTabs(
                tabs,
                selectedTab = currentTab,
                onTabSelected = { index ->
                    navigationClick(index)
                }
            )
        }
    )
}
