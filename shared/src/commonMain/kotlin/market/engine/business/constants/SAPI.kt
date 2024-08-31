package application.market.auction_mobile.business.constants


object SAPI {
    var API_BASE: String = ""

    var SERVER_BASE: String = ""

    var YA_API_KEY: String = ""

    var REVIEW_URL: String = ""

    val headers = mutableMapOf<String, String>()

    var secret = ""

    fun getApiKey(): String {
        val sole = "d434ba90-bf1e-4a"
        return "BYP9qNRlA3Mpx1w8jS31kOnz"
    }
}
