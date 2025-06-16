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
    val id: Long,
    var title: String,
    var images: List<String?>,
    var note : String? = null,
    var isWatchedByMe: Boolean,
    val videoUrls : List<String>? = null,
    val isPrototype: Boolean = false,
    var quantity: Int,
    var currentQuantity: Int,
    var price : String,
    val type : String = "",
    val seller : User,
    var buyer : BuyerData? = null,
    val numParticipants : Int = 0,
    var watchersCount : Int = 0,
    var viewsCount : Int = 0,
    val publicUrl : String? = null,
    var relistingMode : RelistingMode? = null,
    var bids : List<Bids>? = null,
    var location : String,
    val safeDeal : Boolean = false,
    var promoOptions: List<PromoOption>? = null,
    val deliveryMethods: List<DeliveryMethod>? = null,
    var myMaximalBid: String = "0",
    val catPath : List<Long> = emptyList(),
    val discount: Int = 0,
    val isPromo : Boolean = false,
    val createdTs : Long = getCurrentDate().toLongOrNull() ?: 1,
    var state : String?,
    var session : Session? = null,
)
