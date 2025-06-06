package market.engine.fragments.root.main.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    list : List<NavigationItem>,
) {
    val isShowDialog = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        drawerContainerColor = colors.primaryColor,
        drawerContentColor = colors.black,
        drawerTonalElevation = 0.dp,
        modifier = if(!isBigScreen.value) Modifier.fillMaxWidth(0.8f) else Modifier.wrapContentWidth(),
    ) {
        LazyColumnWithScrollBars {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
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
                    ){
                        scope.launch {
                            item.onClick()
                            delay(300)
                            drawerState.close()
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = SAPI.version,
                        color = colors.grayText,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                    )
                }
            }

            item {
                LogoutDialog(
                    isShowDialog.value,
                    onDismiss = {
                        isShowDialog.value = false
                    },
                    goToLogin
                )
            }
        }
    }
}
