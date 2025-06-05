package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.OfferItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.onClickOfferOperationItem
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.dialogs.OfferMessagingDialog
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.dialogs.OfferOperationsDialogs
import market.engine.widgets.dropdown_menu.PopUpMenu
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.rows.UserRow
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CabinetBidsItem(
    offer: OfferItem,
    onUpdateOfferItem : (Long) -> Unit,
    baseViewModel: BaseViewModel,
    updateTrigger : Int,
    goToUser: (Long) -> Unit,
    goToOffer: (Long) -> Unit,
    goToMyPurchases: () -> Unit,
    goToDialog: (Long?) -> Unit
) {
    if(updateTrigger < 0) return

    val showMesDialog = remember { mutableStateOf(false) }

    val isOpenPopup = remember { mutableStateOf(false) }

    val showDialog = remember { mutableStateOf("") }

    val title = remember { mutableStateOf(AnnotatedString("")) }
    val fields = remember { mutableStateOf< ArrayList<Fields>>(arrayListOf()) }

    val menuList = remember {
        mutableStateOf<List<MenuItem>>(emptyList())
    }

    val currentDate = getCurrentDate().toLongOrNull() ?: 1L
    val isActive = ((offer.session?.end?.toLongOrNull() ?: 1L) > currentDate)

    val date1 = offer.session?.start?.convertDateWithMinutes()
    val date2 = offer.session?.end?.convertDateWithMinutes()
    val d3 = "$date1 – $date2"

    if(offer.bids?.isNotEmpty() == true) {
        Card(
            colors = colors.cardColors,
            shape = MaterialTheme.shapes.small,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(dimens.smallPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                horizontalAlignment = Alignment.Start
            ) {
                HeaderOfferBar(
                    offer = offer,
                    baseViewModel = baseViewModel,
                    onUpdateTrigger = updateTrigger,
                    onUpdateOfferItem = onUpdateOfferItem
                )

                Row(
                    modifier = Modifier.clickable {
                        goToOffer(offer.id)
                    }.fillMaxWidth().padding(dimens.extraSmallPadding),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imageSize = 100.dp

                    Column(
                        modifier = Modifier.padding(dimens.smallPadding),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Box(
                            modifier = Modifier.size(imageSize),
                        ) {
                            LoadImage(
                                offer.images.firstOrNull() ?: "empty",
                                modifier = Modifier.size(imageSize)
                            )
                        }

                        SimpleTextButton(
                            text = stringResource(strings.actionsLabel),
                            textStyle = MaterialTheme.typography.labelSmall,
                            textColor = colors.actionTextColor,
                            backgroundColor = colors.grayLayout,
                            leadIcon = {
                                Icon(
                                    painter = painterResource(drawables.shareMenuIcon),
                                    contentDescription = "",
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                    tint = colors.actionTextColor
                                )
                            },
                        ) {
                            baseViewModel.getOfferOperations(offer.id) { listOperations ->
                                menuList.value = buildList {
                                    addAll(listOperations.map { operation ->
                                        MenuItem(
                                            id = operation.id ?: "",
                                            title = operation.name ?: "",
                                            onClick = {
                                                operation.onClickOfferOperationItem(
                                                    offer,
                                                    baseViewModel,
                                                    title,
                                                    fields,
                                                    showDialog,
                                                    onUpdateOfferItem,
                                                )
                                            }
                                        )
                                    })
                                }
                                isOpenPopup.value = true
                            }
                        }

                        PopUpMenu(
                            openPopup = isOpenPopup.value,
                            menuList = menuList.value,
                            onClosed = { isOpenPopup.value = false }
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TitleText(offer.title, color = colors.actionTextColor)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Image(
                                painter = painterResource(drawables.locationIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                            Text(
                                offer.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.black
                            )
                        }

                        val deliveryMethods =
                            offer.deliveryMethods?.joinToString { it.name ?: "" } ?: ""
                        if (deliveryMethods.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            ) {
                                Icon(
                                    painter = painterResource(drawables.trackIcon),
                                    contentDescription = "",
                                    tint = colors.textA0AE,
                                    modifier = Modifier.size(dimens.extraSmallIconSize)
                                )
                                Text(
                                    deliveryMethods,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.black
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Icon(
                                painter = painterResource(drawables.bidsIcon),
                                contentDescription = "",
                                tint = colors.textA0AE,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                            Text(
                                (offer.bids?.size ?: 0).toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.notifyTextColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Image(
                                painter = painterResource(drawables.iconClock),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )
                            var date = d3
                            if (offer.session == null) {
                                date = stringResource(strings.offerSessionCompletedLabel)
                            }
                            Text(
                                date,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.black
                            )
                        }

                        offer.seller.let {
                            UserRow(
                                it,
                                Modifier.clip(MaterialTheme.shapes.small).clickable {
                                    goToUser(it.id)
                                }.padding(dimens.extraSmallPadding),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isActive) {
                        SimpleTextButton(
                            stringResource(strings.orderLabel),
                            backgroundColor = colors.solidGreen,
                            textColor = colors.alwaysWhite,
                        ) {
                            goToMyPurchases()
                        }
                    }


                    if (isActive) {
                        SimpleTextButton(
                            stringResource(strings.writeSellerLabel),
                            leadIcon = {
                                Icon(
                                    painterResource(drawables.mail),
                                    contentDescription = "",
                                    tint = colors.alwaysWhite,
                                    modifier = Modifier.size(dimens.extraSmallIconSize)

                                )
                            },
                            textStyle = MaterialTheme.typography.labelSmall,
                            backgroundColor = colors.steelBlue,
                            textColor = colors.alwaysWhite
                        ) {
                            showMesDialog.value = true
                        }

                        OfferMessagingDialog(
                            showMesDialog.value,
                            offer,
                            onSuccess = { dialogId ->
                                goToDialog(dialogId)
                                showMesDialog.value = false
                            },
                            onDismiss = {
                                showMesDialog.value = false
                            },
                            baseViewModel = baseViewModel
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Text(
                        stringResource(strings.currentPriceParameterName),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textA0AE
                    )

                    Text(
                        offer.price + stringResource(strings.currencySign),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.priceTextColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Text(
                        stringResource(strings.yourBidLabel),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textA0AE
                    )

                    Text(
                        offer.myMaximalBid + stringResource(strings.currencySign),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.positiveGreen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    val title = if (isActive)
                        stringResource(strings.leadingBidsNowLabel)
                    else
                        stringResource(strings.winnerParameterName)


                    val body = buildAnnotatedString {
                        if (offer.buyer?.login == stringResource(strings.yourselfBidsLabel)) {
                            append(UserData.userInfo?.login ?: offer.buyer?.login)
                        } else {
                            offer.bids?.let {
                                if (it.isNotEmpty()) {
                                    append(it[0].obfuscatedMoverLogin)
                                }
                            }
                        }
                    }

                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textA0AE
                    )

                    Text(
                        body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.actionTextColor
                    )
                }

                OfferOperationsDialogs(
                    offer = offer,
                    showDialog = showDialog,
                    viewModel = baseViewModel,
                    title = title,
                    fields = fields,
                    updateItem = {
                        onUpdateOfferItem(it)
                    }
                )
            }
        }
    }
}
