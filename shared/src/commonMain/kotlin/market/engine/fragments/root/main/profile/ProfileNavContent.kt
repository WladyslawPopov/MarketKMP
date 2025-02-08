package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.dialogs.LogoutDialog
import market.engine.widgets.bars.UserPanel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileNavContent(
    list: List<NavigationItem>,
    activeTitle: StringResource?=null,
    goToAllLots: (() -> Unit)?=null,
    goToAboutMe: (() -> Unit)?=null,
    goToSubscriptions: (() -> Unit)?=null,
) {
    val userInfo = UserData.userInfo

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(goToAllLots != null || goToAboutMe != null || goToSubscriptions != null) {
            item {
                UserPanel(
                    modifier = Modifier.fillMaxWidth(),
                    userInfo,
                    updateTrigger = 1,
                    goToUser = null,
                    goToAllLots = {
                        goToAllLots?.invoke()
                    },
                    goToAboutMe = {
                        goToAboutMe?.invoke()
                    },
                    addToSubscriptions = {

                    },
                    goToSubscriptions = {
                        goToSubscriptions?.invoke()
                    },
                    isBlackList = arrayListOf()
                )
            }
        }
        itemsIndexed(list) { _, item ->
            val showLogoutDialog = remember { mutableStateOf(false) }
            if (item.isVisible) {
                NavigationDrawerItem(
                    label = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                val builder = buildAnnotatedString {
                                    if (item.icon != drawables.balanceIcon){
                                        append(stringResource(item.title))
                                    }else{
                                        val color = when{
                                            (UserData.userInfo?.balance?:0.0) > 0 -> colors.positiveGreen
                                            (UserData.userInfo?.balance?:0.0) < 0 -> colors.negativeRed
                                            else -> colors.black
                                        }
                                        append(stringResource(item.title))
                                        withStyle(
                                            SpanStyle(
                                                color = color,
                                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ){
                                            append(" ${UserData.userInfo?.balance} ${
                                                stringResource(
                                                strings.currencyCode)
                                            }")
                                        }
                                    }
                                }
                                Text(
                                    builder,
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
                    onClick = {
                        if(item.icon == drawables.logoutIcon){
                            showLogoutDialog.value = true
                        }else{
                            item.onClick()
                        }
                    },
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
                            Badge { }
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
                if(showLogoutDialog.value) {
                    LogoutDialog(
                        showLogoutDialog.value,
                        onDismiss = {
                            showLogoutDialog.value = false
                        },
                        goToLogin = {
                            item.onClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(dimens.smallPadding))
            }
        }
    }
}
