package market.engine.core.network

import market.engine.core.constants.SAPI

class SAPIRepository {

    fun setUpSAPI(apiBase: String, serverBase: String, yaApiKey: String, reviewUrl: String, secret: String) {
        SAPI.API_BASE = apiBase
        SAPI.SERVER_BASE = serverBase
        SAPI.YA_API_KEY = yaApiKey
        SAPI.REVIEW_URL = reviewUrl
        SAPI.secret = secret
    }

    fun addHeader(key: String, value: String) {
        SAPI.headers[key] = value
    }

    fun removeHeader(key: String) {
        SAPI.headers.remove(key)
    }
}
