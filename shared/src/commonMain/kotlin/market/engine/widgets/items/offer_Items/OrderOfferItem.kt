package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderOfferItem(
    offer: OfferItem?,
    selectedQuantity: Int?,
    goToOffer: (Long) -> Unit,
    addToFavorites: () -> Unit,
) {
    if (offer == null) return

    Card(
        colors = colors.cardColors,
        modifier = Modifier.padding(dimens.smallPadding),
        shape = MaterialTheme.shapes.small,
        onClick = remember {{
            goToOffer(offer.id)
        }}
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
           LoadImage(
               url = offer.images.firstOrNull() ?: "",
               modifier = Modifier.size(120.dp)
           )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleText(
                        text = offer.title,
                        modifier = Modifier.weight(1f),
                        color = colors.actionTextColor
                    )

                    SmallIconButton(
                        icon = if (offer.isWatchedByMe) drawables.favoritesIconSelected
                        else drawables.favoritesIcon,
                        color = colors.inactiveBottomNavIconColor,
                        modifierIconSize = Modifier.size(dimens.smallIconSize),
                        modifier = Modifier.align(Alignment.Top)
                    ){
                       addToFavorites()
                    }
                }

                if (offer.location.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(drawables.locationIcon),
                            contentDescription = "",
                            modifier = Modifier.size(dimens.smallIconSize),
                        )
                        Text(
                            text = offer.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.black
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(drawables.iconCountBoxes),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.smallIconSize),
                    )
                    Text(
                        text = (selectedQuantity ?: (stringResource(strings.inStockLabel) + " " + offer.currentQuantity)).toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.black
                    )
                }

                val priceText =
                    buildAnnotatedString {
                        append(offer.price)
                        append(" ${stringResource(strings.currencySign)}")
                    }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(strings.priceOfOneOfferLabel),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.black,
                    )
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.black,
                    )
                }
            }
        }
    }
}
