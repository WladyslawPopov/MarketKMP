package market.engine.fragments.root.main.profile.myBids

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.utils.convertDateWithMinutes
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.getOfferImagePreview
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.dialogs.CreateOfferDialog
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.bars.HeaderOfferBar
import market.engine.widgets.rows.UserRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BidsItem(
    offer: Offer,
    onUpdateOfferItem : (offer: Offer) -> Unit,
    baseViewModel: BaseViewModel,
    updateTrigger : Int,
    goToUser: (Long) -> Unit,
    goToOffer: (Long) -> Unit,
    goToMyPurchases: () -> Unit,
    goToDialog: (Long?) -> Unit
) {
    if(updateTrigger < 0) return

    val showMesDialog = remember { mutableStateOf(false) }

    val currentDate = getCurrentDate().toLongOrNull() ?: 1L
    val isActive = ((offer.session?.end?.toLongOrNull() ?: 1L) > currentDate)

    val date1 = offer.session?.start?.convertDateWithMinutes()
    val date2 = offer.session?.end?.convertDateWithMinutes()
    val d3 = "$date1 – $date2"

    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            HeaderOfferBar(
                offer = offer,
                onUpdateOfferItem = {
                    onUpdateOfferItem(it)
                },
                goToCreateOffer = {

                },
                baseViewModel = baseViewModel,
                onUpdateTrigger = updateTrigger
            )


            offer.sellerData?.let {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        stringResource(strings.sellerLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.black
                    )

                    UserRow(
                        it,
                        Modifier.clickable {
                            goToUser(it.id)
                        }.fillMaxWidth(),
                    )
                }
            }

            Row(
                modifier = Modifier.clickable {
                    goToOffer(offer.id)
                }.fillMaxWidth().padding(dimens.smallPadding),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(dimens.smallPadding)
                        .wrapContentSize(),
                    contentAlignment = Alignment.TopStart
                ) {
                    LoadImage(
                        url = offer.getOfferImagePreview(),
                        size = 90.dp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = offer.title ?: "",
                            color = colors.actionTextColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    val locationText = buildString {
                        offer.freeLocation?.let { append(it) }
                        offer.region?.name?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }
                    if (locationText.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Image(
                                painter = painterResource(drawables.locationIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                            Text(
                                locationText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.black
                            )
                        }
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
                                modifier = Modifier.size(dimens.smallIconSize)
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
                            modifier = Modifier.size(dimens.smallIconSize)
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
                            modifier = Modifier.size(dimens.smallIconSize)
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
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.smallPadding),
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

                    CreateOfferDialog(
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
                    offer.currentPricePerItem + stringResource(strings.currencySign),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.titleTextColor
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
                    if (offer.buyerData?.login == stringResource(strings.yourselfBidsLabel)) {
                        append(UserData.userInfo?.login ?: offer.buyerData?.login)
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
        }
    }
}
