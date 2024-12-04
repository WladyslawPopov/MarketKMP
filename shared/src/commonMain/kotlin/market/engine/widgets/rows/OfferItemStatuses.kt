package market.engine.widgets.rows

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
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
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
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {

        var typeString = ""
        var colorType = colors.titleTextColor

        FlowRow(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth(),
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

                    Text(
                        text = offer.currentQuantity.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = dimens.extraSmallPadding)
                    )

                    if (offer.session != null && !offer.isPrototype) {
                        Text(
                            text = stringResource(strings.noBuyer),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.grayText
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

                    Text(
                        text = offer.numParticipants.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = dimens.smallPadding)
                    )

                    var bids = stringResource(strings.noBids)
                    if (offer.bids?.isNotEmpty() == true) {
                        bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                    }
                    Text(
                        text = bids,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.grayText
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

                    Text(
                        text = offer.numParticipants.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = dimens.smallPadding)
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
            modifier = Modifier.padding(vertical = dimens.extraSmallPadding)
        )
    }
}
