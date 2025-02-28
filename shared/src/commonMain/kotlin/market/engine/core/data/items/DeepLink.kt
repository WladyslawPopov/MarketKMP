package market.engine.core.data.items

import kotlinx.serialization.Serializable

@Serializable
sealed class DeepLink {
    @Serializable
    class GoToUser(val userId: Long) : DeepLink()
    @Serializable
    class GoToListing(val ownerId: Long) : DeepLink()
    @Serializable
    class GoToOffer(val offerId: Long) : DeepLink()
    @Serializable
    class GoToDynamicSettings(val ownerId: Long?, val code: String?, val settingsType: String) : DeepLink()
    @Serializable
    class GoToVerification(val ownerId: Long?, val code: String?, val settingsType: String?) : DeepLink()
    @Serializable
    class GoToAuth(val clientId: String?=null, val redirectUri: String?=null) : DeepLink()
    @Serializable
    class GoToDialog(val dialogId: Long,val mes : String?) : DeepLink()
    @Serializable
    data object GoToRegistration : DeepLink()
    @Serializable
    data class Unknown(val path: String) : DeepLink()
}
