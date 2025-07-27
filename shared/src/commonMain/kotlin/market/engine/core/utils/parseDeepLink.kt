package market.engine.core.utils

import coil3.Uri
import coil3.pathSegments
import coil3.toUri
import market.engine.core.data.items.DeepLink

fun parseDeepLink(fullPath: String): DeepLink? {
    return try {
        val uri = fullPath.toUri()
        val path = uri.pathSegments.firstOrNull()
        when(path) {
            "user" -> {
                uri.pathLongId()?.let { DeepLink.GoToUser(it) }
            }
            "listing" -> {
                uri.getQueryParam("flt_prp_owner")?.let {
                    DeepLink.GoToListing(it.toLongOrNull() )
                }
                val (catId, catName) = uri.pathCategoryLongId()
                DeepLink.GoToListing(categoryId = catId, categoryName = catName)
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
    } catch (e: IllegalArgumentException) {
        // Log that the fullPath was invalid
        println("Invalid URI for deep link: $fullPath, Error: ${e.message}")
        null
    } catch (e: Exception) {
        // Catch any other unexpected errors during parsing
        println("Error parsing deep link: $fullPath, Error: ${e.message}")
        e.printStackTrace()
        null
    }
}

private fun Uri.parseQueryParameters(): Map<String, String> {
    return this.query
        ?.split("&")
        ?.mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        ?.toMap()
        ?: emptyMap()
}

private fun Uri.pathLongId(): Long? {
    val regex = "-i(\\d+)".toRegex()
    val string = this.toString()
    val id = regex.find(string)?.groups?.get(1)?.value?.toLongOrNull()
    return id
}

private fun Uri.pathCategoryLongId(): Pair<Long?, String?> {
    val regex = "-(\\d+)".toRegex()
    val string = this.toString()
    val name = pathSegments.getOrNull(
        2
    )
    val id = regex.find(string)?.groups?.get(1)?.value?.toLongOrNull()
    return id to name
}

fun Uri.getQueryParam(param: String): String? {
    val queryPairs = query?.split("&")?.associate {
        val list = it.split("=")
        list.firstOrNull() to list.lastOrNull()
    }
    return queryPairs?.get(param)
}
