package market.engine.fragments.root.main.createOffer

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import market.engine.common.Platform
import market.engine.common.getFileUpload
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.items.ToastItem
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class CreateOfferContentState(
    val appBarState: SimpleAppBarData = SimpleAppBarData(),
    val dynamicPayloadState: DynamicPayload<OperationResult>? = null,
    val categoryState: CategoryState = CategoryState(),

    val catHistory : List<Category> = emptyList(),
    val deleteImages : List<JsonPrimitive> = emptyList(),

    val textState : String = "",
    val futureTime : Long = 1L,
    val selectedDate : Long? = null,

    val firstDynamicContent : List<String> = listOf(
        "title",
        "saletype",
    ),
    val secondDynamicContent : List<String> = listOf("params"),
    val thirdDynamicContent : List<String> = listOf(
        "length_in_days",
        "quantity",
        "relisting_mode",
        "whopaysfordelivery",
        "region",
        "freelocation",
        "paymentmethods",
        "dealtype",
        "deliverymethods",
    ),
    val endDynamicContent : List<String> = listOf(
        "images",
        "session_start",
        "description",
    )
)

class PhotoTempViewModel(val type: CreateOfferType) : CoreViewModel(){
    private val _deleteImages = MutableStateFlow<List<JsonPrimitive>>(emptyList())
    val deleteImages = _deleteImages.asStateFlow()

    private val _responseImages = MutableStateFlow<List<PhotoTemp>>(emptyList())
    val responseImages = _responseImages.asStateFlow()

    @OptIn(ExperimentalUuidApi::class)
    fun getImages(pickImagesRaw : PlatformFiles) {
        viewModelScope.launch {
            val photos = pickImagesRaw.map { file ->
                PhotoTemp(
                    file = file,
                    id = Uuid.random().toString(),
                    uri = file.path
                )
            }
            _responseImages.value = buildList {
                addAll(_responseImages.value)
                photos.forEach {
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


    fun setDeleteImages(item : PhotoTemp) {
        if (type == CreateOfferType.EDIT || type == CreateOfferType.COPY) {
            if (item.url != null && item.id != null) {
                _deleteImages.value += JsonPrimitive(item.id!!.last().toString())
            }
        }

        _responseImages.update {
            val newList = it.toMutableList()
            newList.remove(item)
            newList
        }
    }

    fun rotatePhoto(item : PhotoTemp) {
        _responseImages.update { list ->
            list.map {
                if (it.id == item.id) {
                    item.copy()
                } else {
                    it.copy()
                }
            }
        }
    }

    fun openPhoto(item : PhotoTemp) {
        val i = item
        printLogD("Open photo", i.toString())
    }

    fun uploadPhotoTemp(item : PhotoTemp, onSuccess : (PhotoTemp) -> Unit) {
        viewModelScope.launch {
            val res = uploadFile(item)

            if (res.success != null) {
                delay(1000)

                if (res.success?.tempId?.isNotBlank() == true) {
                    item.uri = res.success?.uri
                    item.tempId = res.success?.tempId

                    _responseImages.update { list ->
                        list.map {
                            if (it.id == item.id) {
                                item.copy()
                            } else {
                                it.copy()
                            }
                        }
                    }
                }else{
                    showToast(
                        errorToastItem.copy(
                            message = res.error?.humanMessage ?: getString(strings.failureUploadPhoto)
                        )
                    )
                    setDeleteImages(item)
                }
                withContext(Dispatchers.Main){
                    onSuccess(item)
                }
            } else {
                showToast(
                    errorToastItem.copy(
                        message = res.error?.humanMessage ?: getString(strings.failureUploadPhoto)
                    )
                )
            }
        }
    }

    private suspend fun uploadFile(photoTemp: PhotoTemp) : ServerResponse<PhotoTemp> {
        try {
            val res = withContext(Dispatchers.IO) {
                getFileUpload(photoTemp)
            }

            return withContext(Dispatchers.Main) {
                val cleanedSuccess = res.success?.trimStart('[')?.trimEnd(']')?.replace("\"", "")
                photoTemp.tempId = cleanedSuccess
                ServerResponse(photoTemp)
            }
        } catch (e : ServerErrorException){
            return withContext(Dispatchers.Main) {
                ServerResponse(error = e)
            }
        }catch (e : Exception){
            return withContext(Dispatchers.Main) {
                ServerResponse(error = ServerErrorException(errorCode = e.message ?: ""))
            }
        }
    }
}

class CreateOfferViewModel(
    val catPath : List<Long>?,
    val offerId : Long?,
    val type : CreateOfferType,
    val externalImages : List<String>?,
    val component: CreateOfferComponent
) : CoreViewModel() {

    val categoryViewModel = CategoryViewModel(
        isCreateOffer = true
    )

    private val _responseGetPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    private val _responsePostPage = MutableStateFlow<DynamicPayload<OperationResult>?>(null)
    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())

    private val _isEditCat = MutableStateFlow(false)

    private val _choiceCodeSaleType = MutableStateFlow<Int?>(null)
    val choiceCodeSaleType = _choiceCodeSaleType.asStateFlow()

    private val _selectedDate = MutableStateFlow(_responseGetPage.value?.fields?.find { it.key == "future_time" }?.data?.jsonPrimitive?.longOrNull)
    val selectedDate = _selectedDate.asStateFlow()

    private val _newOfferId = MutableStateFlow<Long?>(null)
    val newOfferId = _newOfferId.asStateFlow()
    
    val searchData = categoryViewModel.searchData

    val photoTempViewModel = PhotoTempViewModel(type)

    val createOfferContentState : StateFlow<CreateOfferContentState> = combine(
        _responseGetPage,
        _responseCatHistory,
        _isEditCat,
    )
    { dynamicPayload, catHistory, openCategory ->
        val tempPhotos: ArrayList<PhotoTemp> = arrayListOf()

        when (type) {
            CreateOfferType.EDIT, CreateOfferType.COPY -> {
                val photos =
                    dynamicPayload?.fields?.filter { it.key?.contains("photo_") == true }
                        ?: emptyList()

                photos.forEach { field ->
                    if (field.links != null) {
                        tempPhotos.add(
                            PhotoTemp(
                                id = field.key,
                                url = field.links.mid?.jsonPrimitive?.content,
                                tempId = ""
                            )
                        )
                    }
                }

                photoTempViewModel.setImages(tempPhotos.toList())
            }

            else -> {
                if (externalImages != null) {
                    externalImages.forEach {
                        tempPhotos.add(
                            PhotoTemp(
                                url = it
                            )
                        )
                    }
                    photoTempViewModel.setImages(tempPhotos.toList())
                }
            }
        }

        dynamicPayload?.fields?.find { it.key == "category_id" }
            ?.let { field ->
                field.data?.jsonPrimitive?.longOrNull?.let {
                    if (categoryViewModel.searchData.value.searchCategoryID == 1L) {
                        categoryViewModel.updateFromSearchData(SD(
                            searchCategoryID = it,
                            searchCategoryName = "",
                            searchParentID = it,
                            searchIsLeaf = true
                        ))
                    }
                }
            }

        if(type == CreateOfferType.CREATE) {
            dynamicPayload?.fields?.find { it.key == "session_start" }?.data =
                if (selectedDate.value != null) {
                    JsonPrimitive(2)
                } else {
                    JsonPrimitive(0)
                }
        }

        CreateOfferContentState(
            appBarState = SimpleAppBarData(
                onBackClick = {
                    component.onBackClicked()
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        onClick = {
                            refresh()
                        }
                    )
                )
            ),
            dynamicPayloadState = dynamicPayload,
            catHistory = catHistory,
            categoryState = CategoryState(
                openCategory = openCategory,
                categoryViewModel = categoryViewModel
            ),
            textState = dynamicPayload?.fields?.find { it.key == "title" }?.data?.jsonPrimitive?.content ?: ""
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        CreateOfferContentState()
    )

    init {
        when(type){
            CreateOfferType.CREATE -> {
                categoryViewModel.initialize()
                _isEditCat.value = searchData.value.searchCategoryID == 1L
                analyticsHelper.reportEvent("add_offer_start", mapOf())
            }
            CreateOfferType.EDIT -> {
                setCatHistory()
                getPage("offers/$offerId/operations/edit_offer")
                analyticsHelper.reportEvent("edit_offer_start", mapOf())
            }
            CreateOfferType.COPY -> {
                setCatHistory()
                getPage("offers/$offerId/operations/copy_offer")
                analyticsHelper.reportEvent("copy_offer_start", mapOf())
            }
            CreateOfferType.COPY_WITHOUT_IMAGE ->{
                setCatHistory()
                getPage("offers/$offerId/operations/copy_offer_without_old_photo")
                analyticsHelper.reportEvent("copy_offer_without_image_start", mapOf())
            }
            CreateOfferType.COPY_PROTOTYPE ->{
                setCatHistory()
                getPage("offers/$offerId/operations/copy_offer_from_prototype")
                analyticsHelper.reportEvent("copy_offer_prototype_start", mapOf())
            }
        }
    }

    fun refreshPage(){
        refresh()

        if(searchData.value.searchCategoryID != 1L) {
            getCategoriesHistory(searchData.value.searchCategoryID)

            // update params after category change
            if (_isEditCat.value) {
                updateParams(searchData.value.searchCategoryID)
                _isEditCat.value = false
            } else {
                val url = when (type) {
                    CreateOfferType.CREATE -> "categories/${searchData.value.searchCategoryID}/operations/create_offer"
                    CreateOfferType.EDIT -> "offers/$offerId/operations/edit_offer"
                    CreateOfferType.COPY -> "offers/$offerId/operations/copy_offer"
                    CreateOfferType.COPY_WITHOUT_IMAGE -> "offers/$offerId/operations/copy_offer_without_old_photo"
                    CreateOfferType.COPY_PROTOTYPE -> "offers/$offerId/operations/copy_offer_from_prototype"
                }
                if (type != CreateOfferType.CREATE) {
                    updateParams(searchData.value.searchCategoryID)
                }
                getPage(url)
            }
        }
    }


    fun setCatHistory() {
        getCategoriesHistory(catPath?.firstOrNull())
    }

    fun getPage(url: String) {
        updateUserInfo()

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
                }catch (_: Exception){
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
                    } catch (_: Exception) {
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

    fun postPage() {
        val body = createJsonBody()
        val url = when (type) {
            CreateOfferType.CREATE -> {
                "categories/${searchData.value.searchCategoryID}/operations/create_offer"
            }

            CreateOfferType.EDIT -> {
                "offers/$offerId/operations/edit_offer"
            }

            CreateOfferType.COPY -> {
                "offers/$offerId/operations/copy_offer"
            }

            CreateOfferType.COPY_WITHOUT_IMAGE -> {
                "offers/$offerId/operations/copy_offer_without_old_photo"
            }

            CreateOfferType.COPY_PROTOTYPE -> {
                "offers/$offerId/operations/copy_offer_from_prototype"
            }
        }
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.postCreateOfferPage(url, body)

                try {
                    val serializer = DynamicPayload.serializer(OperationResult.serializer())
                    val payload: DynamicPayload<OperationResult> =
                        deserializePayload(response.payload, serializer)
                    if (payload.status == "operation_success") {
                        showToast(
                            ToastItem(
                                isVisible = true,
                                message = payload.operationResult?.message ?: getString(
                                    strings.operationSuccess
                                ),
                                type = ToastType.SUCCESS
                            )
                        )

                        val title =
                            _responseGetPage.value?.fields?.find { it.key == "title" }?.data?.jsonPrimitive?.content
                                ?: ""
                        val loc =
                            _responseGetPage.value?.fields?.find { it.key == "location" }?.data?.jsonPrimitive?.content
                                ?: ""

                        val eventParams = mapOf(
                            "lot_id" to offerId,
                            "lot_name" to title,
                            "lot_city" to loc,
                            "lot_category" to "${searchData.value.searchCategoryID}",
                            "seller_id" to UserData.userInfo?.id
                        )

                        if (type != CreateOfferType.EDIT) {
                            _newOfferId.value = payload.body?.jsonPrimitive?.longOrNull
                            analyticsHelper.reportEvent("added_offer_success", eventParams)
                        } else {
                            analyticsHelper.reportEvent("edit_offer_success", eventParams)
                        }

                        _responsePostPage.value = payload

                        if (type == CreateOfferType.EDIT) {
                            delay(1000L)
                            withContext(Dispatchers.Main) {
                                component.onBackClicked()
                            }
                        }
                    } else {
                        val eventParams = mapOf(
                            "error_type" to payload.globalErrorMessage,
                            "seller_id" to UserData.userInfo?.id,
                            "body" to body.toString()
                        )
                        analyticsHelper.reportEvent("added_offer_fail", eventParams)
                        showToast(
                            ToastItem(
                                isVisible = true,
                                message = payload.operationResult?.message ?: getString(
                                    strings.operationFailed
                                ),
                                type = ToastType.ERROR
                            )
                        )
                        _responseGetPage.value =
                            _responseGetPage.value?.let { currentPayload ->
                                val updatedFields = arrayListOf<Fields>()
                                updatedFields.addAll(
                                    payload.recipe?.fields ?: currentPayload.fields
                                )
                                currentPayload.copy(fields = updatedFields)
                            }
                    }
                } catch (_: Exception) {
                    throw ServerErrorException(
                        errorCode = response.errorCode.toString(),
                        humanMessage = response.humanMessage.toString()
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
            } finally {
                setLoading(false)
            }
        }
    }

    fun getCategoriesHistory(catId: Long?) {
        viewModelScope.launch {
            try {
                val categories = withContext(Dispatchers.IO) {
                    val catHistory = arrayListOf<Category>()
                    var cats = categoryOperations.getCategoryInfo(catId).success
                    if (cats != null){
                        catHistory.add(cats)
                        while (cats?.id != 1L){
                            cats = categoryOperations.getCategoryInfo(cats?.parentId).success
                            if (cats != null){
                                catHistory.add(cats)
                            }
                        }
                    }
                    catHistory
                }
                if (categories.isNotEmpty()) {
                    _responseCatHistory.value = categories
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching categories", ""))
            }
        }
    }

    fun setSelectData(data: Long) {
        _responseGetPage.update { page ->
            val date = page?.fields?.map {
                if(it.key == "future_time"){
                    it.copy(data = JsonPrimitive(data) )
                } else it.copy()
            } ?: page?.fields ?: emptyList()

            page?.copy(
                fields = ArrayList(date)
            )
        }
        _selectedDate.value = data
    }

    fun setDescription(description: String) {
        val text = KsoupEntities.decodeHtml(description)
        _responseGetPage.update { page ->
            val date = page?.fields?.map {
                if(it.key == "description"){
                    it.copy(data = JsonPrimitive(text) )
                } else it.copy()
            } ?: page?.fields ?: emptyList()

            page?.copy(
                fields = ArrayList(date)
            )
        }
    }


    fun setChoiceCodeSaleType(code: Int) {
        _choiceCodeSaleType.value = code
    }

    fun openCategory() {
        categoryViewModel.initialize()
        _isEditCat.value = true
    }

    fun closeCategory() {
        _isEditCat.value = false
    }


    fun createJsonBody() : JsonObject {
        val fields = _responseGetPage.value?.fields?.filter { it.data != null }
        val categoryID = searchData.value.searchCategoryID
        val selectedDate = selectedDate.value
        val deleteImages = photoTempViewModel.deleteImages.value
        val images = photoTempViewModel.responseImages.value

        return buildJsonObject {
            fields?.forEach { field ->
                when (field.key) {
                    "deliverymethods" -> {
                        val valuesDelivery = arrayListOf<JsonObject>()
                        field.data?.jsonArray?.forEach { choices ->
                            val deliveryPart = buildJsonObject {
                                put(
                                    "code",
                                    JsonPrimitive(choices.jsonObject["code"]?.jsonPrimitive?.intOrNull)
                                )

                                field.choices?.find {
                                    it.code?.jsonPrimitive?.intOrNull ==
                                            choices.jsonObject["code"]?.jsonPrimitive?.intOrNull
                                }?.extendedFields?.forEach { field ->
                                    if (field.data != null) {
                                        put(
                                            field.key.toString(),
                                            field.data!!.jsonPrimitive
                                        )
                                    }
                                }
                            }
                            valuesDelivery.add(deliveryPart)
                        }
                        put(field.key, JsonArray(valuesDelivery))
                    }

                    "category_id" -> {
                        put(field.key, JsonPrimitive(categoryID))
                    }

                    "session_start" -> {
                        if (selectedDate == null) {
                            put(field.key, field.data ?: JsonPrimitive("null"))
                        }
                    }
                    "future_time" ->{
                        if (selectedDate != null) {
                            put(field.key, field.data ?: JsonPrimitive("null"))
                        }
                    }

                    else -> {
                        put(field.key ?: "", field.data ?: JsonPrimitive("null"))
                    }
                }
            }

            when (type) {
                CreateOfferType.EDIT, CreateOfferType.COPY -> {
                    put("delete_images", JsonArray(deleteImages))
                }

                else -> {}
            }

            val positionArray = buildJsonArray {
                images.forEach { photo ->
                    val listIndex = images.indexOf(photo) + 1
                    if (photo.url != null && photo.tempId?.isBlank() == true) {
                        add(buildJsonObject {
                            put("key", JsonPrimitive(photo.id))
                            put("position", JsonPrimitive(listIndex))
                        })
                    }
                }
            }

            val tempImagesArray = buildJsonArray {
                images.forEach { photo ->
                    val listIndex = images.indexOf(photo) + 1
                    if (photo.tempId?.isNotBlank() == true) {
                        add(buildJsonObject {
                            put("id", JsonPrimitive(photo.tempId))
                            put("rotation", JsonPrimitive(photo.rotate))
                            put("position", JsonPrimitive(listIndex))
                        })
                    }
                }
            }

            if (tempImagesArray.isNotEmpty()) {
                put("temp_images", tempImagesArray)
            }

            if (positionArray.isNotEmpty()) {
                put("position_images", positionArray)
            }
        }
    }
}
