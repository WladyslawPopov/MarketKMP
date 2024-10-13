package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.coil3.CoilImage
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.networkObjects.Offer
import market.engine.core.util.convertDateWithMinutes
import market.engine.core.util.getImage
import market.engine.core.util.printLogD
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GridItemListing(
    offer: Offer,
    onFavouriteClick: suspend (Offer) -> Boolean
) {
    val imageLoadFailed = remember { mutableStateOf(false) }
    val isFavorites = remember { mutableStateOf(offer.isWatchedByMe) }
    val scope = rememberCoroutineScope()

    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        modifier = Modifier
            .clickable {

            }
    ) {
        Column(
            modifier = Modifier
                .padding(dimens.smallPadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUrl = when {
                offer.images?.isNotEmpty() == true -> offer.images.firstOrNull()?.urls?.small?.content
                offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
                else -> null
            }

            Box(
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .align(Alignment.CenterHorizontally)
            ) {
                if (imageLoadFailed.value) {
                    getImage(imageUrl, 200.dp)
                } else {
                    CoilImage(
                        modifier = Modifier.size(200.dp),
                        imageModel = {
                            imageUrl
                        },
                        previewPlaceholder = painterResource(drawables.noImageOffer),
                        failure = { e ->
                            imageLoadFailed.value = true
                            printLogD("Coil", e.reason?.message)
                        }
                    )
                }

                if (offer.videoUrls?.isNotEmpty() == true) {
                    IconButton(
                        modifier = Modifier.align(Alignment.TopStart),
                        onClick = {

                        },
                    ) {
                        Image(
                            painter = painterResource(drawables.iconYouTubeSmall),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.mediumIconSize)
                        )
                    }
                }

                if (offer.discountPercentage > 0) {
                    val pd = "-" + offer.discountPercentage.toString() + "%"

                    Card(
                        modifier = Modifier
                            .background(colors.greenColor)
                            .padding(dimens.smallPadding)
                            .align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = pd,
                            modifier = Modifier.padding(5.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.white
                        )
                    }
                }
            }

            Text(
                text = offer.title ?: "",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    scope.launch {
                        isFavorites.value = onFavouriteClick(offer)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorites.value) drawables.favoritesIconSelected
                        else drawables.favoritesIcon
                    ),
                    contentDescription = "",
                    modifier = Modifier.size(dimens.smallIconSize),
                    tint = colors.inactiveBottomNavIconColor
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
                val sessionEnd = offer.session?.end?.convertDateWithMinutes() ?: ""
                Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier.fillMaxWidth()) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (offer.saleType == "buy_now") {
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

                    Text(
                        text = stringResource(strings.buyNow),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.titleTextColor,
                        modifier = Modifier.padding(horizontal = dimens.extraSmallPadding)
                    )

                } else {
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
                if (offer.safeDeal) {
                    Icon(
                        painter = painterResource(drawables.safeDealIcon),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.smallIconSize),
                        tint = colors.brightBlue
                    )
                }
            }


            if (offer.discountPercentage > 0 && offer.buyNowPrice?.toDouble() != offer.currentPricePerItem?.toDouble()) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = offer.buyNowPrice.toString() + " ${stringResource(strings.currencySign)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.brightGreen,
                        textDecoration = TextDecoration.LineThrough
                    )
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


            Spacer(modifier = Modifier.height(dimens.mediumSpacer))

            val priceText = buildAnnotatedString {
                append(offer.currentPricePerItem ?: "")
                append(" ${stringResource(strings.currencySign)}")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.titleTextColor,
                )
            }
        }
    }
}
