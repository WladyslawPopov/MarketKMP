package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.network.networkObjects.Offer
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.stringResource

@Composable
fun PromoOfferGridItem(offer: Offer, onOfferClick: (Offer) -> Unit) {

    val images = when {
        offer.images?.isNotEmpty() == true -> offer.images.firstOrNull()?.urls?.small?.content ?: ""
        offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull() ?: ""
        else -> ""
    }

    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        onClick = { onOfferClick(offer) }
    ) {
        Column(
            modifier = Modifier.padding(dimens.smallPadding).widthIn(max = 420.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(dimens.smallPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LoadImage(
                    url = images,
                    size = 200.dp
                )

                if (offer.videoUrls?.isNotEmpty() == true) {
                    SmallImageButton(
                        drawables.iconYouTubeSmall,
                        modifierIconSize = Modifier.size(dimens.mediumIconSize),
                        modifier = Modifier.align(Alignment.TopStart),
                    ){

                    }
                }

                if (offer.discountPercentage > 0) {
                    val pd = "-" + offer.discountPercentage.toString() + "%"
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DiscountBadge(pd)
                    }
                }
            }

            TitleText(
                offer.title ?: "",
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimens.smallSpacer))

            Text(
                text = offer.currentPricePerItem.toString() + stringResource(strings.currencySign),
                color = colors.titleTextColor,
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
