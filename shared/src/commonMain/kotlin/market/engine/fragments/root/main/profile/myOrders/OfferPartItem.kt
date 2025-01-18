package market.engine.fragments.root.main.profile.myOrders

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Text
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
import market.engine.widgets.exceptions.LoadImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfferPartItem(
    offer: Offer,
    goToOffer: (Long) -> Unit,
) {
    val imageUrl = when {
        offer.image != null -> offer.image.small?.content
        offer.images?.isNotEmpty() == true -> offer.images?.firstOrNull()?.urls?.small?.content
        offer.externalImages?.isNotEmpty() == true -> offer.externalImages.firstOrNull()
        offer.externalUrl != null -> offer.externalUrl
        else -> null
    }

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
                    url = imageUrl ?: "",
                    size = 60.dp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = offer.title ?: "",
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

                        append(offer.pricePerItem + " ${stringResource(strings.currencySign)}")
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
