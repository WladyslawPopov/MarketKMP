package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.dropdown_menu.PopUpMenu

interface SimpleAppBarData{
    val modifier: Modifier
    val color: Color
    val content : @Composable () -> Unit
    val onBackClick: (() -> Unit)?
    val listItems: List<NavigationItem>
    val showMenu: MutableState<Boolean>
    val menuItems: List<MenuItem>
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAppBar(
    data: SimpleAppBarData,
) {
    TopAppBar(
        modifier = data.modifier,
        title = {
            data.content()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = data.color
        ),
        navigationIcon = if (data.onBackClick != null) {
            {
                NavigationArrowButton {
                    data.onBackClick?.invoke()
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
