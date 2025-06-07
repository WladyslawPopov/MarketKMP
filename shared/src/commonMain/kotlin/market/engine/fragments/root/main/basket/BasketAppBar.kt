package market.engine.fragments.root.main.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.texts.TextAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketAppBar(
    title : String,
    subtitle : String?,
    listItems : List<NavigationItem>,
    menuItems : List<MenuItem>,
    showMenu : MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        TopAppBar(
            modifier = modifier
                .fillMaxWidth(),
            title = {
                Column(
                    modifier = modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    TextAppBar(title)

                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.titleTextColor
                        )
                    }
                }
            },
            actions = {
                Column {
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

                    PopUpMenu(
                        openPopup = showMenu.value,
                        menuList = menuItems,
                    ) {
                        showMenu.value = false
                    }
                }
            }
        )
    }
}
