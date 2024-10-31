package market.engine.core.globalData


object SAPI {
    var API_BASE: String = ""

    var SERVER_BASE: String = ""

    var YA_API_KEY: String = ""

    var REVIEW_URL: String = ""

    var secret = ""

    val headers = mutableMapOf<String, String>()

    var workstationData = ""

    var version = ""

    fun getApiKey(): String {
        //val sole = "d434ba90-bf1e-4a"
        return secret
    }
}
