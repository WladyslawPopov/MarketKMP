package market.engine.fragments.root.main.profile.myBids

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseViewModel
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.rows.HeaderOfferItem
import market.engine.widgets.rows.UserSimpleRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BidsItem(
    offer: Offer,
    onUpdateOfferItem : (offer: Offer) -> Unit,
    baseViewModel: BaseViewModel,
    updateTrigger : Int,
    goToOffer: (Long) -> Unit,
    goToMyPurchases: () -> Unit
) {
    val currentDate = getCurrentDate().toLongOrNull() ?: 1L
    val isActive = ((offer.session?.end?.toLongOrNull() ?: 1L) > currentDate)

    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            horizontalAlignment = Alignment.Start
        ) {
            HeaderOfferItem(
                offer = offer,
                onUpdateOfferItem = onUpdateOfferItem,
                goToCreateOffer = {

                },
                baseViewModel = baseViewModel
            )

            val imageUrl = when {
                offer.image != null -> offer.image.small?.content
                offer.images?.isNotEmpty() == true -> offer.images?.firstOrNull()?.urls?.small?.content
                offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
                offer.externalUrl != null -> offer.externalUrl
                else -> null
            }

            Row(
                modifier = Modifier.clickable {
                    goToOffer(offer.id)
                }.fillMaxWidth(),
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
                        url = imageUrl ?: "",
                        size = 60.dp
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(drawables.iconCountBoxes),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.smallIconSize),
                        )

                        Spacer(modifier = Modifier.width(dimens.mediumSpacer))

                        Text(
                            text = offer.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(dimens.smallPadding),
                            color = colors.black
                        )
                    }

                    offer.sellerData?.let { UserSimpleRow(it, Modifier.fillMaxWidth()) }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimens.smallPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isActive) {
                    SimpleTextButton(
                        stringResource(strings.orderLabel),
                        backgroundColor = colors.solidGreen,
                        textColor = colors.white,
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

                    }
                }
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
                    Icon(
                        painter = painterResource(drawables.locationIcon),
                        contentDescription = "",
                        tint = colors.textA0AE,
                        modifier = Modifier.size(dimens.smallIconSize)
                    )
                    Text(
                        locationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black
                    )
                }
            }

            val deliveryMethods = offer.deliveryMethods?.joinToString { it.name ?: "" } ?: ""
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

        }
    }
}
