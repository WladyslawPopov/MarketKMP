package market.engine.widgets.bars.appBars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.dropdown_menu.PopUpMenu

data class SimpleAppBarData(
    val color: Color = colors.white,
    val onBackClick: (() -> Unit)? = null,
    val listItems: List<NavigationItem> = emptyList(),
    val showMenu: MutableState<Boolean> = mutableStateOf(false),
    val menuItems: List<MenuItem> = emptyList(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAppBar(
    modifier: Modifier = Modifier,
    data: SimpleAppBarData,
    content : @Composable () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = {
            content()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = data.color
        ),
        navigationIcon = if (data.onBackClick != null) {
            {
                NavigationArrowButton {
                    data.onBackClick.invoke()
                }
            }
        }else{
            {}
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

                PopUpMenu(
                    openPopup = data.showMenu.value,
                    menuList = data.menuItems,
                ) {
                    data.showMenu.value = false
                }
            }
        }
    )
}
