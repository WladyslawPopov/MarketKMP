package market.engine.fragments.root.registration

import androidx.lifecycle.SavedStateHandle

import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

class RegViewModel(savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {
    private val _responseGetRegFields = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetRegFields",
        emptyList(),
        ListSerializer(Fields.serializer())
    )
    val responseGetRegFields= _responseGetRegFields.state

    private val _showSuccessReg = savedStateHandle.getSavedStateFlow(
        scope,
        "showSuccessReg",
        false,
        Boolean.serializer()
    )
    val showSuccessReg = _showSuccessReg.state

    init {
        getRegFields()

        analyticsHelper.reportEvent("view_register_account", mapOf())
    }

    fun getRegFields() {
        setLoading(true)
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = apiService.getRegistration()
                    try {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                        _responseGetRegFields.value = payload.fields
                    }catch (e : Exception){
                        throw ServerErrorException(errorCode = e.message.toString(), humanMessage = e.message.toString())
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

    fun postRegistration(){
        scope.launch {
            setLoading(true)
            val body = HashMap<String, JsonElement>()
            responseGetRegFields.value.forEach {
                if (it.data != null && it.widgetType != "captcha_image") {
                    body[it.key ?: ""] = it.data!!
                }
            }
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.postRegistration(body)
                }

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
                        _showSuccessReg.value = true
                    } else {
                        _responseGetRegFields.value = payload.recipe?.fields ?: payload.fields
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
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message.toString(), ""))
            } finally {
                setLoading(false)
            }
        }
    }

    fun setNewField(field: Fields){
        _responseGetRegFields.update {
            it.map { item ->
                if (item.key == field.key){
                    field.copy()
                } else {
                    item.copy()
                }
            }
        }
    }
}
