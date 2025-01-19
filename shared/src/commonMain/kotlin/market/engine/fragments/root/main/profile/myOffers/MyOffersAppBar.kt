package market.engine.fragments.root.main.profile.myOffers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.LotsType
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersAppBar(
    currentTab : LotsType,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    navigationClick : (LotsType) -> Unit,
) {
    val active = stringResource(strings.activeTab)
    val inactive = stringResource(strings.inactiveTab)
    val future = stringResource(strings.futureTab)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            MenuHamburgerButton(
                drawerState
            )
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){

                SimpleTextButton(
                    active,
                    backgroundColor = if (currentTab == LotsType.MYLOT_ACTIVE) colors.rippleColor else colors.white,
                    textStyle = MaterialTheme.typography.bodySmall
                ){
                    navigationClick(LotsType.MYLOT_ACTIVE)
                }

                SimpleTextButton(
                    inactive,
                    if (currentTab == LotsType.MYLOT_UNACTIVE) colors.rippleColor else colors.white,
                    textStyle = MaterialTheme.typography.bodySmall
                ){
                    navigationClick(LotsType.MYLOT_UNACTIVE)
                }

                SimpleTextButton(
                    future,
                    if (currentTab == LotsType.MYLOT_FUTURE) colors.rippleColor else colors.white,
                    textStyle = MaterialTheme.typography.bodySmall
                ){
                    navigationClick(LotsType.MYLOT_FUTURE)
                }
            }
        }
    )
}
