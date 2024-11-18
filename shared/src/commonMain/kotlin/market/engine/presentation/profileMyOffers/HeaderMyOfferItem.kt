package market.engine.presentation.profileMyOffers


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun HeaderMyOfferItem(
    offer: Offer,
    onMenuClick: () -> Unit,
) {
    var typeString = ""
    var colorType = colors.titleTextColor

    Row(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (offer.saleType) {
            "buy_now" -> {
                typeString = stringResource(strings.buyNow)
            }

            "ordinary_auction" -> {
                typeString = stringResource(strings.ordinaryAuction)
            }

            "auction_with_buy_now" -> {
                typeString = stringResource(strings.blitzAuction)
                colorType = colors.auctionWithBuyNow
            }
        }

        TitleText(
            text = typeString,
            color = colorType
        )

        Row(
            modifier = Modifier.wrapContentSize().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorites Icon and Count
            Icon(
                painter = painterResource(drawables.favoritesIcon),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            Text(
                text = offer.watchersCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.width(dimens.smallPadding))

            // Views Icon and Count
            Icon(
                painter = painterResource(drawables.eyeOpen),
                contentDescription = "",
                modifier = Modifier.size(dimens.smallIconSize),
                tint = colors.textA0AE
            )
            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
            Text(
                text = offer.viewsCount.toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.width(dimens.mediumPadding))

            IconButton(
                onClick = {
                    onMenuClick()
                },
            ) {
                Icon(
                    painter = painterResource(drawables.menuIcon),
                    contentDescription = "",
                    tint = colors.black
                )
            }
        }
    }
}
