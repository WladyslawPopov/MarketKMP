package market.engine.core.data.globalData

object SAPI {
    var API_BASE: String = ""

    var SERVER_BASE: String = ""

    var REVIEW_URL: String = ""

    var secret = ""

    var dataPolicyURL = ""

    val headers = mutableMapOf<String, String>()

    var workstationData = ""

    var version = ""

    fun setUpSAPI(apiBase: String, serverBase: String, reviewUrl: String, key: String) {
        API_BASE = apiBase
        SERVER_BASE = serverBase
        REVIEW_URL = reviewUrl
        secret = key
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }

    fun removeHeader(key: String) {
        headers.remove(key)
    }
}
