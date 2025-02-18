package market.engine.fragments.root.main.messenger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.network.networkObjects.Conversations
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.rows.UserRow
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogsAppBar(
    conversation: Conversations,
    modifier: Modifier = Modifier,
    onMenuClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    goToUser: (Long) -> Unit
) {
    val showMenu = remember { mutableStateOf(false) }
    val onClose = {
        showMenu.value = false
    }

    val menuList = listOf(
        "copyId" to if(conversation.aboutObjectClass == "offer")
            stringResource(strings.copyOfferId)
        else stringResource(strings.copyOrderId),
        "delete_dialog" to stringResource(strings.deleteDialogLabel),
    )

    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.favoritesTitle),
            icon = drawables.recycleIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            badgeCount = null,
            onClick = onRefresh
        ),
        NavigationItem(
            title = stringResource(strings.menuTitle),
            icon = drawables.menuIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                showMenu.value = true
            }
        )
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.white,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        navigationIcon = {
            NavigationArrowButton {
                onBack()
            }
        },
        modifier = modifier
            .fillMaxWidth(),
        title = {
            if (conversation.interlocutor != null) {
                UserRow(
                    user = conversation.interlocutor!!,
                    modifier = Modifier.clickable {
                        goToUser(conversation.interlocutor?.id!!)
                    }
                )
            }else{
                TextAppBar(
                    stringResource(strings.messageTitle)
                )
            }
        },
        actions = {
            Column{
                Row(
                    modifier = modifier.padding(end = dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    listItems.forEachIndexed { _, item ->
                        if (item.isVisible) {
                            IconButton(
                                modifier = modifier.size(50.dp),
                                onClick = { item.onClick() }
                            ) {
                                getBadgedBox(modifier = modifier, item)
                            }
                        }
                    }
                }

                DropdownMenu(
                    modifier = modifier.widthIn(max = 350.dp).heightIn(max = 400.dp),
                    expanded = showMenu.value,
                    onDismissRequest = { onClose() },
                    containerColor = colors.white,
                    offset = DpOffset(0.dp, 0.dp)
                ) {
                    menuList.forEach { operation ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = operation.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.black
                                )
                            },
                            onClick = {
                                onMenuClick(operation.first)
                            }
                        )
                    }
                }
            }
        }
    )
}
