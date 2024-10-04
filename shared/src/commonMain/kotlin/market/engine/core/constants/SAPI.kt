package market.engine.core.constants


object SAPI {
    var API_BASE: String = "https://api.auction.ru/"

    var SERVER_BASE: String = "https://auction.ru/"

    var YA_API_KEY: String = "d434ba90-bf1e-4a52-ab21-c838dfc7e1a9"

    var REVIEW_URL: String = "https://play.google.com/store/apps/details?id=application.market.auction_mobile"

    val headers = mutableMapOf<String, String>()

    var secret = ""

    fun getApiKey(): String {
        val sole = "d434ba90-bf1e-4a"
        return "CtCN5KhmBdWvNzJEfr3pwgwU"
    }
}
