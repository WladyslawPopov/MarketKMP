package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun ColumnItemListing(
    offer: Offer,
    onFavouriteClick: suspend (Offer) -> Boolean
) {
    val imageLoadFailed = remember { mutableStateOf(false) }
    val isFavorites = remember { mutableStateOf(offer.isWatchedByMe) }
    val scope = rememberCoroutineScope()

    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        onClick = {

        }
    ){
        Row(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {

            val imageUrl = when {
                offer.images?.isNotEmpty() == true -> offer.images.firstOrNull()?.urls?.small?.content
                offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
                else -> null
            }

            Box(
                modifier = Modifier.padding(dimens.smallPadding).align(Alignment.Top).wrapContentSize()
            ) {

                if (imageLoadFailed.value) {
                    getImage(imageUrl, 128.dp)
                } else {
                    CoilImage(
                        modifier = Modifier.size(128.dp),
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
                        modifier = Modifier.background(colors.greenColor)
                            .padding(dimens.smallPadding).align(Alignment.BottomEnd)
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

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = offer.title ?: "",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                    )

                    IconButton(
                        onClick = {
                            scope.launch {
                                isFavorites.value = onFavouriteClick(offer)
                            }
                        },
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
                }

                val location = buildString {
                    offer.freeLocation?.let { append(it) }
                    offer.region?.name?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                }

                if (location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.height(dimens.smallPadding))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (offer.saleType == "buy_now") {

                        Image(
                            painter = painterResource(drawables.iconCountBoxes),
                            contentDescription = stringResource(strings.numberOfItems),
                            modifier = Modifier.size(dimens.smallIconSize),
                        )

                        Text(
                            text = offer.currentQuantity.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(5.dp)
                        )

                        Text(
                            text = stringResource(strings.buyNow),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(5.dp),
                            color = colors.titleTextColor
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
                            modifier = Modifier.padding(5.dp)
                        )

                    }

                    if (offer.safeDeal){
                        Image(
                            painter = painterResource(drawables.safeDealIcon),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.smallIconSize)
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
}
