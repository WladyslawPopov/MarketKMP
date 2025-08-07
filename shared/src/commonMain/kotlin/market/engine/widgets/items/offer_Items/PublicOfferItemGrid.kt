package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.repositories.PublicOfferRepository
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.ilustrations.HorizontalImageViewer
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PublicOfferItemGrid(
    offerRepository: PublicOfferRepository,
    updateItem : Long?
) {
    val offer by offerRepository.offerState.collectAsState()

    LaunchedEffect(updateItem) {
        if (updateItem == offer.id){
            offerRepository.updateItem()
        }
    }

    val pagerState = rememberPagerState(
        pageCount = { offer.images.size },
    )

    Card(
        colors = if (!offer.isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = MaterialTheme.shapes.small,
        onClick = {
            offerRepository.goToOffer(offer)
        }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(250.dp),
            ) {
                if (offer.images.isNotEmpty()) {
                    HorizontalImageViewer(
                        images = offer.images,
                        pagerState = pagerState,
                    )
                }else {
                    Image(
                        painter = painterResource(drawables.noImageOffer),
                        contentDescription = null,
                        modifier = Modifier.size(250.dp)
                    )
                }

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
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        offerRepository.clickToFavorite()
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
                        maxLines = 2,
                        minLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
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
