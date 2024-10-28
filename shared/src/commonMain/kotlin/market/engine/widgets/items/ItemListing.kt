package market.engine.widgets.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.network.networkObjects.Offer

@Composable
fun ItemListing(
    offer: Offer,
    isGrid : Boolean,
    onFavouriteClick: suspend (Offer) -> Boolean
) {
    var isPromo = false

    if (offer.promoOptions != null) {
        val isBackLight = offer.promoOptions.find { it.id == "backlignt_in_listing" }
        if (isBackLight != null) {
            isPromo = true
        }
    }

    Card(
        colors = if (!isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        modifier = Modifier
            .clickable {

            }
    ) {
        if (isGrid){
            Column(
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ListingContent(
                    offer,
                    modifier = Modifier.align(Alignment.End),
                    isGrid = isGrid,
                    onFavouriteClick
                )
            }
        }else{
            Row(
                modifier = Modifier.padding(dimens.smallPadding).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                ListingContent(
                    offer,
                    modifier = Modifier.align(Alignment.Bottom),
                    isGrid = false,
                    onFavouriteClick
                )
            }
        }
    }
}
