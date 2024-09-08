package market.engine.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.market.auction_mobile.business.networkObjects.Offer
import business.util.printLogD
import coil3.compose.AsyncImage
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource


@Composable
fun PromoLotItem(offer: Offer, onOfferClick: (Offer) -> Unit) {
    Card(
        colors = CardColors(
            containerColor = colors.white,
            contentColor = colors.black,
            disabledContainerColor = colors.lightGray,
            disabledContentColor = colors.grayText
        ),
        shape = RoundedCornerShape(8.dp),
        onClick = { onOfferClick(offer) }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = offer.images?.firstOrNull()?.urls?.big?.content,
                contentDescription = null,
                modifier = Modifier.sizeIn(120.dp, 90.dp, 300.dp, 250.dp).align(Alignment.CenterHorizontally),
                onSuccess = {
                    printLogD("AsyncImage", "Image loaded successfully")
                },
                onError = { e ->
                    printLogD("AsyncImage", "Error: ${e.result.throwable.message}")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = offer.title ?: "",
                color = colors.black,
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                letterSpacing = 0.1.sp,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
            Text(
                text = offer.currentPricePerItem.toString() + stringResource(strings.currencySign),
                color = colors.titleTextColor,
                modifier = Modifier.align(Alignment.End).padding(dimens.smallPadding),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
