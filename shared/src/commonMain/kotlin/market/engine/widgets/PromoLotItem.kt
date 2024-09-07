package market.engine.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.market.auction_mobile.business.networkObjects.Offer
import business.util.printLogD
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.setSingletonImageLoaderFactory
import market.engine.business.constants.ThemeResources.colors
import market.engine.root.getAsyncImageLoader


@OptIn(ExperimentalCoilApi::class)
@Composable
fun PromoLotItem(offer: Offer, onOfferClick: (Offer) -> Unit) {

    Card(
        shape = RoundedCornerShape(8.dp),
        onClick = { onOfferClick(offer) }
    ) {

        Column(
            modifier = Modifier.padding(8.dp)
        ) {

            AsyncImage(
                model = offer.images?.firstOrNull()?.urls?.big?.content,
                contentDescription = null,
                modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally),
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = offer.currentPricePerItem.toString(),
                color = colors.titleTextColor,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
