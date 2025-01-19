package market.engine.fragments.root.main.profile.myOrders

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
import market.engine.core.data.types.DealType
import market.engine.core.data.types.DealTypeGroup
import market.engine.widgets.buttons.MenuHamburgerButton
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrderAppBar(
    currentTab : DealType,
    typeGroup : DealTypeGroup,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    navigationClick : (DealType) -> Unit,
) {
    val tabs = when (typeGroup){
        DealTypeGroup.BUY -> {
            listOf(
                DealType.BUY_IN_WORK to strings.tabInWorkLabel,
                DealType.BUY_ARCHIVE to strings.tabArchiveLabel
            )
        }
        DealTypeGroup.SELL -> {
            listOf(
                DealType.SELL_ALL to strings.tabAllLabel,
                DealType.SELL_IN_WORK to strings.tabInWorkLabel,
                DealType.SELL_ARCHIVE to strings.tabArchiveLabel,
            )
        }
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
                tabs.forEach { tab ->
                    SimpleTextButton(
                        stringResource(tab.second),
                        backgroundColor = if (currentTab == tab.first) colors.rippleColor else colors.white,
                        textStyle = MaterialTheme.typography.bodyMedium,
                    ) {
                        navigationClick(tab.first)
                    }
                }
            }
        }
    )
}
