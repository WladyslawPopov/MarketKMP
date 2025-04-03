package market.engine.fragments.root.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import market.engine.common.openUrl
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.dialogs.LogoutDialog
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    goToLogin: () -> Unit = {},
    goToContactUs: () -> Unit = {},
    goToSettings: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val list = listOf(
        NavigationItem(
            title = stringResource(strings.top100Title),
            subtitle = stringResource(strings.top100Subtitle),
            icon = drawables.top100Icon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                scope.launch {
                    openUrl("${SAPI.SERVER_BASE}rating_game")
                    drawerState.close()
                }
            }
        ),
        NavigationItem(
            title = stringResource(strings.helpTitle),
            subtitle = stringResource(strings.helpSubtitle),
            icon = drawables.helpIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                scope.launch {
                    openUrl("${SAPI.SERVER_BASE}help/general")
                    drawerState.close()
                }
            }
        ),
        NavigationItem(
            title = stringResource(strings.contactUsTitle),
            subtitle = stringResource(strings.contactUsSubtitle),
            icon = drawables.contactUsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                scope.launch {
                    goToContactUs()
                    drawerState.close()
                }
            }
        ),
        NavigationItem(
            title = stringResource(strings.aboutUsTitle),
            subtitle = stringResource(strings.aboutUsSubtitle),
            icon = drawables.infoIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                scope.launch {
                    openUrl("${SAPI.SERVER_BASE}staticpage/doc/about_us")
                    drawerState.close()
                }
            }
        ),
        NavigationItem(
            title = stringResource(strings.reviewsTitle),
            subtitle = stringResource(strings.reviewsSubtitle),
            icon = drawables.starIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            isVisible = SAPI.REVIEW_URL != "",
            onClick = {
                openUrl(SAPI.REVIEW_URL)
            }
        ),
        NavigationItem(
            title = stringResource(strings.settingsTitleApp),
            subtitle = stringResource(strings.settingsSubtitleApp),
            icon = drawables.settingsIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = {
                scope.launch {
                    goToSettings()
                    drawerState.close()
                }
            }
        ),
    )

    val isShowDialog = remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.black,
        drawerTonalElevation = 0.dp,
        modifier = if(!isBigScreen) Modifier.fillMaxWidth(0.8f) else Modifier.wrapContentWidth(),
    ) {
        LazyColumnWithScrollBars {
            item {
                Row(
                    modifier = Modifier
                        .padding(dimens.mediumPadding),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (UserData.token != "") {
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

                        LogoutDialog(
                            isShowDialog.value,
                            onDismiss = {
                                isShowDialog.value = false
                            },
                            goToLogin
                        )
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
            }
            items(list) { item ->
                if (item.isVisible) {
                    Spacer(modifier = Modifier.height(dimens.mediumSpacer))
                    getNavigationItem(
                        item,
                        label = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    item.title,
                                    color = colors.black,
                                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                    lineHeight = dimens.largeText
                                )
                                if (item.subtitle != null) {
                                    Text(
                                        item.subtitle,
                                        color = colors.grayText,
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        lineHeight = dimens.largeText
                                    )
                                }
                            }
                        }
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .padding(dimens.mediumPadding),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = SAPI.version,
                        color = colors.grayText,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                    )
                }
            }
        }
    }
}
