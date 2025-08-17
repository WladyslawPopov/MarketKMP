package market.engine.core.data.states

import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.items.TopCategory

data class HomeUiState(
    val categories: List<TopCategory> = emptyList(),
    val promoOffers1: List<OfferItem> = emptyList(),
    val promoOffers2: List<OfferItem> = emptyList(),
    val appBarData : SimpleAppBarData = SimpleAppBarData(),
    val listFooter: List<TopCategory> = emptyList(),
    val drawerList: List<NavigationItem> = emptyList(),
)
