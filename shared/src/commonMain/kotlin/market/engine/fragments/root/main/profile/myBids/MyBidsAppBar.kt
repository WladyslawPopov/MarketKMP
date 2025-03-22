package market.engine.fragments.root.main.profile.myBids

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
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
fun MyBidsAppBar(
    currentTab : LotsType,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    navigationClick : (LotsType) -> Unit,
) {
    val active = stringResource(strings.activeTab)
    val inactive = stringResource(strings.inactiveTab)

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
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                item {
                    SimpleTextButton(
                        active,
                        backgroundColor = if (currentTab == LotsType.MYBIDLOTS_ACTIVE) colors.rippleColor else colors.white,
                        textStyle = MaterialTheme.typography.bodySmall
                    ) {
                        navigationClick(LotsType.MYBIDLOTS_ACTIVE)
                    }
                }
                item {
                    SimpleTextButton(
                        inactive,
                        if (currentTab == LotsType.MYBIDLOTS_UNACTIVE) colors.rippleColor else colors.white,
                        textStyle = MaterialTheme.typography.bodySmall
                    ) {
                        navigationClick(LotsType.MYBIDLOTS_UNACTIVE)
                    }
                }
            }
        }
    )
}
