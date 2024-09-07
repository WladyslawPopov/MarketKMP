package market.engine.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.market.auction_mobile.business.networkObjects.Offer

@Composable
fun GridPromoOffers(promoOffers : List<Offer>, onOfferClick: (Offer) -> Unit) {

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.heightIn(200.dp, 1800.dp).padding(8.dp).fillMaxSize(),
        userScrollEnabled = false,
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(promoOffers) { offer ->
                PromoLotItem(offer, onOfferClick = onOfferClick)
            }
        }
    )
}
