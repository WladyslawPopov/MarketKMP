package market.engine.fragments.basket

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.clipBoardEvent
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ToastType
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dropdown_menu.getDropdownMenu
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
            title = strings.menuTitle,
            icon = drawables.menuIcon,
            tint = colors.steelBlue,
            hasNews = false,
            badgeCount = null,
            onClick = {
                showMenu.value = true
            }
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth(),
        title = {
            Column(
                modifier = modifier.fillMaxWidth(),
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
            Row(
                modifier = modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        IconButton(
                            modifier = modifier.size(dimens.smallIconSize),
                            onClick = { item.onClick() }
                        ) {
                            getBadgedBox(modifier = modifier, item)
                        }
                    }
                }
            }
        }
    )

    AnimatedVisibility(showMenu.value) {
        DropdownMenu(
            modifier = modifier.widthIn(max = 350.dp).heightIn(max = 400.dp),
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
                onClick = clearBasket
            )
        }
    }
}
