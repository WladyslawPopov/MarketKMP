package market.engine.presentation.listing

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
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.network.networkObjects.Offer

@Composable
fun ListingItem(
    offer: Offer,
    isGrid : Boolean,
    onFavouriteClick: suspend (Offer) -> Boolean,
    onItemClick: () -> Unit = {}
) {
    var isPromo = false

    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    if (offer.promoOptions != null) {
        val isBackLight = offer.promoOptions.find { it.id == "backlignt_in_listing" }
        if (isBackLight != null) {
            isPromo = true
            val eventParameters = mapOf(
                "catalog_category" to offer.catpath.lastOrNull(),
                "lot_category" to if (offer.catpath.isEmpty()) 1 else offer.catpath.firstOrNull(),
                "offer_id" to offer.id,
            )

            analyticsHelper.reportEvent("show_top_lots", eventParameters)
        }
    }

    Card(
        colors = if (!isPromo) colors.cardColors else colors.cardColorsPromo,
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        modifier = Modifier
            .clickable {
                if (isPromo){
                    val eventParameters = mapOf(
                        "catalog_category" to offer.catpath.lastOrNull(),
                        "lot_category" to if (offer.catpath.isEmpty()) 1 else offer.catpath.firstOrNull(),
                        "offer_id" to offer.id,
                    )
                    analyticsHelper.reportEvent("click_top_lots", eventParameters)
                }

                onItemClick()
            }
    ) {
        if (isGrid){
            Column(
                modifier = Modifier
                    .padding(dimens.smallPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ListingItemContent(
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
                ListingItemContent(
                    offer,
                    modifier = Modifier.align(Alignment.Bottom),
                    isGrid = isGrid,
                    onFavouriteClick
                )
            }
        }
    }
}
