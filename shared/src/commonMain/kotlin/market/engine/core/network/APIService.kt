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

    suspend fun getPageSub(url: String): AppResponse =
        client.get(url).body()

    suspend fun getPageOrders(url: String): AppResponse =
        client.get(url).body()

    suspend fun getPageConversations(url: String): AppResponse =
        client.get(url).body()

    suspend fun getPageSearchDialogs(url: String): AppResponse =
        client.get(url).body()

    suspend fun getPageFeedbacks(url: String): AppResponse =
        client.get(url).body()

    suspend fun getPageDialogs(url: String): AppResponse =
        client.get(url).body()

    suspend fun getCreateOfferPage(url: String): AppResponse =
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

    suspend fun postOfferOperationsWatch(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/watch").body()

    suspend fun postOfferOperationsProlongOffer(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/prolong_offer").body()

    suspend fun postOfferOperationsUnwatch(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/unwatch").body()

    suspend fun postUsersOperationDeleteCart(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/delete_cart").body()

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

    suspend fun postOfferOperationsActivateOfferForFuture(idOffer: Long, body: Map<String, Long>): AppResponse =
        client.post("offers/$idOffer/operations/activate_offer_for_future") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOfferOperationsActivateOffer(idOffer: Long, body: Map<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/activate_offer") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOfferOperationsSetAntiSniper(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/set_anti_sniper").body()

    suspend fun postOfferOperationsUnsetAntiSniper(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/unset_anti_sniper").body()

    suspend fun postOfferOperationsAddBid(idOffer: Long, body: Map<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/add_bid") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOfferOperationsAddBidError(idOffer: Long, body: Map<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/add_bid") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOfferOperationsDeleteOffer(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/delete_offer").body()

    suspend fun postOfferOperationsFinalizeSession(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/finalize_session").body()

    suspend fun postOfferOperationsWriteToSeller(idOffer: Long, body: Map<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/write_to_seller") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postOfferOperationsWriteToSellerError(idOffer: Long, body: Map<String, String>): AppResponse =
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

    suspend fun postUsersOperationsAddItemToCart(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/add_item_to_cart") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetAvatar(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_avatar") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetLogin(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_login") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetBiddingStep(idUser: Long, body: Map<String, Int>): AppResponse =
        client.post("users/$idUser/operations/set_bidding_step") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetVacation(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_vacation") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetWatermarkEnabled(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/enable_watermark") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun postUsersOperationsSetWatermarkDisabled(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/disable_watermark") {
            contentType(ContentType.Application.Json)
        }.body()

    suspend fun getUsersOperationsSetLogin(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_login").body()

    suspend fun getUsersOperationsSetEmail(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/request_email_change").body()

    suspend fun getUsersOperationsSetPhone(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/request_phone_verification").body()

    suspend fun getUsersOperationsSetMessageToBuyer(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_message_to_buyers").body()

    suspend fun getUsersOperationsSetAutoFeedback(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_auto_feedback").body()
    suspend fun getUsersOperationsSetBiddingStep(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_bidding_step").body()

    suspend fun getUsersOperationsSetVacation(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_vacation").body()

    suspend fun getUsersOperationsSetWatermark(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_watermark").body()

    suspend fun getUsersOperationsSetOutgoingAddress(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/save_outgoing_address").body()

    suspend fun getUsersOperationsAddressCards(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/get_address_cards").body()

    suspend fun getUsersOperationsSetAddressCards(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/save_address_cards").body()

    suspend fun getUsersOperationsEditAddressCards(idUser: Long, body: Map<String, String>): AppResponse =
        client.get("users/$idUser/operations/save_address_cards") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetAddressCards(idUser: Long, body: JsonObject): AppResponse =
        client.post("users/$idUser/operations/save_address_cards") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetOutgoingAddress(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/save_outgoing_address") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetMessageToBuyer(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_message_to_buyers") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetPhone(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/request_phone_verification") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUsersOperationsSetPassword(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_password").body()

    suspend fun getUsersOperationsResetPassword(): AppResponse =
        client.get("request_password_reset").body()

    suspend fun postUsersOperationsResetPassword(body: Map<String, String>): AppResponse =
        client.post("request_password_reset") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetPassword(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_password") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetEmail(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/request_email_change") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetAutoFeedback(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_auto_feedback") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUsersOperationsEditAboutMe(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/edit_about_me").body()

    suspend fun postUsersOperationsConfirmEmail(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/confirm_email") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsRAC(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/request_additional_confirmation") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsEACC(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/enter_additional_confirmation_code") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsVerifyPhone(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/verify_phone") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetDefaultAddressCard(idUser: Long, body: Map<String, Long>): AppResponse =
        client.post("users/$idUser/operations/set_default_address_card") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsDeleteAddressCard(idUser: Long, body: Map<String, Long>): AppResponse =
        client.post("users/$idUser/operations/remove_address_card") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsRACE(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/request_additional_confirmation") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsEditAboutMe(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/edit_about_me") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsSetGender(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/set_gender") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUsersOperationsSetGender(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/set_gender").body()

    suspend fun postUsersOperationsUnsetAvatar(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/unset_avatar").body()

    suspend fun postUsersOperationsAddItemToCartError(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/add_item_to_cart") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsRemoveItemFromCart(idUser: Long, body: Map<String, String>): AppResponse =
        client.post("users/$idUser/operations/remove_item_from_cart") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUsersOperationsRemoveManyItemsFromCart(idUser: Long, body: JsonObject): AppResponse =
        client.post("users/$idUser/operations/remove_many_items_from_cart") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postCheckingConversationExistenceOffer(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/checking_conversation_existence").body()

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

    suspend fun postUserOperationsGetAdditionalDataBeforeCreateOrderError(idUser: Long, body: JsonObject): AppResponse =
        client.post("users/$idUser/operations/get_additional_data_before_create_order") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postConversationOperationsAddMessage(dialogId: Long, body: JsonObject): AppResponse =
        client.post("conversations/$dialogId/operations/add_message") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postConversationOperationsAddMessageError(dialogId: Long, body: JsonObject): AppResponse =
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

    suspend fun getOfferOperations(idOffer: Long): AppResponse =
        client.get("offers/$idOffer/operations").body()

    suspend fun getOrderOperations(idOrder: Long): AppResponse =
        client.get("orders/$idOrder/operations").body()

    suspend fun postUserOperationsGetCartItems(idUser: Long): AppResponse =
        client.post("users/$idUser/operations/get_cart_items").body()

    suspend fun getOfferOperationsActivateOffer(idOffer: Long): AppResponse =
        client.get("offers/$idOffer/operations/activate_offer").body()

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

    suspend fun getSupServViewModel(): AppResponse =
        client.get("send_message_to_support").body()

    suspend fun postSupServViewModel(body: HashMap<String, String>): AppResponse =
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

    suspend fun postChangeTokenGoogleAuth(body: HashMap<String, String>): GoogleAuthResponse =
        client.post("https://www.googleapis.com/oauth2/v4/token") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postRegistration(body: HashMap<String, String>): AppResponse =
        client.post("registration") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getRegistration(): AppResponse =
        client.get("registration").body()

    suspend fun getProposal(idOffer: Long): AppResponse =
        client.post("offers/$idOffer/operations/get_proposals").body()

    suspend fun postMakeProposal(idOffer: Long, body: HashMap<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/make_proposal") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getMakeProposal(idOffer: Long): AppResponse =
        client.get("offers/$idOffer/operations/make_proposal").body()

    suspend fun getActOnProposal(idOffer: Long): AppResponse =
        client.get("offers/$idOffer/operations/act_on_proposal").body()

    suspend fun postActOnProposal(idOffer: Long, body: HashMap<String, String>): AppResponse =
        client.post("offers/$idOffer/operations/act_on_proposal") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun postUserCreateSubscription(idUser: Long, body: HashMap<String, JsonElement>): AppResponse =
        client.post("users/$idUser/operations/create_subscription") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

    suspend fun getUserCreateSubscription(idUser: Long): AppResponse =
        client.get("users/$idUser/operations/create_subscription").body()

    suspend fun getSubscriptionsEditSubscription(idSub: Long): AppResponse =
        client.get("subscriptions/$idSub/operations/edit_subscription").body()

    suspend fun postSubscriptionsEditSubscription(idSub: Long, body: HashMap<String, JsonElement>): AppResponse {
        return client.post("subscriptions/$idSub/operations/edit_subscription") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun getSubscriptionOperations(idSub: Long): AppResponse =
        client.get("subscriptions/$idSub/operations").body()

    suspend fun postSubOperationsEnable(idSub: Long): AppResponse =
        client.post("subscriptions/$idSub/operations/enable_subscription").body()

    suspend fun postSubOperationsDisable(idSub: Long): AppResponse =
        client.post("subscriptions/$idSub/operations/disable_subscription").body()

    suspend fun postSubOperationsDelete(idSub: Long): AppResponse =
        client.post("subscriptions/$idSub/operations/delete_subscription").body()

    suspend fun postUserList(idUser: Long, body: HashMap<String, String>): AppResponse {
        return client.post("users/$idUser/operations/get_user_list") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun postUsersOperationRemoveFromList(idUser: Long, body: HashMap<String, String>, list : String? = null): AppResponse {
        return client.post("users/$idUser/operations/remove_from_$list") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun postUsersOperationAddList(idUser: Long, body: HashMap<String, String>, list : String? = null): AppResponse {
        return client.post("users/$idUser/operations/$list") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

}
