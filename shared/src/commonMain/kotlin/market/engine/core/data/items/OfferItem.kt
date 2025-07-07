package market.engine.core.data.items

import kotlinx.serialization.Serializable
import market.engine.core.network.networkObjects.Bids
import market.engine.core.network.networkObjects.BuyerData
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.PromoOption
import market.engine.core.network.networkObjects.RelistingMode
import market.engine.core.network.networkObjects.Session
import market.engine.core.network.networkObjects.User
import market.engine.core.utils.getCurrentDate


@Serializable
data class OfferItem(
    val id: Long = 1L,
    var title: String = "",
    var images: List<String> = emptyList(),
    var note : String? = null,
    var isWatchedByMe: Boolean = false,
    var videoUrls : List<String>? = null,
    var isPrototype: Boolean = false,
    var quantity: Int = 0,
    var currentQuantity: Int = 0,
    var price : String = "",
    val type : String = "",
    var seller : User = User(),
    var buyer : BuyerData? = null,
    val numParticipants : Int = 0,
    var watchersCount : Int = 0,
    var viewsCount : Int = 0,
    var publicUrl : String? = null,
    var relistingMode : RelistingMode? = null,
    var bids : List<Bids>? = null,
    var location : String = "",
    var safeDeal : Boolean = false,
    var promoOptions: List<PromoOption>? = null,
    val deliveryMethods: List<DeliveryMethod>? = null,
    var myMaximalBid: String = "0",
    var catPath : List<Long> = emptyList(),
    val discount: Int = 0,
    val isPromo : Boolean = false,
    val createdTs : Long = getCurrentDate().toLongOrNull() ?: 1,
    var state : String? = null,
    var session : Session? = null,
    var isProposalEnabled : Boolean = false,
    var externalImages : List<String>? = null,
)
