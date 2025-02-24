package market.engine.fragments.root.main.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketAppBar(
    title : String,
    subtitle : String?,
    modifier: Modifier = Modifier,
    clearBasket: () -> Unit,
) {
    val showMenu = remember {
        mutableStateOf(false)
    }

    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.menuTitle),
            icon = drawables.menuIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                showMenu.value = true
            }
        ),
    )

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

                    if(showMenu.value){
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false },
                            containerColor = colors.white,
                        ) {
                            val s = stringResource(strings.actionClearBasket)

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = s,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.black
                                    )
                                },
                                onClick = {
                                    showMenu.value = false
                                    clearBasket()
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}
