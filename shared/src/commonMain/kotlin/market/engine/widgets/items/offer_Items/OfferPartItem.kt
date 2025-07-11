package market.engine.widgets.items.offer_Items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.OfferItem
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferPartItem(
    offer: OfferItem,
    modifier: Modifier = Modifier,
    goToOffer: (Long) -> Unit,
) {
    Card(
        modifier = modifier,
        colors = colors.cardColors,
        onClick = {
            goToOffer(offer.id)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Box(
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .wrapContentSize(),
                contentAlignment = Alignment.TopStart
            ) {
                LoadImage(
                    url = offer.images.firstOrNull() ?: "",
                    modifier = Modifier.size(60.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = offer.title,
                        color = colors.actionTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimens.extraSmallPadding)
                ) {
                    Image(
                        painter = painterResource(drawables.iconCountBoxes),
                        contentDescription = "",
                        modifier = Modifier.size(dimens.smallIconSize),
                    )

                    Spacer(modifier = Modifier.width(dimens.smallSpacer))

                    val builder = buildAnnotatedString {
                        append(offer.quantity.toString())
                        append("     ")
                        withStyle(SpanStyle(
                            color = colors.grayText,
                            fontWeight = FontWeight.Bold
                        )){
                            append(stringResource(strings.costLabel) + "   ")
                        }

                        append(offer.price + " ${stringResource(strings.currencySign)}")
                    }

                    Text(
                        text = builder,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(dimens.smallPadding),
                        color = colors.black
                    )
                }
            }
        }
    }
}
