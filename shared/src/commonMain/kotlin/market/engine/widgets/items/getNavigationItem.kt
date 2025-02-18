package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.getBadge
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun getNavigationItem(
    item: NavigationItem,
    label: @Composable () -> Unit,
    isSelected: Boolean = false,
    badgeColor: Color = colors.negativeRed
) {
    NavigationDrawerItem(
        label = label,
        onClick = {
            item.onClick()
        },
        icon = {
            when{
                item.icon != null -> {
                    Icon(
                        painter = painterResource(item.icon!!),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.smallIconSize),
                        tint = item.tint
                    )
                }
                item.image != null -> {
                    Image(
                        painter = painterResource(item.image!!),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.smallIconSize),
                    )
                }
                item.imageString != null -> {
                    LoadImage(
                        item.imageString,
                        isShowLoading = false,
                        isShowEmpty = false,
                        size = dimens.mediumIconSize,
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        },
        badge = {
            getBadge(item.badgeCount, item.hasNews, color = badgeColor)
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = colors.rippleColor,
            unselectedContainerColor = colors.white,
            selectedIconColor = colors.black,
            unselectedIconColor = colors.black,
            selectedTextColor = colors.black,
            selectedBadgeColor = colors.black,
            unselectedTextColor = colors.black,
            unselectedBadgeColor = colors.black
        ),
        shape = MaterialTheme.shapes.small,
        selected = isSelected
    )
}
