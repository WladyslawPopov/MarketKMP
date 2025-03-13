package market.engine.core.utils

import coil3.Uri
import coil3.pathSegments
import coil3.toUri
import market.engine.core.data.items.DeepLink


fun parseDeepLink(fullPath: String): DeepLink? {
    val uri = fullPath.toUri()
    val path = uri.pathSegments.firstOrNull()
    printLogD("DeepLink", path.toString())
    return when (path) {
        "user" -> {
            uri.pathLongId()?.let { DeepLink.GoToUser(it) }
        }
        "listing" -> {
            uri.getQueryParam("flt_prp_owner")?.let {
                if(it.toLongOrNull() != null){
                    DeepLink.GoToListing(it.toLong())
                }else{
                    null
                }
            }
        }
        "offer" -> {
            uri.pathLongId()?.let { DeepLink.GoToOffer(it) }
        }
        "auth" -> {
            val queryParams = uri.parseQueryParameters()
            val clientId = queryParams["client_id"]
            val redirectUri = queryParams["redirect_uri"]
            if (clientId != null && redirectUri != null) {
                DeepLink.GoToAuth(clientId, redirectUri)
            } else {
                DeepLink.GoToAuth()
            }
        }
        "registration" -> DeepLink.GoToRegistration
        "email" ->{
            val queryParams = uri.parseQueryParameters()
            val owner = queryParams["us_id"]?.toLongOrNull()
            val code = queryParams["code"]
            DeepLink.GoToVerification(owner,code, null)
        }
        "password" ->{
            val queryParams = uri.parseQueryParameters()
            val owner = queryParams["us_id"]?.toLongOrNull()
            val code = queryParams["code"]
            DeepLink.GoToDynamicSettings(owner,code,"set_password")
        }
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
