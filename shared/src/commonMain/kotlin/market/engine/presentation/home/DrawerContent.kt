package market.engine.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.globalData.SAPI
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.core.repositories.UserRepository
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    mod: Modifier = Modifier,
    goToLogin: () -> Unit = {}
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

    val userRepository : UserRepository = koinInject()
    val analyticsHelper : AnalyticsHelper = koinInject()

    val isShowDialog = remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.black,
        drawerTonalElevation = 0.dp,
        modifier = mod.wrapContentWidth()
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .padding(dimens.mediumPadding),
                horizontalArrangement = Arrangement.End
            ) {
                if (UserData.token != "") {
                    if (isShowDialog.value) {
                        AlertDialog(
                            onDismissRequest = { isShowDialog.value = false },
                            title = {  },
                            text = { Text(stringResource(strings.checkForLogoutTitle)) },
                            confirmButton = {
                                SimpleTextButton(
                                    text = stringResource(strings.logoutTitle),
                                    backgroundColor = colors.grayLayout,
                                    onClick = {
                                        analyticsHelper.reportEvent("logout_success", "")
                                        isShowDialog.value = false
                                        userRepository.delete()
                                        goToLogin()
                                    }
                                )
                            },
                            dismissButton = {
                                SimpleTextButton(
                                    text = stringResource(strings.closeWindow),
                                    backgroundColor = colors.grayLayout,
                                    onClick = {
                                        isShowDialog.value = false
                                    }
                                )
                            }
                        )
                    }

                    TextButton(
                        onClick = {
                            isShowDialog.value = true
                        },
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
                        onClick = {
                            goToLogin()
                        },
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

            list.forEachIndexed { _, item ->
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
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        .wrapContentWidth(),
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
                    .padding(dimens.mediumPadding),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = SAPI.version,
                    color = colors.textA0AE,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize
                )
            }
        }
    }
}


