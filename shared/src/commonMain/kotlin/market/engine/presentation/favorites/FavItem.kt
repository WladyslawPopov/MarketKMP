package market.engine.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.coil3.CoilImage
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.core.util.convertDateWithMinutes
import market.engine.core.util.getImage
import market.engine.core.util.printLogD
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.texts.DiscountText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavItem(
    offer: Offer,
    onMenuClick: () -> Unit,
    onSelectionChange: (Boolean) -> Unit,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {
    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        modifier = Modifier
            .clickable {
                if (!isSelected) {
                    onItemClick()
                }else{
                    onSelectionChange(false)
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
        ) {
            // State to handle image load by Coil failure
            var imageLoadFailed by remember { mutableStateOf(false) }

            // Determine the image URL
            val imageUrl = when {
                offer.images?.isNotEmpty() == true -> offer.images.firstOrNull()?.urls?.small?.content
                offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
                else -> null
            }

            HeaderSection(
                offer = offer,
                onMenuClick = onMenuClick,
                onSelectionChange = onSelectionChange,
                isSelected = isSelected
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
                    if (imageLoadFailed) {
                        getImage(imageUrl, 160.dp)
                    } else {
                        CoilImage(
                            modifier = Modifier.size(160.dp),
                            imageModel = {
                                imageUrl
                            },
                            previewPlaceholder = painterResource(drawables.noImageOffer),
                            failure = { e ->
                                imageLoadFailed = true
                                printLogD("Coil", e.reason?.message)
                            }
                        )
                    }
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

                        var typeString = ""
                        var colorType = colors.titleTextColor
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                                    colorType = colors.brightPurple

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
                            }

                            if (offer.safeDeal) {
                                Image(
                                    painter = painterResource(drawables.safeDealIcon),
                                    contentDescription = "",
                                    modifier = Modifier.size(dimens.smallIconSize).padding(dimens.smallPadding),
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
                        if (offer.sellerData?.isVerified == true) {
                            Image(
                                painter = painterResource(drawables.verifySellersIcon),
                                contentDescription = "",
                            )
                        }

                        Text(
                            text = (offer.sellerData?.login + " (${offer.sellerData?.rating})"),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(dimens.smallPadding),
                            color = colors.actionTextColor
                        )
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
        }
    }
}

@Composable
fun HeaderSection(
    offer: Offer,
    isSelected: Boolean,
    onMenuClick: () -> Unit,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelectionChange(it) },
            modifier = Modifier.size(38.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = colors.inactiveBottomNavIconColor,
                uncheckedColor = colors.textA0AE,
                checkmarkColor = colors.alwaysWhite
            )
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

            AnimatedVisibility (!isSelected) {
                IconButton(
                    onClick = { onMenuClick() },
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
}

