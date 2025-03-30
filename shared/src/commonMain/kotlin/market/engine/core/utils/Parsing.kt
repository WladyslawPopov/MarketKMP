package market.engine.core.utils


import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer

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


