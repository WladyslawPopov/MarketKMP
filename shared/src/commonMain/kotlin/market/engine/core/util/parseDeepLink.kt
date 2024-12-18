package market.engine.core.util

import coil3.Uri
import market.engine.core.data.items.DeepLink


fun parseDeepLink(uri: Uri): DeepLink? {
    return when (uri.path) {
        "/user" -> {
            uri.pathLongId()?.let { DeepLink.User(it) }
        }
        "/listing/offer" -> {
            uri.getQueryParam("flt_prp_owner")?.let {
                if(it.toLongOrNull() != null){
                    DeepLink.Listing(it.toLong())
                }else{
                    null
                }
            }
        }
        "/offer" -> {
            uri.pathLongId()?.let { DeepLink.Offer(it) }
        }
        "/auth" -> {
            val queryParams = uri.parseQueryParameters()
            val clientId = queryParams["client_id"]
            val redirectUri = queryParams["redirect_uri"]
            if (clientId != null && redirectUri != null) {
                DeepLink.Auth(clientId, redirectUri)
            } else {
                DeepLink.Auth()
            }
        }
        "/registration" -> DeepLink.Registration
        else -> null
    }
}

private fun Uri.parseQueryParameters(): Map<String, String> {
    return this.query
        ?.split("&")
        ?.mapNotNull {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        ?.toMap()
        ?: emptyMap()
}

private fun Uri.pathLongId(): Long? {
    val regex = "-i(\\d+)".toRegex()
    return regex.find(toString())?.groups?.get(1)?.value?.toLongOrNull()
}

fun Uri.getQueryParam(param: String): String? {
    val queryPairs = query?.split("&")?.associate {
        val (key, value) = it.split("=")
        key to value
    }
    return queryPairs?.get(param)
}
