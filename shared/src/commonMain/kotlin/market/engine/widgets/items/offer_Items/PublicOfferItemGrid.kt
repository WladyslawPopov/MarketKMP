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
import androidx.compose.runtime.snapshotFlow
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
import market.engine.core.data.states.OfferItemState
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
    state: OfferItemState,
    updateItem : Long?
) {
    val item = state.item
    val onItemClick = state.onItemClick
    val addToFavorites = state.addToFavorites

    LaunchedEffect(updateItem) {
        snapshotFlow {
            updateItem
        }.collect { id ->
            if (id == item.id){
                state.updateItemState()
            }
        }
    }

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
        Column(
            modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(250.dp),
            ) {
                if (item.images.isNotEmpty()) {
                    HorizontalImageViewer(
                        images = item.images,
                        pagerState = pagerState,
                    )
                }else {
                    Image(
                        painter = painterResource(drawables.noImageOffer),
                        contentDescription = null,
                        modifier = Modifier.size(250.dp)
                    )
                }

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
                verticalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        maxLines = 2,
                        minLines = 2,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
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
