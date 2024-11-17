package market.engine.presentation.profileMyOffers

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.core.util.convertDateWithMinutes
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.texts.DiscountText
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyOffersItem(
    offer: Offer,
    onMenuClick: () -> Unit,
    onItemClick: () -> Unit
) {
    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        modifier = Modifier
            .clickable {
                onItemClick()
            }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
        ) {
            // Determine the image URL
            val imageUrl = when {
                offer.images?.isNotEmpty() == true -> offer.images.firstOrNull()?.urls?.small?.content
                offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
                else -> null
            }

            HeaderMyOfferItem(
                offer = offer,
                onMenuClick = onMenuClick,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .padding(dimens.smallPadding),
                    contentAlignment = Alignment.TopStart
                ) {
                    LoadImage(
                        url = imageUrl ?: "",
                        size = 160.dp
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(dimens.smallPadding)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = offer.title ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    val location = buildString {
                        offer.freeLocation?.let { append(it) }
                        offer.region?.name?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }

                    if (location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(drawables.locationIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize),
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )
                        }
                    }

                    if (!offer.isPrototype) {
                        var sessionEnd = stringResource(strings.inactiveOffer)
                        if (offer.session != null) {
                            sessionEnd = offer.session.end?.convertDateWithMinutes() ?: ""
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(drawables.iconClock),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize),
                            )
                            Text(
                                text = sessionEnd,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(dimens.smallPadding)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (offer.saleType) {
                                "buy_now" -> {
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
                                    var myLotWinner = stringResource(strings.noBuyer)
                                    var color = colors.grayText
                                    if (offer.session != null && !offer.isPrototype) {
                                        if (offer.currentQuantity < 2) {
                                            if (offer.buyerData?.login != "" && offer.buyerData?.login != null) {
                                                myLotWinner = offer.buyerData.login
                                                color = colors.ratingBlue
                                            }
                                        }

                                        Text(
                                            text = myLotWinner,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = color
                                        )
                                    }
                                }

                                "auction_with_buy_now" , "ordinary_auction" -> {
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

                            if (offer.safeDeal) {
                                Image(
                                    painter = painterResource(drawables.safeDealIcon),
                                    contentDescription = "",
                                    modifier = Modifier.size(dimens.smallIconSize).padding(
                                        dimens.smallPadding),
                                )
                            }
                        }
                    }


                    if (offer.discountPercentage > 0 && offer.buyNowPrice?.toDouble() != offer.currentPricePerItem?.toDouble()) {
                        Row(
                            Modifier.padding(dimens.smallPadding),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DiscountText(offer.buyNowPrice.toString())

                            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))

                            if (offer.discountPercentage > 0) {
                                val pd = "-" + offer.discountPercentage.toString() + "%"
                                DiscountBadge(pd)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimens.mediumSpacer))

                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

//                        offer.promoOptions?.forEach { o ->
//
//                            when (o.id) {
//                                "featured_in_listing" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = colors.brightPurple,
                                            contentColor = colors.white
                                        )
                                    ) {
                                        TitleText(text = "TOP", color = colors.alwaysWhite)
                                    }
                                    Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
//                                }

//                                "featured_on_main_page" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = colors.brightPurple,
                                            contentColor = colors.white
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(drawables.homeIcon),
                                            contentDescription = "",
                                            tint = colors.alwaysWhite
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
//                                }

//                                "recommended_in_listing" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = colors.brightPurple,
                                            contentColor = colors.white
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(drawables.megaphoneIcon),
                                            contentDescription = "",
                                            tint = colors.alwaysWhite
                                        )
                                    }
                                     Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
//                                }

//                                "backlignt_in_listing" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = colors.brightPurple,
                                            contentColor = colors.white
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(drawables.promoHighlightIcon),
                                            contentDescription = "",
                                            tint = colors.alwaysWhite
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(dimens.extraSmallPadding))
//                                }

//                                "featured_in_offer" -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = colors.brightPurple,
                                            contentColor = colors.white
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(drawables.adIcon),
                                            contentDescription = "",
                                            tint = colors.alwaysWhite
                                        )
                                    }
//                                }
//                            }
//                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding)
                    ) {

                        Text(
                            text = "${offer.currentPricePerItem} ${stringResource(strings.currencySign)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.titleTextColor,
                            )
                        )
                    }
                }
            }

            if (offer.relistingMode != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        painter = painterResource(drawables.recycleIcon),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.smallIconSize),
                        tint = colors.inactiveBottomNavIconColor
                    )

                    Text(
                        offer.relistingMode.name ?: ""
                        ,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
