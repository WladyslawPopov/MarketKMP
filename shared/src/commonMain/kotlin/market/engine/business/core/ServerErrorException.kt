package application.market.auction_mobile.business.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
class ServerErrorException(var errorCode: String = "", var humanMessage: String = "") : Exception() {

    fun isNotSuccessfulServerResponse(errorBody: String?, errorCode : Int) : ServerErrorException {
        if (errorBody != null) {
            if (errorCode != 500) {
                val element = Json.parseToJsonElement(errorBody).jsonObject
                val errorText = element.jsonObject["error_code"]?.jsonPrimitive?.content ?: ""
                val hm = element.jsonObject["human_message"]?.jsonPrimitive?.content ?: ""
                return ServerErrorException(errorText, hm)
            }
        }
        return ServerErrorException("", "")
    }
}
