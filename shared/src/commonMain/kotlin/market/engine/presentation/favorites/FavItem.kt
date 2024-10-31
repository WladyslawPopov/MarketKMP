package market.engine.presentation.favorites

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
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.text.style.TextDecoration
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
                onItemClick()
            }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
        ) {
            // State to handle image load failure
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
                            modifier = Modifier.weight(0.8f)
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
                            Image(
                                painter = painterResource(drawables.safeDealIcon),
                                contentDescription = "",
                                modifier = Modifier.size(dimens.smallIconSize)
                            )
                        }
                    }

                    if (offer.discountPercentage > 0 && offer.buyNowPrice?.toDouble() != offer.currentPricePerItem?.toDouble()) {
                        Row(
                            Modifier.padding(dimens.smallPadding),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = offer.buyNowPrice.toString() + " ${stringResource(strings.currencySign)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.brightGreen,
                                textDecoration = TextDecoration.LineThrough
                            )

                            Spacer(modifier = Modifier.width(dimens.extraSmallPadding))

                            if (offer.discountPercentage > 0) {
                                val pd = "-" + offer.discountPercentage.toString() + "%"

                                Card(
                                    colors = CardColors(
                                        containerColor = colors.greenColor,
                                        contentColor = colors.alwaysWhite,
                                        disabledContainerColor = colors.greenColor,
                                        disabledContentColor = colors.alwaysWhite
                                    ),
                                    modifier = Modifier
                                        .padding(dimens.smallPadding)
                                ) {
                                    Text(
                                        text = pd,
                                        modifier = Modifier.padding(5.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.alwaysWhite
                                    )
                                }
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
            modifier = Modifier,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.greenColor,
                uncheckedColor = colors.grayText
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

            IconButton(
                onClick = { onMenuClick() },
            ) {
                Icon(
                    painter = painterResource(drawables.menuIcon),
                    contentDescription = "",
                    tint = colors.steelBlue
                )
            }
        }
    }
}

