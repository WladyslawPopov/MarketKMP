package market.engine.core.utils


import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.items.DeepLink
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.shared.NotificationsHistory

fun<T> deserializePayload(
    jsonElement: JsonElement?,
    serializer: KSerializer<T>
): T {
    if (jsonElement != null) {
        try {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
            return json.decodeFromJsonElement(serializer, jsonElement)
        } catch (e: Exception) {
            throw ServerErrorException(e.message.toString(), "")
        }
    } else {
        throw ServerErrorException("empty_payload", "empty payload!")
    }
}

fun Offer.getOfferImagePreview(): String {
    return when {
        images?.isNotEmpty() == true -> images?.firstOrNull()?.urls?.small?.content ?: ""
        externalImages?.isNotEmpty() == true -> externalImages.firstOrNull() ?: ""
        externalUrl != null -> externalUrl
        image?.small?.content != null -> image.small.content
        else -> ""
    }
}

fun String.cleanSearchString(): String {
    return this.replace(Regex("[^a-zA-Zа-яА-Я0-9]"), "_")
}

fun provideByType(notificationItem: NotificationsHistory, onProvide : (DeepLink) -> Unit) {
    notificationItem.run {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
        val je = json.parseToJsonElement(data_)

        when (type) {
            "message about offer" -> {
                val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
                if (dialogId != null) {
                    val deepLink = DeepLink.GoToDialog(dialogId, null)
                    onProvide(deepLink)
                }
            }

            "message about order" -> {
                val dialogId = je.jsonObject["dialogId"]?.jsonPrimitive?.content?.toLongOrNull()
                if (dialogId != null) {
                    val deepLink = DeepLink.GoToDialog(dialogId, null)
                    onProvide(deepLink)
                }
            }
        }
    }
}


