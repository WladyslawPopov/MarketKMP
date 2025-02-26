package market.engine.core.data.items

sealed class DeepLink {
    class GoToUser(val userId: Long) : DeepLink()
    class GoToListing(val ownerId: Long) : DeepLink()
    class GoToOffer(val offerId: Long) : DeepLink()
    class GoToDynamicSettings(val ownerId: Long?, val code: String?, val settingsType: String) : DeepLink()
    class GoToVerification(val ownerId: Long?, val code: String?, val settingsType: String?) : DeepLink()
    class GoToAuth(val clientId: String?=null, val redirectUri: String?=null) : DeepLink()
    class GoToDialog(val dialogId: Long,val mes : String?) : DeepLink()
    data object GoToRegistration : DeepLink()
    data class Unknown(val path: String) : DeepLink()
}
