package market.engine.core.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.data.items.OfferItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.utils.parseToOfferItem

class PublicOfferRepository(
    offer: Offer = Offer(),
    val goToOffer : (OfferItem) -> Unit,
    private val addFavorite : (OfferItem, (Boolean) -> Unit) -> Unit,
    private val updateOffer: (Long, (Offer) -> Unit) -> Unit,
) {
    private val _offerState = MutableStateFlow(offer.parseToOfferItem())
    val offerState: StateFlow<OfferItem> = _offerState.asStateFlow()

    fun clickToFavorite() {
        addFavorite(offerState.value) {
            _offerState.value = offerState.value.copy(isWatchedByMe = it)
        }
    }

    fun updateItem() {
        updateOffer(offerState.value.id){
            _offerState.value = it.parseToOfferItem()
        }
    }
}
