package market.engine.core.data.items

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import market.engine.core.network.networkObjects.AddedDescriptions
import market.engine.core.network.networkObjects.Bids
import market.engine.core.network.networkObjects.BuyerData
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.Param
import market.engine.core.network.networkObjects.PromoOption
import market.engine.core.network.networkObjects.Region
import market.engine.core.network.networkObjects.RelistingMode
import market.engine.core.network.networkObjects.RemoveBid
import market.engine.core.network.networkObjects.Session
import market.engine.core.network.networkObjects.StandardDescriptions
import market.engine.core.network.networkObjects.User
import market.engine.core.network.networkObjects.WhoPaysForDelivery
import market.engine.core.utils.getCurrentDate


@Serializable
data class OfferItem(
    val id: Long = 1L,
    val title: String = "",
    val images: List<String> = emptyList(),
    val note: String? = null,
    val isWatchedByMe: Boolean = false,
    val videoUrls: List<String> = emptyList(),
    val isPrototype: Boolean = false,
    val quantity: Int = 0,
    val currentQuantity: Int = 0,
    val price: String = "",
    val minimalAcceptablePrice: String = "0",
    val type: String = "",
    val seller: User = User(),
    val buyer: BuyerData? = null,
    val numParticipants: Int = 0,
    val watchersCount: Int = 0,
    val viewsCount: Int = 0,
    val publicUrl: String? = null,
    val relistingMode: RelistingMode? = null,
    val bids: List<Bids>? = null,
    val location: String = "",
    val safeDeal: Boolean = false,
    val promoOptions: List<PromoOption> = emptyList(),
    val deliveryMethods: List<DeliveryMethod>? = null,
    val myMaximalBid: String = "0",
    val catPath: List<Long> = emptyList(),
    val discount: Int = 0,
    val isPromo: Boolean = false,
    val createdTs: Long = getCurrentDate().toLongOrNull() ?: 1,
    val state: String? = null,
    val session: Session? = null,
    val externalImages: List<String>? = null,
    val version: JsonElement? = null,
    val standardDescriptions : List<StandardDescriptions>? = listOf(),
    val addedDescriptions : List<AddedDescriptions>? = listOf(),
    val description: String? = null,
    val params: List<Param>? = null,
    val region: Region? = null,
    val hasTempImages: Boolean = false,
    val removedBids: List<RemoveBid>? = null,
    val whoPaysForDelivery: WhoPaysForDelivery? = null,
    val antisniper: Boolean = false,
)
