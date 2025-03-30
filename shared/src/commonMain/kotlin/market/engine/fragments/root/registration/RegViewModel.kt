package market.engine.fragments.root.registration

import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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

class RegViewModel : BaseViewModel() {
    private val _responseGetRegFields = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetRegFields: StateFlow<DynamicPayload<OperationResult>?> = _responseGetRegFields.asStateFlow()

    fun getRegFields() {
        setLoading(true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = apiService.getRegistration()
                    withContext(Dispatchers.Main) {
                        try {
                            val serializer = DynamicPayload.serializer(OperationResult.serializer())
                            val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                            _responseGetRegFields.value = payload
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

    fun postRegistration(onSuccess : () -> Unit){
        viewModelScope.launch {
            setLoading(true)
            val body = HashMap<String, JsonElement>()
            responseGetRegFields.value?.fields?.forEach {
                if (it.data != null && it.widgetType != "captcha_image") {
                    body[it.key ?: ""] = it.data!!
                }
            }
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.postRegistration(body)
                }

                withContext(Dispatchers.Main) {
                    try {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload: DynamicPayload<OperationResult> =
                            deserializePayload(response.payload, serializer)

                        if (payload.operationResult?.result == "ok") {
                            val events = mapOf(
                                "login_type" to "email",
                                "body" to body.toString()
                            )
                            analyticsHelper.reportEvent("register_success", events)
                            showToast(
                                successToastItem.copy(
                                    message = payload.operationResult.message
                                        ?: response.humanMessage
                                        ?: getString(strings.operationSuccess)
                                )
                            )
                            onSuccess()
                        } else {
                            _responseGetRegFields.value = _responseGetRegFields.value?.copy(
                                fields = payload.recipe?.fields ?: payload.fields
                            )
                            val events = mapOf(
                                "login_type" to "email",
                                "body" to body.toString()
                            )
                            showToast(
                                errorToastItem.copy(
                                    message = getString(strings.operationFailed)
                                )
                            )
                            analyticsHelper.reportEvent("register_fail", events)
                        }
                    } catch (e: Exception) {
                        throw ServerErrorException(
                            errorCode = e.message.toString(),
                            humanMessage = e.message.toString()
                        )
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
}
