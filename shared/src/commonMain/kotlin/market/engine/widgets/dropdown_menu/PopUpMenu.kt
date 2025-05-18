package market.engine.widgets.dropdown_menu

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.MenuItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PopUpMenu(
    openPopup: Boolean,
    menuList: List<MenuItem>,
    onClosed: () -> Unit,
) {
    DropdownMenu(
        modifier = Modifier.widthIn(max = 350.dp).heightIn(max = 800.dp),
        expanded = openPopup,
        onDismissRequest = { onClosed() },
        containerColor = colors.white,
        offset = DpOffset(0.dp, 0.dp)
    ) {
        menuList.forEach { menu ->
            DropdownMenuItem(
                leadingIcon =
                    menu.icon?.let {
                        {
                            Icon(
                                painterResource(menu.icon!!),
                                contentDescription = stringResource(strings.shareOffer),
                                modifier = Modifier.size(dimens.smallIconSize),
                                tint = colors.steelBlue
                            )
                        }
                    },
                text = {
                    Text(
                        text = menu.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                },
                onClick = {
                    menu.onClick()
                    onClosed()
                }
            )
            if (menuList.indexOf(menu) != menuList.lastIndex)
                Divider()
        }
    }
}
