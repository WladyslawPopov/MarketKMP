package market.engine.core.network.networkObjects

import kotlinx.serialization.KSerializer
import market.engine.core.network.ServerErrorException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement


@Serializable
data class AppResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("payload") val payload: JsonElement? = null,
    @SerialName("human_message") val humanMessage: String? = null,
)

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


@Serializable
data class Payload<T>(
    @SerialName("objects") val objects: ArrayList<T> = arrayListOf(),
    @SerialName("total_count") val totalCount: Int = 0,
    @SerialName("is_more") val isMore: Boolean = false,
)

@Serializable
data class BodyListPayload<T>(
    @SerialName("operation_result") val operationResult: BodyOperationResult<T>? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("body") val bodyList: ArrayList<T> = arrayListOf(),
)

@Serializable
data class BodyPayload<T>(
    @SerialName("operation_result") val operationResult: BodyOperationResult<T>? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("human_message") val humanMessage: String? = null,
    @SerialName("body") val body: T? = null,
)

@Serializable
data class BodyOperationResult<T>(
    @SerialName("about_me") val aboutMe: String? = null,
    @SerialName("body") val body: ArrayList<T> = arrayListOf(),
)

@Serializable
data class DynamicPayload<T>(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("global_error_message") val globalErrorMessage: String? = null,
    @SerialName("fields") val fields: ArrayList<Fields> = arrayListOf(),
    @SerialName("status") val status: String? = null,
    @SerialName("recipe") val recipe: DynamicPayload<OperationResult>? = null,
    @SerialName("operation_result") val operationResult: T? = null,
    @SerialName("body") val body: JsonElement? = null,
)

@Serializable
data class PayloadExistence<T>(
    @SerialName("status") val status: String? = null,
    @SerialName("operation_result") val operationResult: OperationResultExistence<T>? = null,
    @SerialName("body") val body: String? = null,
)

@Serializable
data class OperationResultExistence<T>(
    @SerialName("result") val result: String? = null,
    @SerialName("additional_data") val additionalData: T? = null,
)
