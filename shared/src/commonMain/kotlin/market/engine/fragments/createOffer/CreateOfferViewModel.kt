package market.engine.fragments.createOffer

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.fragments.base.BaseViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateOfferViewModel(private val apiService: APIService) : BaseViewModel() {

    private var _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseDynamicPayload : StateFlow<DynamicPayload<OperationResult>?> = _responseGetPage.asStateFlow()

    private var _responsePostPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    val responseCreateOffer : StateFlow<DynamicPayload<OperationResult>?> = _responsePostPage.asStateFlow()

    private val _responseImages = MutableStateFlow<List<PhotoTemp>>(emptyList())
    val responseImages: StateFlow<List<PhotoTemp>> = _responseImages.asStateFlow()

    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())
    val responseCatHistory: StateFlow<List<Category>> = _responseCatHistory.asStateFlow()

    val positionList = mutableStateOf(0)

    fun getImages(pickImagesRaw : List<PhotoTemp>) {
        viewModelScope.launch {
            _responseImages.value = buildList {
                addAll(_responseImages.value)
                pickImagesRaw.forEach {
                    if (size < MAX_IMAGE_COUNT) {
                        add(it)
                    }
                }
            }
        }
    }

    fun setImages(images: List<PhotoTemp>) {
        _responseImages.value = images
    }

    fun getPage(url: String) {
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

    fun updateParams(categoryID: Long?) {
        viewModelScope.launch {
            if (categoryID != null) {
                try {
                    val res = withContext(Dispatchers.IO) {
                        apiService.getPage("categories/$categoryID/operations/create_offer")
                    }
                    try {
                        val serializer = DynamicPayload.serializer(OperationResult.serializer())
                        val payload: DynamicPayload<OperationResult> =
                            deserializePayload(res.payload, serializer)


                        val newFields = payload.fields.filter { it.key.toString().contains("par_") }

                        withContext(Dispatchers.Main) {
                            _responseGetPage.value = _responseGetPage.value?.let { currentPayload ->
                                val updatedFields = currentPayload.fields.filterNot {
                                    it.key.toString().contains("par_")
                                }
                                val mergedFields = ArrayList(updatedFields + newFields)
                                currentPayload.copy(fields = mergedFields)
                            }
                        }
                    } catch (e: Exception) {
                        throw ServerErrorException(
                            errorCode = res.errorCode.toString(),
                            humanMessage = res.errorCode.toString()
                        )
                    }
                } catch (exception: ServerErrorException) {
                    onError(exception)
                } catch (exception: Exception) {
                    onError(
                        ServerErrorException(
                            errorCode = exception.message.toString(),
                            humanMessage = exception.message.toString()
                        )
                    )
                }
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
                            if (payload.status == "operation_success"){
                                _responsePostPage.value = payload
                            }else{
                                _responseGetPage.value = _responseGetPage.value?.let { currentPayload ->
                                    val updatedFields = arrayListOf<Fields>()
                                    updatedFields.addAll(payload.recipe?.fields ?: currentPayload.fields)
                                    currentPayload.copy(fields = updatedFields)
                                }
                            }
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

    fun getCategoriesHistory(catPath: List<Long>) {
        viewModelScope.launch {
            try {
                val categories = withContext(Dispatchers.IO) {
                    catPath.reversed().mapNotNull { id ->
                        categoryOperations.getCategoryInfo(id).success
                    }
                }
                _responseCatHistory.value = categories
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching categories", ""))
            }
        }
    }
}
