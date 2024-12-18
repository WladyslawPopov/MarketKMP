package market.engine.widgets.bars

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfferItemStatuses(
    offer: Offer
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {

        var typeString = ""
        var colorType = colors.titleTextColor

        FlowRow(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        ) {
            when (offer.saleType) {
                "buy_now" -> {
                    typeString = stringResource(strings.buyNow)
                    colorType = colors.buyNowColor

                    Image(
                        painter = painterResource(drawables.iconCountBoxes),
                        contentDescription = stringResource(strings.numberOfItems),
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    Text(
                        text = offer.currentQuantity.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))
                    var buyer = offer.buyerData?.login ?: stringResource(strings.noBuyer)
                    var color = colors.grayText
                    if (!offer.isPrototype) {
                        if (offer.currentQuantity < 2) {
                            if (offer.buyerData?.login != "" && offer.buyerData?.login != null) {
                                buyer = offer.buyerData.login
                                color = colors.ratingBlue
                            }
                        }
                        Text(
                            text = buyer,
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }

                "ordinary_auction" -> {
                    typeString = stringResource(strings.ordinaryAuction)

                    Image(
                        painter = painterResource(drawables.iconGroup),
                        contentDescription = stringResource(strings.numberOfBids),
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    Text(
                        text = offer.numParticipants.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    var bids = stringResource(strings.noBids)
                    var color = colors.grayText
                    if (offer.bids?.isNotEmpty() == true) {
                        bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                        color = colors.ratingBlue
                    }
                    Text(
                        text = bids,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }

                "auction_with_buy_now" -> {
                    typeString = stringResource(strings.blitzAuction)
                    colorType = colors.auctionWithBuyNow

                    Image(
                        painter = painterResource(drawables.iconGroup),
                        contentDescription = stringResource(strings.numberOfBids),
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    Text(
                        text = offer.numParticipants.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    var bids = stringResource(strings.noBids)
                    var color = colors.grayText
                    if (offer.bids?.isNotEmpty() == true) {
                        bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                        color = colors.ratingBlue
                    }
                    Text(
                        text = bids,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.width(dimens.smallSpacer))

            if (offer.safeDeal) {
                Image(
                    painter = painterResource(drawables.safeDealIcon),
                    contentDescription = "",
                    modifier = Modifier.size(dimens.smallIconSize)
                )
            }
        }

        Text(
            text = typeString,
            style = MaterialTheme.typography.titleSmall,
            color = colorType,
        )
    }
}
