package market.engine.core.data.states

import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.network.networkObjects.AdditionalDataForNewOrder

data class CreateOrderState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val responseGetOffers: List<OfferItem> = emptyList(),
    val responseGetAdditionalData: AdditionalDataForNewOrder? = null,
    val selectDeliveryMethod: Int = 0,
    val selectDealType: Int = 0,
    val selectPaymentType: Int = 0
)
