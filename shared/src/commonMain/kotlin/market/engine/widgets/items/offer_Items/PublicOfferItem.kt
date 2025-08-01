package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.repositories.OfferBaseViewModel
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.ilustrations.HorizontalImageViewer
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PublicOfferItem(
    offerBaseViewModel: OfferBaseViewModel,
    updateItem : Long?
) {
    val offer by offerBaseViewModel.offerState.collectAsState()
    val events = offerBaseViewModel.events

    LaunchedEffect(updateItem) {
        if (updateItem == offer.id){
            offerBaseViewModel.updateItem()
        }
    }

    val pagerState = rememberPagerState(
        pageCount = { offer.images.size },
    )

    Card(
        colors = if (!offer.isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = MaterialTheme.shapes.small,
        onClick = {
            events.openCabinetOffer(offer)
        }
    ) {
        Row(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            val imageSize =
                if (isBigScreen.value){
                    250.dp
                } else {
                    165.dp
                }
            Box(
                modifier = Modifier.size(imageSize),
            ) {
                HorizontalImageViewer(
                    images = offer.images,
                    pagerState = pagerState,
                )

                if (offer.videoUrls.isNotEmpty()) {
                    SmallImageButton(
                        drawables.iconYouTubeSmall,
                        modifierIconSize = Modifier.size(dimens.mediumIconSize),
                        modifier = Modifier.align(Alignment.TopStart),
                    ){

                    }
                }

                if (offer.discount > 0) {
                    val pd = "-" + offer.discount.toString() + "%"

                    DiscountBadge(pd)
                }
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    verticalAlignment = Alignment.Top
                ) {
                    TitleText(offer.title, modifier = Modifier.weight(1f))
                    SmallIconButton(
                        icon = if (offer.isWatchedByMe) drawables.favoritesIconSelected
                        else drawables.favoritesIcon,
                        color = colors.inactiveBottomNavIconColor,
                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                        modifier = Modifier.align(Alignment.Top).weight(0.2f)
                    ){
                        offerBaseViewModel.addToFavorites()
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(drawables.locationIcon),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.extraSmallIconSize),
                    )
                    Text(
                        text = offer.location,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                if (!offer.isPrototype) {
                    val sessionEnd = stringResource(strings.offerSessionInactiveLabel)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(drawables.iconClock),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.extraSmallIconSize),
                        )

                        Text(
                            text = buildString {
                                if (offer.session != null)
                                    append((offer.session?.end ?: "").convertDateWithMinutes())
                                else
                                    append(sessionEnd)
                            },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                    modifier = Modifier.fillMaxWidth(),
                )  {
                    var typeString = ""
                    var colorType = colors.titleTextColor

                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        when (offer.type) {
                            "buy_now" -> {
                                typeString = stringResource(strings.buyNow)
                                colorType = colors.buyNowColor

                                Image(
                                    painter = painterResource(drawables.iconCountBoxes),
                                    contentDescription = stringResource(strings.numberOfItems),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = offer.currentQuantity.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var buyer = offer.buyer?.login ?: ""
                                var color = colors.grayText

                                if (!offer.isPrototype) {
                                    if (offer.currentQuantity < 2) {
                                        if (offer.buyer?.login != "" && offer.buyer?.login != null) {
                                            buyer = offer.buyer?.login ?: ""
                                            color = colors.ratingBlue
                                        }
                                    }

                                    Text(
                                        text = buyer,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color
                                    )
                                }
                            }

                            "ordinary_auction" -> {
                                typeString = stringResource(strings.ordinaryAuction)

                                Image(
                                    painter = painterResource(drawables.iconGroup),
                                    contentDescription = stringResource(strings.numberOfBids),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = offer.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText

                                if (offer.bids?.isNotEmpty() == true) {
                                    bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }

                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }

                            "auction_with_buy_now" -> {
                                typeString = stringResource(strings.blitzAuction)
                                colorType = colors.auctionWithBuyNow

                                Image(
                                    painter = painterResource(drawables.iconGroup),
                                    contentDescription = stringResource(strings.numberOfBids),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = offer.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText
                                if (offer.bids?.isNotEmpty() == true) {
                                    bids = offer.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }
                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }

                        if (offer.safeDeal) {
                            Image(
                                painter = painterResource(drawables.safeDealIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                    ) {
                        Text(
                            text = offer.seller.login ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.brightBlue,
                        )

                        if ((offer.seller.rating ?: 0) > 0) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        colors.ratingBlue,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(dimens.extraSmallPadding)
                            ) {
                                Text(
                                    text = offer.seller.rating.toString(),
                                    color = colors.alwaysWhite,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        if (offer.seller.isVerified) {
                            Image(
                                painter = painterResource(drawables.verifiedIcon),
                                contentDescription = null,
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                        }
                    }

                    if (offer.note != null) {
                        Row(
                            modifier = Modifier.background(
                                colors.white,
                                MaterialTheme.shapes.small
                            ).padding(dimens.smallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Icon(
                                painterResource(drawables.editNoteIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize),
                                tint = colors.black
                            )

                            Text(
                                text = offer.note ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.black,
                                modifier = Modifier.weight(1f, !isBigScreen.value)
                            )
                        }
                    }

                    Text(
                        text = typeString,
                        style = MaterialTheme.typography.titleSmall,
                        color = colorType,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(offer.price)
                            append(" ${stringResource(strings.currencySign)}")
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.priceTextColor,
                    )
                }
            }
        }
    }
}
