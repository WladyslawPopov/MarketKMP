package market.engine.fragments.root.main.createOffer

import androidx.lifecycle.SavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.PhotoSave
import market.engine.core.data.items.ToastItem
import market.engine.core.data.states.CategoryState
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.states.CreateOfferContentState
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.nowAsEpochSeconds
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString


class CreateOfferViewModel(
    val catPath : List<Long>?,
    val offerId : Long?,
    val type : CreateOfferType,
    val externalImages : List<String>?,
    val component: CreateOfferComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    val categoryViewModel = component.additionalModels.value.categoryViewModel

    private val _responseGetPage = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "responseGetPage",
        emptyList(),
        ListSerializer(Fields.serializer())
    )
    val responseGetPage = _responseGetPage.state

    private val _responsePostPage = savedStateHandle.getSavedStateFlow<DynamicPayload<OperationResult>>(
        viewModelScope,
        "responsePostPage",
        DynamicPayload(),
        DynamicPayload.serializer(OperationResult.serializer())
    )

    private val _responseCatHistory = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "responseCatHistory",
        emptyList(),
        ListSerializer(Category.serializer())
    )


    private val _selectedDate = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "selectedDate",
        _responseGetPage.value.find { it.key == "future_time" }?.data?.jsonPrimitive?.longOrNull ?: 1,
        Long.serializer()
    )
    val selectedDate = _selectedDate.state

    private val _newOfferId = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "newOfferId",
        1L,
        Long.serializer()
    )
    val newOfferId = _newOfferId.state
    
    val searchData = categoryViewModel.searchData


    private val _isEditCat = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "isEditCat",
        searchData.value.searchCategoryID,
        Long.serializer()
    )
    val isEditCat = _isEditCat.state

    @OptIn(ExperimentalDecomposeApi::class)
    val photoTempViewModel = (component as JetpackComponentContext).viewModel("createOfferPhotoTempViewModel") {
        PhotoTempViewModel(type, savedStateHandle)
    }

    val createOfferContentState : StateFlow<CreateOfferContentState> = combine(
        _responseGetPage.state,
        _responseCatHistory.state
    )
    { dynamicPayload, catHistory ->
        CreateOfferContentState(
            appBarState = SimpleAppBarData(
                onBackClick = {
                    component.onBackClicked()
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = {
                            refresh()
                        }
                    )
                )
            ),
            catHistory = catHistory,
            categoryState = CategoryState(
                categoryViewModel = categoryViewModel
            ),
            textState = dynamicPayload.find { it.key == "title" }?.data?.jsonPrimitive?.content ?: ""
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        CreateOfferContentState(
            categoryState = CategoryState(
                categoryViewModel = categoryViewModel
            )
        )
    )

    init {
        when(type){
            CreateOfferType.CREATE -> {
                categoryViewModel.initialize()
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
        viewModelScope.launch {
            // update params after category change
            if (isEditCat.value != 1L && type != CreateOfferType.CREATE) {
                val newFields = updateParams(isEditCat.value)

                _responseGetPage.update { page ->
                    buildList {
                        page.filterNot { it.key.toString().contains("par_") }.map {
                            add(
                                if(it.key == "category_id"){
                                    it.copy(data = JsonPrimitive(isEditCat.value))
                                }else{
                                    it.copy()
                                }
                            )
                        }
                        addAll(newFields)
                    }
                }
            }

            getCategoriesHistory(isEditCat.value)

            val url = when (type) {
                CreateOfferType.CREATE -> "categories/${isEditCat.value}/operations/create_offer"
                CreateOfferType.EDIT -> "offers/$offerId/operations/edit_offer"
                CreateOfferType.COPY -> "offers/$offerId/operations/copy_offer"
                CreateOfferType.COPY_WITHOUT_IMAGE -> "offers/$offerId/operations/copy_offer_without_old_photo"
                CreateOfferType.COPY_PROTOTYPE -> "offers/$offerId/operations/copy_offer_from_prototype"
            }

            getPage(url)
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

                    val serializer = DynamicPayload.serializer(OperationResult.serializer())
                    val payload: DynamicPayload<OperationResult> =
                        deserializePayload(response.payload, serializer)

                    payload.fields.find {
                        it.key == "future_time"
                    }?.let { field ->
                        if (
                            field.data != null &&
                            (field.data?.jsonPrimitive?.longOrNull ?: 1) >
                            nowAsEpochSeconds()
                        ) {
                            payload.fields.find {
                                it.key == "session_start"
                            }?.let { it.data = JsonPrimitive(2) }
                            _selectedDate.value = field.data?.jsonPrimitive?.longOrNull ?: 1
                        }
                    }

                    val tempPhotos = mutableListOf<PhotoSave>()

                    when (type) {
                        CreateOfferType.EDIT, CreateOfferType.COPY -> {
                            val photos =
                                payload.fields.filter { it.key?.contains("photo_") == true }

                            photos.forEach { field ->
                                if (field.links != null) {
                                    tempPhotos.add(
                                        PhotoSave(
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
                                        PhotoSave(
                                            url = it
                                        )
                                    )
                                }
                                photoTempViewModel.setImages(tempPhotos.toList())
                            }
                        }
                    }

                    val categoryID =
                        payload.fields.find { it.key == "category_id" }?.data?.jsonPrimitive?.longOrNull

                    if (categoryID != null) {
                        _isEditCat.value = categoryID
                        categoryViewModel.updateFromSearchData(
                            SD(
                                searchCategoryID = categoryID,
                                searchCategoryName = "",
                                searchParentID = categoryID,
                                searchIsLeaf = true
                            )
                        )
                    }

                    _responseGetPage.value = payload.fields

                } catch (_: Exception){
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

    suspend fun updateParams(categoryID: Long?) : List<Fields> {
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

                    return newFields
                } catch (_: Exception) {
                    throw ServerErrorException(
                        errorCode = res.errorCode.toString(),
                        humanMessage = res.errorCode.toString()
                    )
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
                return emptyList()
            } catch (exception: Exception) {
                onError(
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
                return emptyList()
            }
        } else {
            return emptyList()
        }
    }

    fun postPage() {
        val body = createJsonBody()
        val url = when (type) {
            CreateOfferType.CREATE -> {
                "categories/${isEditCat.value}/operations/create_offer"
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
                            _responseGetPage.value.find { it.key == "title" }?.data?.jsonPrimitive?.content
                                ?: ""
                        val loc =
                            _responseGetPage.value.find { it.key == "location" }?.data?.jsonPrimitive?.content
                                ?: ""

                        val eventParams = mapOf(
                            "lot_id" to offerId,
                            "lot_name" to title,
                            "lot_city" to loc,
                            "lot_category" to "${isEditCat.value}",
                            "seller_id" to UserData.userInfo?.id
                        )

                        if (type != CreateOfferType.EDIT) {
                            _newOfferId.value = payload.body?.jsonPrimitive?.longOrNull ?: 1
                            analyticsHelper.reportEvent("added_offer_success", eventParams)
                        } else {
                            analyticsHelper.reportEvent("edit_offer_success", eventParams)
                        }

                        _responsePostPage.value = payload

                        if (type == CreateOfferType.EDIT) {
                            categoryViewModel.resetToRoot()
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
                        _responseGetPage.update { currentPayload ->
                            payload.recipe?.fields ?: currentPayload
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
            } catch (_: Exception) { }
        }
    }

    fun setSelectData(data: Long = 1) {
        _responseGetPage.update { page ->
            page.map {
                if (it.key == "future_time") {
                    it.copy(data = JsonPrimitive(data))
                } else it.copy()
            }
        }
        _selectedDate.value = data
    }

    fun setDescription(description: String) {
        val text = KsoupEntities.decodeHtml(description)
        _responseGetPage.update { page ->
            page.map {
                if(it.key == "description"){
                    it.copy(data = JsonPrimitive(text) )
                } else it.copy()
            }
        }
    }

    fun openCategory() {
        categoryViewModel.initialize()
        _isEditCat.value = 1L
    }

    fun closeCategory() {
        _isEditCat.value = searchData.value.searchCategoryID
    }

    fun createJsonBody() : JsonObject {
        val fields = _responseGetPage.value.filter { it.data != null }
        val selectedDate = selectedDate.value
        val deleteImages = photoTempViewModel.deleteImages.value
        val images = photoTempViewModel.responseImages.value

        return buildJsonObject {
            fields.forEach { field ->
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

                    "session_start" -> {
                        if (selectedDate <= 1L) {
                            put(field.key, field.data ?: JsonPrimitive("null"))
                        }
                    }
                    "future_time" ->{
                        if (selectedDate > 1L) {
                            put(field.key, field.data ?: JsonPrimitive("null"))
                        }else{
                            if (type != CreateOfferType.CREATE && type != CreateOfferType.EDIT) {
                                put(field.key, field.data ?: JsonPrimitive("null"))
                            }
                        }
                    }

                    else -> {
                        put(field.key ?: "", field.data ?: JsonPrimitive("null"))
                    }
                }
            }

            when (type) {
                CreateOfferType.EDIT, CreateOfferType.COPY -> {
                    if(deleteImages.isNotEmpty()) {
                        put("delete_images", JsonArray(deleteImages))
                    }
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

    fun setNewFiles(field: Fields) {
        _responseGetPage.update { page ->
            page.map { oldField ->
                if (oldField.key == field.key){
                    field.copy()
                }else{
                    oldField.copy()
                }
            }
        }
    }
}
