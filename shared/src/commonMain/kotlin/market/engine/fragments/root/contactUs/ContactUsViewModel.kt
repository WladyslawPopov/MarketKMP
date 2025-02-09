package market.engine.fragments.root.contactUs

import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class ContactUsViewModel : BaseViewModel() {
    private val _responseGetFields = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetFields: StateFlow<DynamicPayload<OperationResult>?> = _responseGetFields.asStateFlow()

    fun getFields() {
        setLoading(true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = apiService.getSupServViewModel()
                    withContext(Dispatchers.Main) {
                        try {
                            val serializer = DynamicPayload.serializer(OperationResult.serializer())
                            val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                            _responseGetFields.value = payload
                        }catch (e : Exception){
                            throw ServerErrorException(errorCode = e.message.toString(), humanMessage = e.message.toString())
                        }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message.toString(), ""))
            } finally {
                setLoading(false)
            }
        }
    }

    fun postContactUs(onSuccess : () -> Unit) {
        viewModelScope.launch {
            val body = HashMap<String, JsonElement>()
            responseGetFields.value?.fields?.forEach {
                if (it.data != null && it.widgetType != "captcha_image") {
                    body[it.key ?: ""] = it.data!!
                }
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    setLoading(true)
                    apiService.postSupServViewModel(body)
                }

                withContext(Dispatchers.Main) {
                    setLoading(false)

                    try {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                        if (payload.operationResult?.result == "ok") {
                            val events = mapOf(
                                "body" to body.toString()
                            )
                            analyticsHelper.reportEvent("support_letter_send", events)
                            showToast(
                                successToastItem.copy(
                                    message = getString(strings.operationSuccess)
                                )
                            )
                            delay(2000)
                            onSuccess()
                        }else{
                            _responseGetFields.value = _responseGetFields.value?.copy(
                                fields = payload.recipe?.fields ?: payload.fields
                            )
                            val events = mapOf(
                                "body" to body.toString(),
                                "error_type" to payload.operationResult?.message
                            )
                            showToast(
                                errorToastItem.copy(
                                    message = getString(strings.operationFailed)
                                )
                            )
                            analyticsHelper.reportEvent("support_letter_fail", events)
                        }
                    }catch (e : Exception){
                        throw ServerErrorException(errorCode = e.message.toString(), humanMessage = e.message.toString())
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message.toString(), ""))
            }
        }
    }
}
