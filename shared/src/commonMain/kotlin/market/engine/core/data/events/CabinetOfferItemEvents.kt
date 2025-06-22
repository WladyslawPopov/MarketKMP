package market.engine.core.data.events

import market.engine.core.data.items.MenuItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType

interface CabinetOfferItemEvents {
    fun getMenuOperations(tag : String? = null, callback : (List<MenuItem>) -> Unit)
    fun onItemClick()
    fun goToCreateOffer(type : CreateOfferType)
    fun goToDynamicSettings(type : String, id: Long?)
    fun onUpdateItem()
    fun goToUser()
    fun goToPurchase()
    fun goToProposal(type: ProposalType)
    fun sendMessageToUser()
    fun isHideItem() : Boolean
}
