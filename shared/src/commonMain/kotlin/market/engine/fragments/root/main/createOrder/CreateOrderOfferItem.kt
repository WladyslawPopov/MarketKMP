package market.engine.fragments.root.main.createOrder

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
import market.engine.core.network.networkObjects.Offer
import market.engine.core.utils.getOfferImagePreview
import market.engine.widgets.ilustrations.LoadImage
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateOrderOfferItem(
    offer: Offer?,
    selectedQuantity: Int,
    goToOffer: (Long) -> Unit,
) {
    if (offer == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.clickable {
                goToOffer(offer.id)
            }.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .wrapContentSize(),
                contentAlignment = Alignment.TopStart
            ) {
                LoadImage(
                    url = offer.getOfferImagePreview(),
                    size = 90.dp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleText(
                        text = offer.title ?: "",
                        color = colors.actionTextColor
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

                    Spacer(modifier = Modifier.width(dimens.mediumSpacer))

                    val builder = buildAnnotatedString {
                        withStyle(SpanStyle(
                            color = colors.grayText,
                            fontWeight = FontWeight.Bold
                        )){
                            append(selectedQuantity.toString())
                            append("     ")
                        }

                        append(offer.currentPricePerItem + " ${stringResource(strings.currencySign)}")
                    }

                    Text(
                        text = builder,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(dimens.smallPadding),
                        color = colors.black
                    )
                }

                Spacer(modifier = Modifier.height(dimens.smallSpacer))

                val priceText = buildAnnotatedString {
                    append(((offer.currentPricePerItem?.toDoubleOrNull() ?: 0.0) * selectedQuantity).toString())
                    append(" ${stringResource(strings.currencySign)}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(strings.totalLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.black,
                    )
                    Spacer(modifier = Modifier.width(dimens.smallSpacer))
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.black,
                    )
                }
            }
        }
    }
}
