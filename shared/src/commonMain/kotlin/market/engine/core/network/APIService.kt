package market.engine.core.network


import market.engine.core.network.networkObjects.AppResponse
import market.engine.core.network.networkObjects.GoogleAuthResponse
import io.ktor.client.request.forms.submitFormWithBinaryData
import kotlin.collections.HashMap
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.http.content.PartData
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class APIService(private val client: HttpClient) {

    suspend fun getImage(url: String): HttpResponse =
        client.get(url).body()

    suspend fun getPage(url: String): AppResponse =
        client.get(url).body()

    suspend fun postCreateOfferPage(url: String, body: JsonObject): AppResponse =
        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun uploadFile(part: List<PartData>): AppResponse {
        return client.submitFormWithBinaryData(
            url = "upload",
            formData = part
        ).body()
    }

    suspend fun getOurChoiceOffers(idOffer : Long): AppResponse =
        client.get("offers/get_featured_in_offers?offer_id=$idOffer").body()

    suspend fun postUnMarkAsParcelSent(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/unmark_as_parcel_sent").body()

    suspend fun postMarkAsParcelSent(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/mark_as_parcel_sent").body()

    suspend fun postPMOperationDeleteForInterlocutor(messagesId: Long): AppResponse =
        client.post("private_messages/$messagesId/operations/delete_for_interlocutor").body()

    suspend fun postUnMarkAsPaymentSent(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/unmark_as_payment_sent").body()

    suspend fun postMarkAsPaymentSent(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/mark_as_payment_received").body()

    suspend fun postMarkAsArchivedBySeller(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/mark_as_archived_by_seller").body()

    suspend fun postEnableFeedbacks(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/enable_feedbacks").body()

    suspend fun postDisableFeedbacks(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/disable_feedbacks").body()

    suspend fun postRemoveFeedbackToBuyer(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/remove_feedback_to_buyer").body()

    suspend fun postMarkAsArchivedByBuyer(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/mark_as_archived_by_buyer").body()

    suspend fun postUnMarkAsArchivedBySeller(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/unmark_as_archived_by_seller").body()

    suspend fun postOfferOperationsAddBid(idOffer: Long, body: Map<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/add_bid") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOfferOperationsWriteToSeller(idOffer: Long, body: Map<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/write_to_seller") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOrderOperationsSetComment(idOrder: Long, body: Map<String, String>): AppResponse =
        client.post("orders/$idOrder/operations/set_comment") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOrderOperationsGiveFeedbackToBuyer(idOrder: Long, body: Map<String, String>): AppResponse =
        client.post("orders/$idOrder/operations/give_feedback_to_buyer") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOrderOperationsGiveFeedbackToSeller(idOrder: Long, body: Map<String, String>): AppResponse =
        client.post("orders/$idOrder/operations/give_feedback_to_seller") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOrderOperationsProvideTrackId(idOrder: Long, body: Map<String, String>): AppResponse =
        client.post("orders/$idOrder/operations/provide_track_id") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOrderOperationsWriteToPartner(idOrder: Long, body: Map<String, String>): AppResponse =
        client.post("orders/$idOrder/operations/write_to_partner") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUsersOperationsResetPassword(): AppResponse =
        client.get("request_password_reset").body()

    suspend fun postUsersOperationsResetPassword(body: Map<String, JsonElement>): AppResponse =
        client.post("request_password_reset") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUsersOperationsAddressCards(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/get_address_cards").body()

    suspend fun postUsersOperationsConfirmEmail(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/confirm_email") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postCheckingConversationExistenceOffer(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/checking_conversation_existence").body()

    suspend fun postCheckingConversationExistenceOrder(idOrder: Long): AppResponse =
        client.post("orders/$idOrder/operations/checking_conversation_existence").body()

    suspend fun postOfferOperationsGetLeaderAndPrice(idOffer: Long, body: HashMap<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/get_leader_and_price") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUserOperationsGetAdditionalDataBeforeCreateOrder(idUser: Long, body: JsonObject): AppResponse =
        client.post("users/$idUser/operations/get_additional_data_before_create_order") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postConversationOperationsAddMessage(dialogId: Long, body: JsonObject): AppResponse =
        client.post("conversations/$dialogId/operations/add_message") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postConversationsOperationsMarkAsReadByInterlocutor(idConversation: Long): AppResponse =
        client.post("conversations/$idConversation/operations/mark_as_read_by_interlocutor").body()

    suspend fun getConversation(idConversation: Long): AppResponse =
        client.get("conversations/$idConversation").body()

    suspend fun postConversationsOperationsDeleteForInterlocutor(idConversation: Long): AppResponse =
        client.post("conversations/$idConversation/operations/delete_for_interlocutor").body()

    suspend fun getOfferOperations(idOffer: Long, tag : String): AppResponse =
        client.get("offers/$idOffer/operations?tag=$tag").body()

    suspend fun getOffersListItemOperations(idOffer: Long): AppResponse =
        client.get("offers_lists/$idOffer/operations").body()

    suspend fun getOffersListItem(id: Long): AppResponse =
        client.get("offers_lists/$id").body()

    suspend fun getOrderOperations(idOrder: Long): AppResponse =
        client.get("orders/$idOrder/operations").body()

    suspend fun postUserOperationsGetCartItems(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/get_cart_items").body()

    suspend fun getOrder(idOrder: Long): AppResponse =
        client.get("orders/$idOrder").body()

    suspend fun getOffer(idOffer: Long): AppResponse =
        client.get("offers/$idOffer").body()

    suspend fun getOfferSnapshots(idOfferSnapshots: Long): AppResponse =
        client.get("offersnapshots/$idOfferSnapshots").body()

    suspend fun getOffersRecommendedInListing(idCategory: Long): AppResponse =
        client.get("offers/get_offers_recommended_in_listing") {
            parameter("category_id", idCategory)
        }.body()

    suspend fun getPublicCategories(idCategory: Long): AppResponse =
        client.get("categories/get_children_of_category") {
            parameter("category_id", idCategory)
        }.body()

    suspend fun getPublicCategory(idCategory: Long): AppResponse =
        client.get("categories/$idCategory").body()

    suspend fun getTaggedBy(tag: String): AppResponse =
        client.get("parameters/get_tagged_by") {
            parameter("tag", tag)
        }.body()

    suspend fun getOffersPromotedOnMainPage(page: Int, ipp: Int): AppResponse =
        client.get("offers/get_offers_promoted_on_main_page") {
            parameter("pg", page)
            parameter("ipp", ipp)
        }.body()

    suspend fun getOperationFields(id: Long, operation: String, method: String): AppResponse =
        client.get("/$method/$id/operations/$operation").body()

    suspend fun postOperation(id: Long, operation: String, method: String, body: Map<String, JsonElement>): AppResponse =
        client.post("/$method/$id/operations/$operation") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getSupServViewModel(): AppResponse =
        client.get("send_message_to_support").body()

    suspend fun postSupServViewModel(body: HashMap<String, JsonElement>): AppResponse =
        client.post("send_message_to_support") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postCrateOrder(idUser: Long, body: JsonObject): AppResponse =
        client.post("users/$idUser/operations/create_new_order") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postAuthExternal(body: HashMap<String, String>): AppResponse =
        client.post("auth_external") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postAuth(body: HashMap<String, String>): AppResponse =
        client.post("auth") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUsers(idUser: Long): AppResponse =
        client.get("users/$idUser").body()

    suspend fun getSubscription(idSub: Long): AppResponse =
        client.get("subscriptions/$idSub").body()

    suspend fun postChangeTokenGoogleAuth(body: HashMap<String, String>): GoogleAuthResponse =
        client.post("https://www.googleapis.com/oauth2/v4/token") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postRegistration(body: HashMap<String, JsonElement>): AppResponse =
        client.post("registration") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getRegistration(): AppResponse =
        client.get("registration").body()

    suspend fun getProposal(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/get_proposals").body()

    suspend fun postMakeProposal(idOffer: Long, body: HashMap<String, JsonElement>): AppResponse =
        client.post("offers/$idOffer/operations/make_proposal") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postActOnProposal(idOffer: Long, body: HashMap<String, JsonElement>): AppResponse =
        client.post("offers/$idOffer/operations/act_on_proposal") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getSubscriptionOperations(idSub: Long): AppResponse =
        client.get("subscriptions/$idSub/operations").body()

    suspend fun postUserList(idUser: Long, body: HashMap<String, String>): AppResponse {
        return client.post("users/$idUser/operations/get_user_list") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }
}
