package market.engine.core.data.events

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

    fun goToProposalPage(type: ProposalType)
    fun goToDynamicSettings(type: String, id: Long)
    fun goToLogin()
    fun goToDialog(id: Long?)
    fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>)

    fun scrollToBids()
    fun update()
}
