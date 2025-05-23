package market.engine.core.data.items

import market.engine.core.network.networkObjects.Bids
import market.engine.core.network.networkObjects.BuyerData
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.PromoOption
import market.engine.core.network.networkObjects.RelistingMode
import market.engine.core.network.networkObjects.Session
import market.engine.core.network.networkObjects.User

data class OfferItem(
    val id: Long,
    var title: String,
    var images: List<String?>,
    var note : String?,
    var isWatchedByMe: Boolean,
    val videoUrls : List<String>?,
    val isPrototype: Boolean,
    val quantity: Int,
    var price : String,
    val type : String,
    val seller : User,
    var buyer : BuyerData?,
    val numParticipants : Int,
    var watchersCount : Int,
    var viewsCount : Int,
    val publicUrl : String?,
    var relistingMode : RelistingMode?,
    var bids : List<Bids>?,
    var location : String,
    val safeDeal : Boolean,
    var promoOptions: List<PromoOption>?,
    val deliveryMethods: List<DeliveryMethod>? = null,
    var myMaximalBid: String = "0",
    val catPath : List<Long>,
    val discount: Int,
    val isPromo : Boolean,
    val createdTs : Long,
    var state : String?,
    var session : Session?,
)
