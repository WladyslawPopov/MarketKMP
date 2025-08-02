package market.engine.widgets.bars.appBars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.SimpleAppBarData
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.MenuHamburgerButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerAppBar(
    modifier: Modifier = Modifier,
    data: SimpleAppBarData,
    drawerState: DrawerState,
    color: Color = colors.white,
    content: @Composable () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            content()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = color,
            scrolledContainerColor = color
        ),
        navigationIcon = {
            MenuHamburgerButton(
                drawerState
            )
        },
        actions = {
            Column {
                Row(
                    modifier = Modifier.padding(end = dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        alignment = Alignment.End
                    )
                ) {
                    data.listItems.forEachIndexed { _, item ->
                        if (item.isVisible) {
                            BadgedButton(item)
                        }
                    }
                }
            }
        }
    )
}
