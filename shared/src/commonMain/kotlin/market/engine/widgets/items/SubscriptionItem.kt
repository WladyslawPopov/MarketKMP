package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.MenuItem
import market.engine.core.data.states.SubItemState
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionItem(
    state: SubItemState,
    updateId : Long?
) {
    val events = state.events
    val subscription = state.subscription
    val user = remember(subscription.sellerData) { subscription.sellerData }
    val menuList = remember {
        mutableStateOf<List<MenuItem>>(emptyList())
    }

    if(subscription.id == 1L) return

    LaunchedEffect(updateId) {
        if (updateId == subscription.id) {
            events.onUpdateItem()
        }
    }

    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small,
        onClick = {
            events.onItemClick()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
        ) {
            //header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                Card(
                    modifier = Modifier.wrapContentSize().padding(dimens.smallPadding),
                    shape = CircleShape,
                    colors = colors.cardColors
                ) {
                    if (user != null) {
                        LoadImage(
                            url = user.avatar?.thumb?.content ?: "",
                            modifier = Modifier.size(40.dp),
                            isShowLoading = false,
                            isShowEmpty = false
                        )
                    } else {
                        Icon(
                            painter = painterResource(drawables.searchIcon),
                            contentDescription = null,
                            tint = colors.black,
                            modifier = Modifier.size(dimens.mediumIconSize)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
                ) {
                    Text(
                        text = if (user != null) "${user.login} (${user.rating})" else subscription.name
                            ?: subscription.searchQuery ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (user != null) colors.brightBlue else colors.black,
                    )

                    if (subscription.catpath != null) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            subscription.catpath?.toList()?.reversed()
                                ?.forEachIndexed { index, cat ->
                                    Text(
                                        text = if ((subscription.catpath?.size
                                                ?: 0) - 1 == index
                                        )
                                            cat.second
                                        else cat.second + "->",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if ((subscription.catpath?.size
                                                ?: 0) - 1 == index
                                        ) colors.black else colors.steelBlue,
                                    )
                                }
                        }
                    }
                }

                Column {
                    SmallIconButton(
                        drawables.menuIcon,
                        color = colors.black,
                    ) {
                        events.getMenuOperations {
                            menuList.value = it
                        }
                    }

                    PopUpMenu(
                        openPopup = menuList.value.isNotEmpty(),
                        menuList = menuList.value,
                        onClosed = { menuList.value = emptyList() }
                    )
                }
            }
            //body
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimens.mediumPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                //search param
                if (subscription.searchQuery != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.searchTitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.grayText
                        )

                        Text(
                            subscription.searchQuery ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.black
                        )
                    }
                }
                //region param
                if (subscription.region != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.searchTitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.grayText
                        )

                        Text(
                            subscription.region?.name ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.black
                        )
                    }
                }
                // price param
                if (subscription.priceTo != null || subscription.priceFrom != null) {
                    val price = buildString {
                        if (subscription.priceFrom != null) append(
                            "${stringResource(strings.fromAboutParameterName)} ${subscription.priceFrom} ${
                                stringResource(
                                    strings.currencySign
                                )
                            }"
                        )
                        if (subscription.priceFrom != null && subscription.priceTo != null) append(
                            " - "
                        )
                        if (subscription.priceTo != null) append(
                            "${stringResource(strings.toAboutParameterName)}  ${subscription.priceTo} ${
                                stringResource(
                                    strings.currencySign
                                )
                            }"
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.priceParameterName),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.grayText
                        )

                        Text(
                            price,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.black
                        )
                    }
                }
                // sale type
                if (subscription.saleType != null) {
                    var typeString = ""
                    var colorType = colors.titleTextColor
                    when (subscription.saleType) {
                        "buy_now" -> {
                            typeString = stringResource(strings.buyNow)
                            colorType = colors.buyNowColor
                        }

                        "ordinary_auction" -> {
                            typeString = stringResource(strings.ordinaryAuction)
                        }

                        "auction_with_buy_now" -> {
                            typeString = stringResource(strings.blitzAuction)
                            colorType = colors.auctionWithBuyNow
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(strings.saleTypeParameterName),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.grayText
                        )

                        Text(
                            typeString,
                            style = MaterialTheme.typography.titleSmall,
                            color = colorType
                        )
                    }
                }
            }
            //footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(drawables.mail),
                    contentDescription = "",
                    modifier = Modifier.size(dimens.smallIconSize),
                    tint = colors.brightBlue
                )

                Text(
                    text = stringResource(
                        if (subscription.isEnabled)
                            strings.subscriptionOnLabel
                        else strings.subscriptionOffLabel
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textA0AE
                )

                Switch(
                    checked = subscription.isEnabled,
                    onCheckedChange = {
                        events.changeActiveSub()
                    },
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = colors.transparent,
                        checkedThumbColor = colors.positiveGreen,
                        checkedTrackColor = colors.transparentGrayColor,
                        uncheckedBorderColor = colors.transparent,
                        uncheckedThumbColor = colors.negativeRed,
                        uncheckedTrackColor = colors.transparentGrayColor,
                    ),
                )
            }
        }
    }
}
