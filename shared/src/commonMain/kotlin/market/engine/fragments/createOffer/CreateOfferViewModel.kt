package market.engine.fragments.createOffer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel

class CreateOfferViewModel(private val apiService: APIService) : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseGetPage : StateFlow<DynamicPayload<OperationResult>?> = _responseGetPage.asStateFlow()

    private var _responsePostPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responsePostPage : StateFlow<DynamicPayload<OperationResult>?> = _responsePostPage.asStateFlow()


    fun getPage(url: String, categoryID: Long? = null) {
        viewModelScope.launch {
            try {
                setLoading(true)

                val response = withContext(Dispatchers.IO) {
                    apiService.getPage(url)
                }

                try {
                    withContext(Dispatchers.Main) {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload: DynamicPayload<OperationResult> =
                            deserializePayload(response.payload, serializer)
                        _responseGetPage.value = payload
                    }


                    if (categoryID != null){
                       val res = withContext(Dispatchers.IO) { apiService.getPage("categories/$categoryID/operations/create_offer")}
                        try {
                            withContext(Dispatchers.Main) {
                                val serializer =
                                    DynamicPayload.serializer(OperationResult.serializer())
                                val payload: DynamicPayload<OperationResult> =
                                    deserializePayload(res.payload, serializer)
                                val filteredFields =
                                    payload.fields.filter { it.key.toString().contains("par_") }
                                _responseGetPage.value?.fields?.addAll(filteredFields)
                            }
                        }catch (e: Exception){
                            throw ServerErrorException(errorCode = response.errorCode.toString(), humanMessage = response.errorCode.toString())
                        }
                    }
                }catch (e: Exception){
                    throw ServerErrorException(errorCode = response.errorCode.toString(), humanMessage = response.errorCode.toString())
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            } finally {
                setLoading(false)
            }
        }
    }

    fun postPage(url: String, body: JsonObject) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    setLoading(true)
                    val response = apiService.postCreateOfferPage(url, body)
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        try {
                            val serializer = DynamicPayload.serializer(OperationResult.serializer())
                            val payload : DynamicPayload<OperationResult> = deserializePayload(response.payload, serializer)
                            _responsePostPage.value = payload
                        }catch (e: Exception){
                            throw ServerErrorException(errorCode = response.errorCode.toString(), humanMessage = response.errorCode.toString())
                        }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }
}
