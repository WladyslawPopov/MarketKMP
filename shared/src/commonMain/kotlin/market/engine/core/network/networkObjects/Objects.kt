package market.engine.core.network.networkObjects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.globalData.UserData


@Serializable
data class GoogleAuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
)

@Serializable
data class UserPayload(
    @SerialName("token") val token: String? = null,
    @SerialName("user") val user: Long = 1L,
    @SerialName("result") val result: String? = null,
    @SerialName("captcha_key") val captchaKey: String? = null,
    @SerialName("captcha_image") val captchaImage: String? = null,
    @SerialName("additional_data") val additionalData: AdditionalData? = null,
)

@Serializable
data class AdditionalData(
    @SerialName("conversation_id") val conversationId: Long? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("user_id") val userId: Long = 1L,
    @SerialName("result") val result: String? = null
)

@Serializable
data class AdditionalDataForNewOrder(
    @SerialName("delivery_methods") val deliveryMethods: List<DeliveryMethod> = listOf(),
    @SerialName("payment_methods") val paymentMethods: List<PaymentMethod> = listOf(),
    @SerialName("deal_types") val dealTypes: List<DealType> = listOf(),
)

@Serializable
data class Fields(
    @SerialName("widget_type") val widgetType: String? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("short_description") val shortDescription: String? = null,
    @SerialName("long_description") val longDescription: String? = null,
    @SerialName("errors") var errors: JsonElement? = null,
    @SerialName("data") var data: JsonElement? = null,
    @SerialName("choices") var choices: List<Choices>? = listOf(),
    @SerialName("has_data") var hasData: Boolean = false,
    @SerialName("validators") val validators: List<Validator>? = listOf(),
    @SerialName("links") val links: Urls? = null
)

@Serializable
data class Validator(
    @SerialName("type") val type: String? = null,
    @SerialName("parameters") val parameters: Parameters? = null,
    @SerialName("error_text") val errorText: String? = null
)

@Serializable
data class Parameters(
    @SerialName("default") val default: Boolean = false,
    @SerialName("max") val max: Int = 1,
    @SerialName("min") val min: Int = 1,
)

@Serializable
data class Choices(
    @SerialName("code") val code: JsonPrimitive? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("weight") val weight: JsonPrimitive? = null,
    @SerialName("extended_fields") val extendedFields: List<Fields>? = null
)

@Serializable
data class Deliveries(
    @SerialName("code") val code: Double = 0.0,
    @SerialName("delivery_price_city") val deliveryPriceCity: String? = null,
    @SerialName("delivery_price_country") val deliveryPriceCountry: String? = null,
    @SerialName("delivery_price_world") val deliveryPriceWorld: String? = null,
    @SerialName("delivery_comment") val deliveryComment: String? = null
)

@Serializable
data class OperationResult(
    @SerialName("result") val result: String? = null,
    @SerialName("message") val message: String? = null
)

@Serializable
data class UserBody(
    @SerialName("total_percentage") val totalPercentage: Int = 0,
    @SerialName("available_quantity") val availableQuantity: Int = 0,
    @SerialName("quantity") var quantity: Int = 0,
    @SerialName("offer_id") val offerId: Long = 1L,
    @SerialName("seller_id") val sellerId: Long = 1L,
    @SerialName("offer_price") val offerPrice: String? = null,
    @SerialName("offer_title") val offerTitle: String? = null,
    @SerialName("freelocation") val freeLocation: String? = null,
    @SerialName("offer_image") val offerImage: String? = null,
    @SerialName("is_buyable") val isBuyable: Boolean? = null,
)

@Serializable
data class Category(
    @SerialName("id") val id: Long = 1L,
    @SerialName("name") val name: String? = null,
    @SerialName("is_leaf") val isLeaf: Boolean = false,
    @SerialName("created_ts") val createdTs: Long = 1L,
    @SerialName("parent_id") val parentId: Long = 1L,
    @SerialName("owner") val owner: Long = 1L,
    @SerialName("is_directly_adult") val isDirectlyAdult: Boolean = false,
    @SerialName("estimated_active_offers_count") var estimatedActiveOffersCount: Int = 1,
)

@Serializable
data class BodyObj(
    @SerialName("action") val action: String? = null,
    @SerialName("is_changed") val isChanged: Boolean = false,
    @SerialName("bids_count") val bidsCount: Int = 0,
    @SerialName("bids") val bids: List<Bids> = listOf(),
    @SerialName("winner_id") val winnerId: Long = 1L,
    @SerialName("current_price") val currentPrice: String? = null,
    @SerialName("minimal_acceptable_price") val minimalAcceptablePrice: String? = null,
    @SerialName("server_now") val serverNow: Long? = null,
    @SerialName("time_left") val timeLeft: Long? = null,
    @SerialName("session_end_ts") val sessionEndTs: Long? = null,
    @SerialName("current_version") val currentVersion: Int? = null,
    @SerialName("state") val state: String? = null,
)

@Serializable
data class Snapshot(
    @SerialName("title") val title: String? = null,
    @SerialName("id") val id: Long? = 1L,
    @SerialName("price_per_item") val pricePerItem: String? = null,
    @SerialName("created_ts") val createdTs: Long = 1,
    @SerialName("lastupdated_ts") val lastUpdatedTs: Long = 1,
    @SerialName("current_price_per_item") var currentPricePerItem: String? = null,
)
@Serializable
data class Offer(
    @SerialName("title") var title: String? = null,
    @SerialName("id") val id: Long = 1L,
    @SerialName("name") val name: String? = null,
    @SerialName("is_leaf") val isLeaf: Boolean = false,
    @SerialName("owner") val owner: Long = 1L,
    @SerialName("created_ts") val createdTs: Long = 1,
    @SerialName("lastupdated_ts") val lastUpdatedTs: Long = 1,
    @SerialName("current_price_per_item") var currentPricePerItem: String? = null,
    @SerialName("buynow_price") var buyNowPrice: String? = null,
    @SerialName("catpath") val catpath: List<Long> = listOf(),
    @SerialName("current_quantity") val currentQuantity: Int = 1,
    @SerialName("original_quantity") val originalQuantity: Int = 1,
    @SerialName("discount_percentage") val discountPercentage: Int = 1,
    @SerialName("safe_deal") val safeDeal: Boolean = false,
    @SerialName("num_participants") val numParticipants: Int = 1,
    @SerialName("seller_data") var sellerData: User? = null,
    @SerialName("buyer_data") val buyerData: BuyerData? = null,
    @SerialName("state") var state: String? = null,
    @SerialName("session") var session: Session? = null,
    @SerialName("minimum_delivery_price") val minimumDeliveryPrice: String? = null,
    @SerialName("images") var images: List<Image>? = null,
    @SerialName("image") val image: Urls? = null,
    @SerialName("delivery_methods") val deliveryMethods: List<DeliveryMethod>? = null,
    @SerialName("payment_methods") val paymentMethods: List<PaymentMethod>? = null,
    @SerialName("sale_type") val saleType: String? = null,
    @SerialName("region") var region: Region? = null,
    @SerialName("free_location") var freeLocation: String? = null,
    @SerialName("who_pays_for_delivery") val whoPaysForDelivery: WhoPaysForDelivery? = null,
    @SerialName("deal_types") val dealTypes: List<DealType>? = null,
    @SerialName("promo_options") val promoOptions: List<PromoOption>? = null,
    @SerialName("params") val params: List<Param>? = null,
    @SerialName("video_urls") val videoUrls: List<String>? = null,
    @SerialName("views_count") val viewsCount: Int = 1,
    @SerialName("my_maximal_bid") val myMaximalBid: String = "0",
    @SerialName("watchers_count") val watchersCount: Int = 1,
    @SerialName("public_url") val publicUrl: String? = null,
    @SerialName("estimated_active_offers_count") val estimatedActiveOffersCount: Int = 1,
    @SerialName("has_temp_images") val hasTempImages: Boolean = false,
    @SerialName("bids") var bids: List<Bids>? = null,
    @SerialName("relisting_mode") var relistingMode: RelistingMode? = null,
    @SerialName("public_snapshot_url") val publicSnapshotUrl: String? = null,
    @SerialName("antisniper") val antisniper: Boolean = false,
    @SerialName("description") val description: String? = null,
    @SerialName("minimal_acceptable_price") var minimalAcceptablePrice: String? = null,
    @SerialName("is_watched_by_me") var isWatchedByMe: Boolean = false,
    @SerialName("quantity") val quantity: Int = 0,
    @SerialName("snapshot_id") val snapshotId: Long = 1L,
    @SerialName("price_per_item") val pricePerItem: String? = null,
    @SerialName("is_proposal_enabled") val isProposalEnabled: Boolean = false,
    @SerialName("version") var version: JsonElement? = null,
    @SerialName("is_prototype") var isPrototype : Boolean = false,
    @SerialName("external_url") val externalUrl : String? = null,
    @SerialName("external_images") val externalImages : List<String>? = listOf(),
    @SerialName("standard_descriptions") val standardDescriptions : List<StandardDescriptions>? = listOf(),
    @SerialName("added_descriptions") val addedDescriptions : List<AddedDescriptions>? = listOf()
)

@Serializable
data class StandardDescriptions(
    @SerialName("deleted") val deleted: Boolean? = null,
    @SerialName("active") val active: Boolean? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("timestamp") val timestamp: Long? = null
)

@Serializable
data class AddedDescriptions(
    @SerialName("text")
    val text: String? = null,
    @SerialName("timestamp")
    val timestamp: Long?=null,
)

@Serializable
data class Bids(
    @SerialName("curprice") val curprice: String? = null,
    @SerialName("ts") val ts: String? = null,
    @SerialName("mover_login") val moverLogin: String? = null,
    @SerialName("mover_id") val moverId: Long = 1L,
    @SerialName("obfuscated_mover_login") val obfuscatedMoverLogin: String? = null
)

@Serializable
data class Operations(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("decision_message") val decisionMessage: String? = null,
    @SerialName("price") val price: Int = 0,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_dataless") val isDataless: Boolean = false,
    @SerialName("is_allowed") val isAllowed: Boolean = false,
)

@Serializable
data class BuyerData(
    @SerialName("id") val id: Long = 1L,
    @SerialName("login") val login: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("rating") val rating: Int = 0,
)

@Serializable
data class RatingBadge(
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("urls") val urls: Urls? = null
)

@Serializable
data class Avatar(
    @SerialName("origin") val origin: JsonPrimitive? = null,
    @SerialName("thumb") val thumb: JsonPrimitive? = null,
)

@Serializable
data class Session(
    @SerialName("start") val start: String? = null,
    @SerialName("end") val end: String? = null,
)

@Serializable
data class Image(
    @SerialName("id") val id: Long = 1L,
    @SerialName("urls") val urls: Urls? = null
)

@Serializable
data class Urls(
    @SerialName("big") val big: JsonPrimitive? = null,
    @SerialName("mid") val mid: JsonPrimitive? = null,
    @SerialName("small") val small: JsonPrimitive? = null,
    @SerialName("tiny") val tiny: JsonPrimitive? = null
)

@Serializable
data class DeliveryMethod(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 1,
    @SerialName("weight") val weight: Int = 1,
    @SerialName("price_within_city") val priceWithinCity: String? = null,
    @SerialName("price_within_country") val priceWithinCountry: String? = null,
    @SerialName("price_within_world") val priceWithinWorld: String? = null,
    @SerialName("comment") val comment: String? = null
)

@Serializable
data class PaymentMethod(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 1,
    @SerialName("weight") val weight: Int = 1,
)

@Serializable
data class RelistingMode(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 1,
)

@Serializable
data class RegionOptions(
    @SerialName("options") val options: List<Options> = listOf()
)

@Serializable
data class Options(
    @SerialName("code") val code: Int = 1,
    @SerialName("weight") val weight: Int = 0,
    @SerialName("name") val name: String? = null,
)

@Serializable
data class Region(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 1,
)

@Serializable
data class WhoPaysForDelivery(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 1,
)

@Serializable
data class DealType(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 0,
    @SerialName("weight") val weight: Int = 0,
)

@Serializable
data class PromoOption(
    @SerialName("name") val name: String? = null,
    @SerialName("id") val id: String? = null,
)

@Serializable
data class Param(
    @SerialName("param_id") val paramId: Long = 1L,
    @SerialName("name") val name: String? = null,
    @SerialName("value") val value: Value? = null,
)

@Serializable
data class Value(
    @SerialName("free_value_type") val freeValueType: String? = null,
    @SerialName("value_free") val valueFree: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("value_choices") val valueChoices: List<ValueChoice>? = null
)

@Serializable
data class ValueChoice(
    @SerialName("name") val name: String? = null,
    @SerialName("code") val code: Int = 1,
)

@Serializable
data class Banners(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("link") val link: String? = null,
)

@Serializable
data class ListData(
    @SerialName("data") val data: ArrayList<ListItem> = arrayListOf(),
)

@Serializable
data class ListItem(
    @SerialName("id") val id: Long = 0L,
    @SerialName("login") val name: String? = null,
    @SerialName("rating") val rating: Int = 0,
    @SerialName("comment") val comment: String? = null,
    @SerialName("listed_at") val listedAt: Long? = null,
)

@Serializable
data class User(
    @SerialName("id") val id: Long = 1L,
    @SerialName("owner") val owner: Long = 1L,
    @SerialName("created_ts") val createdTs: Long = 1L,
    @SerialName("lastupdated_ts") val lastUpdatedTs: Long = 1L,
    @SerialName("login") val login: String? = null,
    @SerialName("rating") val rating: Int = 1,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("rating_badge") val ratingBadge: RatingBadge? = null,
    @SerialName("last_active_ts") val lastActiveTs: Long = 1,
    @SerialName("avatar") val avatar: Avatar? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("about_me") val aboutMe: String? = null,
    @SerialName("count_unread_messages") val countUnreadMessages: Int = 0,
    @SerialName("count_watched_offers") val countWatchedOffers: Int = 0,
    @SerialName("count_offers_in_cart") val countOffersInCart: Int = 0,
    @SerialName("followers_count") val followersCount: Int? = 0,
    @SerialName("marked_as_deleted") val markedAsDeleted: Boolean = false,
    @SerialName("balance") val balance: Double = 0.0,
    @SerialName("feedback_total") val feedbackTotal: FeedbackTotal? = null,
    @SerialName("count_unread_price_proposals") val countUnreadPriceProposals: Int = 0,
    @SerialName("average_response_time") val averageResponseTime: String? = null,
    @SerialName("vacation_enabled") var vacationEnabled: Boolean = false,
    @SerialName("vacation_end") val vacationEnd: Long = 0L,
    @SerialName("vacation_start") val vacationStart: Long = 0L,
    @SerialName("vacation_message") val vacationMessage: String? = null,
    @SerialName("watermark_enabled") var waterMarkEnabled: Boolean = false,
    @SerialName("block_rating_enabled") var blockRatingEnabled: Boolean = false,
    @SerialName("auto_feedback_enabled") val autoFeedbackEnabled: Boolean = false,
    @SerialName("auto_feedback_message") val autoFeedbackMessage: String? = null,
    @SerialName("auto_feedback_after_exceeded_days_limit_enabled") val autoFeedbackExceededEnabled: Boolean = false,
    @SerialName("role") val role: String? = null,
)

@Serializable
data class FeedbackTotal(
    @SerialName("percent_of_positive_feedbacks") val percentOfPositiveFeedbacks: Double = 0.0,
    @SerialName("positive_feedbacks_count") val positiveFeedbacksCount: Int = 0,
    @SerialName("total_percentage") val totalPercentage: Double = 0.0
)

@Serializable
data class Order(
    @SerialName("id") val id: Long = 1L,
    @SerialName("owner") val owner: Long = 1L,
    @SerialName("remoteparty") val remoteparty: Long = 1L,
    @SerialName("created_ts") val createdTs: Long = 1,
    @SerialName("lastupdated_ts") val lastUpdatedTs: Long = 1,
    @SerialName("parcel_sent") val parcelSent: Boolean? = null,
    @SerialName("paid") val paid: Boolean? = null,
    @SerialName("seller_data") val sellerData: User? = null,
    @SerialName("buyer_data") val buyerData: BuyerData? = null,
    @SerialName("suborders") val suborders: List<Offer> = listOf(),
    @SerialName("feedbacks") val feedbacks: Feedbacks? = null,
    @SerialName("marks") val marks: List<Marks>? = null,
    @SerialName("track_id") val trackId: String? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("success_fee") val successFee: Float = 0F,
    @SerialName("deal_type") val dealType: DealType? = null,
    @SerialName("delivery_address") val deliveryAddress: DeliveryAddress? = null,
    @SerialName("payment_method") val paymentMethod: PaymentMethod? = null,
    @SerialName("delivery_method") val deliveryMethod: DeliveryMethod? = null,
    @SerialName("total") val total: String? = null,
)

@Serializable
data class Feedbacks(
    @SerialName("s2b") val s2b: Report? = null,
    @SerialName("b2s") val b2s: Report? = null
)

@Serializable
data class Report(
    @SerialName("type") val type: String? = null,
    @SerialName("comment") val comment: String? = null,
)

@Serializable
data class Marks(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("value") val value: Boolean = false,
)

@Serializable
data class AddressCards(
    @SerialName("address_cards") val addressCards: List<DeliveryAddress>? = null,
)

@Serializable
data class DeliveryAddress(
    @SerialName("id_as_ts") val id: Long = 1L,
    @SerialName("surname") val surname: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("city") val city: JsonElement? = null,
    @SerialName("zip") val zip: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
)

@Serializable
data class Conversations(
    @SerialName("id") val id: Long = 1L,
    @SerialName("created_ts") val createdTs: Long = 1L,
    @SerialName("lastupdated_ts") val lastUpdatedTs: Long = 1L,
    @SerialName("interlocutor") val interlocutor: User? = null,
    @SerialName("about_object_class") val aboutObjectClass: String? = null,
    @SerialName("new_message") val newMessage: String? = null,
    @SerialName("quantity") val quantity: Int = 0,
    @SerialName("about_object_id") val aboutObjectId: Long = 1L,
    @SerialName("new_message_ts") val newMessageTs: Long = 1L,
    @SerialName("count_total_messages") val countTotalMessages: Int = 0,
    @SerialName("count_unread_messages") val countUnreadMessages: Int = 0,
    @SerialName("about_object_icon") val aboutObjectIcon: Urls? = null
)

@Serializable
data class Dialog(
    @SerialName("id") val id: Long = 1L,
    @SerialName("created_ts") val createdTs: Long = 1L,
    @SerialName("lastupdated_ts") val lastUpdatedTs: Long = 1L,
    @SerialName("about_object_class") val aboutObjectClass: String? = null,
    @SerialName("about_object_id") val aboutObjectId: Long = 1L,
    @SerialName("sender") val sender: Long = 1L,
    @SerialName("receiver") val receiver: Long = 1L,
    @SerialName("dialog_id") val dialogId: Long = 1L,
    @SerialName("message") val message: String? = null,
    @SerialName("read_by_receiver") val readByReceiver: Boolean = false,
    @SerialName("images") val images: List<MesImage>? = null
)

@Serializable
data class Reports(
    @SerialName("type") val type: String? = null,
    @SerialName("comment") val comment: String? = null,
    @SerialName("order_id") val orderId: Long? = 1L,
    @SerialName("feedback_ts") val feedbackTs: Long = 1L,
    @SerialName("to_user") val toUser: User? = null,
    @SerialName("from_user") val fromUser: User? = null,
    @SerialName("response_feedback") val responseFeedback: Report? = null,
    @SerialName("offersnapshot") val offerSnapshot: Snapshot? = null,
)

@Serializable
data class MesImage(
    @SerialName("thumb_url") val thumbUrl: String? = null,
    @SerialName("url") val url: String? = null,
)

@Serializable
data class Proposals(
    @SerialName("buyer_info") val buyerInfo: User? = null,
    @SerialName("proposals") val proposals: List<Proposal>? = null,
)

@Serializable
data class Proposal(
    @SerialName("price") val price: String? = null,
    @SerialName("buyer_comment") val buyerComment: String? = null,
    @SerialName("seller_comment") val sellerComment: String? = null,
    @SerialName("created_ts") val createdTs: Long = 0L,
    @SerialName("ts_to_end_answer") val tsToEndAnswer: Long = 0L,
    @SerialName("feedback_ts") val feedbackTs: Long = 1L,
    @SerialName("class") val clas: String? = null,
    @SerialName("is_responser_proposal") val isResponserProposal: Boolean = false,
    @SerialName("quantity") val quantity: Int = 0,
)

@Serializable
data class Subscription(
    @SerialName("id") val id: Long = 0L,
    @SerialName("name") val name: String? = null,
    @SerialName("offer_scope") val offerScope: Long = 0L,
    @SerialName("is_enabled") var isEnabled: Boolean = false,
    @SerialName("created_ts") val createdTs: Long = 0L,
    @SerialName("price_from") val priceFrom: String? = null,
    @SerialName("price_to") val priceTo: String? = null,
    @SerialName("search_query") val searchQuery: String? = null,
    @SerialName("catpath") val catpath: Map<Long, String>? = null,
    @SerialName("region") val region: Region? = null,
    @SerialName("sale_type") val saleType: String? = null,
    @SerialName("seller_data") val sellerData: User? = null,
    @SerialName("notification_schedule") val notificationSchedule: NotificationSchedule? = null
)

@Serializable
data class NotificationSchedule(
    @SerialName("days_of_week") val daysOfWeek: List<Int> = listOf(),
    @SerialName("ts") val ts: Int = 0
)
