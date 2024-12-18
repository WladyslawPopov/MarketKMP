package market.engine.core.data.items

sealed class DeepLink {
    class User(val userId: Long) : DeepLink()
    class Listing(val ownerId: Long) : DeepLink()
    class Offer(val offerId: Long) : DeepLink()
    class Auth(val clientId: String?=null, val redirectUri: String?=null) : DeepLink()
    data object Registration : DeepLink()
    data class Unknown(val path: String) : DeepLink()
}
