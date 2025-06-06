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
import market.engine.core.data.items.OfferItem
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
    item : OfferItem,
    onItemClick: () -> Unit = {},
    addToFavorites : (OfferItem) -> Unit = {},
    updateTrigger : Int = 0,
) {
    if (updateTrigger < 0) return

    val pagerState = rememberPagerState(
        pageCount = { item.images.size },
    )

    Card(
        colors = if (!item.isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = MaterialTheme.shapes.small,
        onClick = {
            onItemClick()
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
                    images = item.images,
                    pagerState = pagerState,
                )

                if (item.videoUrls?.isNotEmpty() == true) {
                    SmallImageButton(
                        drawables.iconYouTubeSmall,
                        modifierIconSize = Modifier.size(dimens.mediumIconSize),
                        modifier = Modifier.align(Alignment.TopStart),
                    ){

                    }
                }

                if (item.discount > 0) {
                    val pd = "-" + item.discount.toString() + "%"

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
                    TitleText(item.title, modifier = Modifier.weight(1f))
                    SmallIconButton(
                        icon = if (item.isWatchedByMe) drawables.favoritesIconSelected
                        else drawables.favoritesIcon,
                        color = colors.inactiveBottomNavIconColor,
                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                        modifier = Modifier.align(Alignment.Top).weight(0.2f)
                    ){
                        addToFavorites(item)
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
                        text = item.location,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                if (!item.isPrototype) {
                    var sessionEnd = stringResource(strings.offerSessionInactiveLabel)

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
                                if (item.session != null)
                                    append((item.session?.end ?: "").convertDateWithMinutes())
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
                        when (item.type) {
                            "buy_now" -> {
                                typeString = stringResource(strings.buyNow)
                                colorType = colors.buyNowColor

                                Image(
                                    painter = painterResource(drawables.iconCountBoxes),
                                    contentDescription = stringResource(strings.numberOfItems),
                                    modifier = Modifier.size(dimens.extraSmallIconSize),
                                )

                                Text(
                                    text = item.quantity.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var buyer = item.buyer?.login ?: ""
                                var color = colors.grayText

                                if (!item.isPrototype) {
                                    if (item.quantity < 2) {
                                        if (item.buyer?.login != "" && item.buyer?.login != null) {
                                            buyer = item.buyer?.login ?: ""
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
                                    text = item.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText

                                if (item.bids?.isNotEmpty() == true) {
                                    bids = item.bids?.get(0)?.obfuscatedMoverLogin ?: ""
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
                                    text = item.numParticipants.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                var bids = stringResource(strings.noBids)
                                var color = colors.grayText
                                if (item.bids?.isNotEmpty() == true) {
                                    bids = item.bids?.get(0)?.obfuscatedMoverLogin ?: ""
                                    color = colors.ratingBlue
                                }
                                Text(
                                    text = bids,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }

                        if (item.safeDeal) {
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
                            text = item.seller.login ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.brightBlue,
                        )

                        if ((item.seller.rating ?: 0) > 0) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        colors.ratingBlue,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(dimens.extraSmallPadding)
                            ) {
                                Text(
                                    text = item.seller.rating.toString(),
                                    color = colors.alwaysWhite,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        if (item.seller.isVerified) {
                            Image(
                                painter = painterResource(drawables.verifiedIcon),
                                contentDescription = null,
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                        }
                    }

                    if (item.note != null) {
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
                                text = item.note ?: "",
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
                            append(item.price)
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
