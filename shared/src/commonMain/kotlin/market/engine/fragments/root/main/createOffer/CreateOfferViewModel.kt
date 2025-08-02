package market.engine.fragments.root.main.createOffer

import androidx.lifecycle.SavedStateHandle
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
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.DynamicPayload
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.OperationResult
import market.engine.core.utils.deserializePayload
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import kotlin.collections.plus

data class CreateOfferContentState(
    val appBarState: SimpleAppBarData = SimpleAppBarData(),
    val categoryState: CategoryState,

    val catHistory : List<Category> = emptyList(),
    val textState : String = "",

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


class CreateOfferViewModel(
    val catPath : List<Long>?,
    val offerId : Long?,
    val type : CreateOfferType,
    val externalImages : List<String>?,
    val component: CreateOfferComponent,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle) {

    val categoryViewModel = CategoryViewModel(
        isCreateOffer = true,
        savedStateHandle = savedStateHandle
    )

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

    private val _isEditCat = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "isEditCat",
        false,
        Boolean.serializer()
    )

    private val _selectedDate = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "selectedDate",
        _responseGetPage.value.find { it.key == "future_time" }?.data?.jsonPrimitive?.longOrNull ?: 0,
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

    val photoTempViewModel = PhotoTempViewModel(type, savedStateHandle)

    val createOfferContentState : StateFlow<CreateOfferContentState> = combine(
        _responseGetPage.state,
        _responseCatHistory.state,
        _isEditCat.state,
    )
    { dynamicPayload, catHistory, openCategory ->
        val tempPhotos: ArrayList<PhotoSave> = arrayListOf()

        when (type) {
            CreateOfferType.EDIT, CreateOfferType.COPY -> {
                val photos = dynamicPayload.filter { it.key?.contains("photo_") == true }

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

        dynamicPayload.find { it.key == "category_id" }
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
            dynamicPayload.find { it.key == "session_start" }?.data =
                if (selectedDate.value != 0L) {
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
                openCategory = openCategory,
                categoryViewModel = categoryViewModel
            ),
            textState = dynamicPayload.find { it.key == "title" }?.data?.jsonPrimitive?.content ?: ""
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        CreateOfferContentState(
            categoryState = CategoryState(
                openCategory = _isEditCat.value,
                categoryViewModel = categoryViewModel
            )
        )
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

                        payload.fields.find {
                            it.key == "future_time"
                        }?.let { field ->
                            if(
                                field.data != null &&
                                (field.data?.jsonPrimitive?.longOrNull ?: 1) >
                                (getCurrentDate().toLongOrNull() ?: 1L)
                            ){
                                payload.fields.find {
                                    it.key == "session_start"
                                }?.let { it.data = JsonPrimitive(2) }
                                _selectedDate.value = field.data?.jsonPrimitive?.longOrNull ?: 0
                            }else{
                                payload.fields.find {
                                    it.key == "session_start"
                                }?.let { it.data = JsonPrimitive(0) }
                                setSelectData()
                            }
                        }

                        _responseGetPage.value = payload.fields
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
                            _responseGetPage.update { currentPayload ->
                                val updatedFields = currentPayload.filterNot {
                                    it.key.toString().contains("par_")
                                }
                                updatedFields + newFields
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
                            _responseGetPage.value.find { it.key == "title" }?.data?.jsonPrimitive?.content
                                ?: ""
                        val loc =
                            _responseGetPage.value.find { it.key == "location" }?.data?.jsonPrimitive?.content
                                ?: ""

                        val eventParams = mapOf(
                            "lot_id" to offerId,
                            "lot_name" to title,
                            "lot_city" to loc,
                            "lot_category" to "${searchData.value.searchCategoryID}",
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
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching categories", ""))
            }
        }
    }

    fun setSelectData(data: Long? = null) {
        _responseGetPage.update { page ->
            page.map {
                if (it.key == "future_time") {
                    it.copy(data = JsonPrimitive(data))
                } else it.copy()
            }
        }
        _selectedDate.value = data ?: 0
    }

    fun onBackClicked(onBack: () -> Unit) {
        if(categoryViewModel.searchData.value.searchCategoryID != 1L){
            _isEditCat.value = true
            categoryViewModel.navigateBack()
        }else{
            onBack()
        }
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
        _isEditCat.value = true
    }

    fun closeCategory() {
        _isEditCat.value = false
    }

    fun createJsonBody() : JsonObject {
        val fields = _responseGetPage.value.filter { it.data != null }
        val categoryID = searchData.value.searchCategoryID
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

                    "category_id" -> {
                        put(field.key, JsonPrimitive(categoryID))
                    }

                    "session_start" -> {
                        if (selectedDate != 2L) {
                            put(field.key, field.data ?: JsonPrimitive("null"))
                        }
                    }
                    "future_time" ->{
                        if (selectedDate == 2L) {
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
