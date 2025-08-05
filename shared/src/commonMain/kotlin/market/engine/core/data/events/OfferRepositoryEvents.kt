package market.engine.core.data.events

import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType

interface OfferRepositoryEvents {
    fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    )

    fun goToProposalPage(offerId: Long, type: ProposalType)
    fun goToDynamicSettings(type: String, id: Long)
    fun goToDialog(id: Long?)
    fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>)
    fun goToUserPage(sellerId : Long)

    fun openCabinetOffer(offer: OfferItem)

    fun scrollToBids()
    fun refreshPage()
    fun updateBidsInfo(item: OfferItem)
}
