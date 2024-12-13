package market.engine.widgets.exceptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.items.NavigationItem
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun ProfileDrawer(
    activeTitle: StringResource,
    list: List<NavigationItem>,
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.8f),
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.black,
        drawerTonalElevation = 0.dp,
    ) {
        Column {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                list.forEachIndexed { _, item ->
                    Spacer(modifier = Modifier.height(dimens.smallSpacer))

                    NavigationDrawerItem(
                        label = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ){
                                    Text(
                                        stringResource(item.title),
                                        color = colors.black,
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                        lineHeight = dimens.largeText,
                                    )
                                    if (item.subtitle != null) {
                                        Text(
                                            stringResource(item.subtitle),
                                            color = colors.steelBlue,
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                            lineHeight = dimens.largeText
                                        )
                                    }
                                }

                            }
                        },
                        onClick = item.onClick,
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = stringResource(item.title),
                                modifier = Modifier.size(dimens.smallIconSize),
                                tint = item.tint
                            )
                        },
                        badge = {
                            if (item.badgeCount != null) {
                                Badge {
                                    Text(text = item.badgeCount.toString())
                                }
                            }

                            if (item.hasNews) {
                                Badge {  }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = colors.outgoingBubble,
                            unselectedContainerColor = colors.white,
                            selectedIconColor = colors.textA0AE,
                            unselectedIconColor = colors.textA0AE,
                            selectedTextColor = colors.black,
                            selectedBadgeColor = colors.black,
                            unselectedTextColor = colors.black,
                            unselectedBadgeColor = colors.black
                        ),
                        shape = MaterialTheme.shapes.small,
                        selected = item.title == activeTitle
                    )
                }
                Spacer(modifier = Modifier.height(dimens.mediumSpacer))
            }
        }
    }
}
