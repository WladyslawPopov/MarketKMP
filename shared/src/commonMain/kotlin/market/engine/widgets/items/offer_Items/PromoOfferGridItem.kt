package market.engine.widgets.items.offer_Items

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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.widgets.badges.DiscountBadge
import market.engine.widgets.buttons.SmallImageButton
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun PromoOfferGridItem(
    offer: OfferItem,
    onOfferClick: (Long) -> Unit
) {
    Card(
        colors = colors.cardColors,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        onClick = { onOfferClick(offer.id) }
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
                    url = offer.images.firstOrNull() ?: "empty",
                    modifier = Modifier.size(200.dp)
                )

                if (offer.videoUrls?.isNotEmpty() == true) {
                    SmallImageButton(
                        drawables.iconYouTubeSmall,
                        modifierIconSize = Modifier.size(dimens.mediumIconSize),
                        modifier = Modifier.align(Alignment.TopStart),
                    ){

                    }
                }

                if (offer.discount > 0) {
                    val pd = "-" + offer.discount.toString() + "%"
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DiscountBadge(pd)
                    }
                }
            }

            Text(
                offer.title,
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                color = colors.black,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(dimens.smallSpacer))

            Text(
                text = offer.price + stringResource(strings.currencySign),
                color = colors.priceTextColor,
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
