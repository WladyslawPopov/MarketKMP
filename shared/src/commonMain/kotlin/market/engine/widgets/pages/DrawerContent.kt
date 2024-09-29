package market.engine.widgets.pages


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.items.NavigationItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    mod: Modifier = Modifier,
) {
    val list = listOf(
        NavigationItem(
            title = stringResource(strings.top100Title),
            subtitle = stringResource(strings.top100Subtitle),
            icon = drawables.top100Icon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.helpTitle),
            subtitle = stringResource(strings.helpSubtitle),
            icon = drawables.helpIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.contactUsTitle),
            subtitle = stringResource(strings.contactUsSubtitle),
            icon = drawables.contactUsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.aboutUsTitle),
            subtitle = stringResource(strings.aboutUsSubtitle),
            icon = drawables.infoIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.reviewsTitle),
            subtitle = stringResource(strings.reviewsSubtitle),
            icon = drawables.starIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.settingsTitleApp),
            subtitle = stringResource(strings.settingsSubtitleApp),
            icon = drawables.settingsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
    )

    ModalDrawerSheet(
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.white,
        modifier = mod
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.mediumPadding),
            horizontalArrangement = Arrangement.End
        ) {
            if (false) {
                TextButton(
                    onClick = {},
                    modifier = Modifier.padding(dimens.smallPadding),
                    colors = colors.simpleButtonColors
                ) {
                    Text(stringResource(strings.logoutTitle), color = colors.black)
                    Icon(
                        painter = painterResource(drawables.logoutIcon),
                        tint = colors.black,
                        contentDescription = stringResource(strings.logoutTitle)
                    )
                }
            } else {
                TextButton(
                    onClick = {},
                    modifier = Modifier.padding(dimens.smallPadding),
                    colors = colors.simpleButtonColors
                ) {
                    Text(stringResource(strings.loginTitle), color = colors.black)
                    Icon(
                        painter = painterResource(drawables.loginIcon),
                        tint = colors.black,
                        contentDescription = stringResource(strings.loginTitle)
                    )
                }
            }
        }

        list.forEachIndexed { index, item ->
            Spacer(modifier = Modifier.height(dimens.mediumSpacer))

            NavigationDrawerItem(
                label = {
                    Box(
                        modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column{
                            Text(
                                item.title,
                                color = colors.black,
                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                lineHeight = dimens.largeText
                            )
                            if (item.subtitle != null) {
                                Text(
                                    item.subtitle,
                                    color = colors.steelBlue,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                    lineHeight = dimens.largeText
                                )
                            }
                        }

                    }
                },
                onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(dimens.smallIconSize)
                    )
                },
                badge = {
                    if (item.badgeCount != null) {
                        Text(text = item.badgeCount.toString())
                    }

                    if (item.hasNews) {
                        Badge {  }
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).wrapContentWidth(),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = colors.white,
                    unselectedContainerColor = colors.white,
                    selectedIconColor = colors.lightGray,
                    unselectedIconColor = colors.lightGray,
                    selectedTextColor = colors.lightGray,
                    selectedBadgeColor = colors.lightGray,
                    unselectedTextColor = colors.lightGray,
                    unselectedBadgeColor = colors.lightGray

                ),

                selected = true
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.mediumPadding),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = " Version 1.8.0",
                color = colors.textA0AE,
                fontSize = MaterialTheme.typography.labelMedium.fontSize
            )
        }
    }
}


