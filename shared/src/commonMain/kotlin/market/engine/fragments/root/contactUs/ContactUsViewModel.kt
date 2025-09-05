package market.engine.fragments.root.contactUs

import androidx.lifecycle.SavedStateHandle

import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.getFileUpload
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

class ContactUsViewModel(
    val component: ContactUsComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    private val _responseGetFields = savedStateHandle.getSavedStateFlow(
        scope,
        "responseGetFields",
        emptyList(),
        ListSerializer(Fields.serializer()),
    )
    val responseGetFields = _responseGetFields.state

    private val _dataImage = savedStateHandle.getSavedStateFlow(
        scope,
        "dataImage",
        "",
        String.serializer(),
    )
    val dataImage: StateFlow<String> = _dataImage.state

    init {
        getFields()
        analyticsHelper.reportEvent("open_support_form", mapOf())
    }

    fun getFields() {
        setLoading(true)
        scope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) { apiService.getSupServViewModel() }
                try {
                    val serializer = DynamicPayload.serializer(OperationResult.serializer())
                    val payload: DynamicPayload<OperationResult> =
                        deserializePayload(response.payload, serializer)
                    if (UserData.token != "") {
                        payload.fields.find { it.key == "email" }?.data =
                            JsonPrimitive(UserData.userInfo?.email)
                        payload.fields.find { it.key == "name" }?.data =
                            JsonPrimitive(UserData.userInfo?.login)
                    }
                    val selectedType = component.model.value.selectedType

                    if (selectedType == "delete_account") {
                        payload.fields.find { it.key == "variant" }?.data = JsonPrimitive(9)
                    }

                    _responseGetFields.value = payload.fields
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

    fun postContactUs(onSuccess : () -> Unit) {
        scope.launch {
            val body = HashMap<String, JsonElement>()
            responseGetFields.value.forEach {
                if (it.data != null && it.widgetType != "captcha_image") {
                    body[it.key ?: ""] = it.data!!
                }
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    setLoading(true)
                    apiService.postSupServViewModel(body)
                }

                setLoading(false)

                try {
                    val serializer = DynamicPayload.serializer(OperationResult.serializer())
                    val payload: DynamicPayload<OperationResult> =
                        deserializePayload(response.payload, serializer)
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
                    } else {
                        _responseGetFields.value = payload.recipe?.fields ?: payload.fields
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
            }
        }
    }

    fun uploadPhotoTemp(item : PhotoTemp) {
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                uploadFile(item)
            }

            if (res.success != null) {

                val result = res.success!!

                _responseGetFields.update { res ->
                    res.map {
                        if (it.key == "attachment") {
                            it.copy(data = JsonPrimitive(result.tempId))
                        } else {
                            it.copy()
                        }
                    }
                }
                _dataImage.value = item.file?.name ?: ""

            } else {
                showToast(
                    errorToastItem.copy(
                        message = res.error?.humanMessage ?: getString(strings.failureUploadPhoto)
                    )
                )
            }
        }
    }

    fun clearDataImage(){
        _dataImage.value = ""
        _responseGetFields.update { res ->
            res.map {
                if (it.key == "attachment") {
                    it.copy(data = null)
                } else {
                    it.copy()
                }
            }
        }
    }

    fun setNewField(fields : Fields){
        _responseGetFields.update { res ->
            res.map {
                if (it.key == fields.key) {
                    fields.copy()
                } else {
                    it.copy()
                }
            }
        }
    }

    private suspend fun uploadFile(photoTemp: PhotoTemp) : ServerResponse<PhotoTemp> {
        try {
            val res = withContext(Dispatchers.IO) {
                getFileUpload(photoTemp)
            }

            val cleanedSuccess = res.success?.trimStart('[')?.trimEnd(']')?.replace("\"", "")
            photoTemp.tempId = cleanedSuccess
            return ServerResponse(photoTemp)

        } catch (e : ServerErrorException){
            onError(e)
            return ServerResponse(error = e)
        }catch (e : Exception){
            onError(ServerErrorException(e.message ?: "", ""))
            return ServerResponse(error = ServerErrorException(errorCode = e.message ?: ""))
        }
    }
}
