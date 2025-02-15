package market.engine.fragments.base

import androidx.compose.material.BottomSheetValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import market.engine.core.network.ServerErrorException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.AnalyticsFactory
import market.engine.common.getFileUpload
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.items.ToastItem
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.repositories.SettingsRepository
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerResponse
import market.engine.core.network.functions.UserOperations
import market.engine.core.repositories.UserRepository
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

open class BaseViewModel: ViewModel() {
    //select items and updateItem
    var selectItems : MutableList<Long> = mutableStateListOf()
    val updateItemTrigger = mutableStateOf(0)
    val updateItem : MutableState<Long?> = mutableStateOf(null)

    //filters params
    var activeFiltersType : MutableState<String> = mutableStateOf("")
    var bottomSheetState : MutableState<BottomSheetValue> = mutableStateOf(BottomSheetValue.Collapsed)
    var scrollItem : MutableState<Int> = mutableStateOf(0)
    var offsetScrollItem : MutableState<Int> = mutableStateOf(0)

    val apiService = getKoin().get<APIService>()
    val userRepository: UserRepository = getKoin().get()
    val offerOperations : OfferOperations = getKoin().get()
    val categoryOperations : CategoryOperations = getKoin().get()

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val userOperations : UserOperations = getKoin().get()

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    val toastItem = mutableStateOf(ToastItem(message = "", type = ToastType.WARNING, isVisible = false))

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    val settings : SettingsRepository = getKoin().get()

    fun onError(exception: ServerErrorException) {
        _errorMessage.value = exception
    }

    fun setLoading(isLoading: Boolean) {
        _isShowProgress.value = isLoading
    }

    fun showToast(newToast: ToastItem) {
        toastItem.value = newToast
        viewModelScope.launch {
            delay(3000)
            toastItem.value = ToastItem(message = "", type = ToastType.WARNING, isVisible = false)
        }
    }

    fun getCategories(searchData : SD, listingData : LD, withoutCounter : Boolean = false) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!searchData.searchIsLeaf) {
                    val id = searchData.searchCategoryID

                    val response = apiService.getPublicCategories(id)

                    val serializer = Payload.serializer(Category.serializer())
                    val payload: Payload<Category> =
                        deserializePayload(response.payload, serializer)

                    val categories = if (!withoutCounter) {
                        val categoriesWithLotCounts = payload.objects.map { category ->
                            async {
                                val sd = searchData.copy()
                                sd.searchCategoryID = category.id
                                val lotCount = categoryOperations.getTotalCount(
                                    sd, listingData
                                )
                                category.copy(estimatedActiveOffersCount = lotCount.success ?: 0)
                            }
                        }

                        categoriesWithLotCounts.awaitAll()
                            .filter { it.estimatedActiveOffersCount > 0 }
                    } else {
                        payload.objects
                    }

                    _responseCategory.value = categories
                }else{
                    if(activeFiltersType.value == "categories") {
                        val category = onCatBack(searchData.searchParentID ?: 1L)
                        if (category != null) {
                            val sd = searchData.copy(
                                searchCategoryID = category.id,
                                searchCategoryName = category.name,
                                searchParentID = category.parentId,
                                searchIsLeaf = category.isLeaf
                            )
                            getCategories(sd, listingData, withoutCounter)
                        }
                    }
                }

            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            }
            finally {
                setLoading(false)
            }
        }
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateToken()
            userRepository.updateUserInfo()
        }
    }

    suspend fun onCatBack(
        uploadId: Long,
    ) : Category? {
        val response = withContext(Dispatchers.IO) {
            categoryOperations.getCategoryInfo(
                uploadId
            )
        }
        return response.success
    }

    suspend fun getOfferById(offerId: Long) : Offer? {
        return try {
            val response = offerOperations.getOffer(offerId)
            response.success?.let {
                return it
            }
        } catch (_: Exception) {
            null
        }
    }

    fun addToFavorites(offer: Offer, onSuccess: (Boolean) -> Unit) {
        if(UserData.token != "") {
            viewModelScope.launch {
                val buf = if (!offer.isWatchedByMe)
                    offerOperations.postOfferOperationWatch(offer.id)
                else
                    offerOperations.postOfferOperationUnwatch(offer.id)

                val eventParameters = mapOf(
                    "lot_id" to offer.id,
                    "lot_name" to offer.name,
                    "lot_city" to offer.freeLocation,
                    "auc_delivery" to offer.safeDeal,
                    "lot_category" to offer.catpath.firstOrNull(),
                    "seller_id" to offer.sellerData?.id,
                    "lot_price_start" to offer.currentPricePerItem,
                )

                val res = buf.success
                withContext(Dispatchers.Main) {
                    if (res != null && res.success) {
                        if (!offer.isWatchedByMe) {
                            analyticsHelper.reportEvent("offer_watch", eventParameters)
                        } else {
                            analyticsHelper.reportEvent("offer_unwatch", eventParameters)
                        }

                        updateUserInfo()
                        onSuccess(!offer.isWatchedByMe)
                    } else {
                        if (buf.error != null)
                            onError(buf.error!!)
                    }
                }
            }
        }else{
            goToLogin(false)
        }
    }

    fun addNewSubscribe(
        listingData : LD,
        searchData : SD,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = userOperations.getUserOperationsCreateSubscription(UserData.login)

            val eventParameters : ArrayList<Pair<String, Any?>> = arrayListOf(
                "buyer_id" to UserData.login.toString(),
            )
            analyticsHelper.reportEvent("click_subscribe_query", eventParameters.toMap())

            val body = HashMap<String, JsonElement>()
            response.success?.fields?.forEach { field ->
                when(field.key) {
                    "category_id" -> {
                        if (searchData.searchCategoryID != 1L) {
                            body["category_id"] = JsonPrimitive(searchData.searchCategoryID)
                            eventParameters.add("category_id" to searchData.searchCategoryID.toString())
                        }
                    }
                    "offer_scope" -> {
                        body["offer_scope"] = JsonPrimitive(1)
                    }
                    "search_query" -> {
                        if(searchData.searchString != "") {
                            body["search_query"] = JsonPrimitive(searchData.searchString)
                            eventParameters.add("search_query" to searchData.searchString)
                        }
                    }
                    "seller" -> {
                        if(searchData.userSearch) {
                            body["seller"] = JsonPrimitive(searchData.userLogin)
                            eventParameters.add("seller" to searchData.userLogin.toString())
                        }
                    }
                    "saletype" -> {
                        when (listingData.filters.find { it.key == "sale_type" }?.value) {
                            "buynow" -> {
                                body["saletype"] = JsonPrimitive(0)
                            }
                            "auction" -> {
                                body["saletype"] = JsonPrimitive(1)
                            }
                        }
                        eventParameters.add("saletype" to listingData.filters.find { it.key == "sale_type" }?.value.toString())
                    }
                    "region" -> {
                        listingData.filters.find { it.key == "region" }?.value?.let {
                            if (it != "") {
                                body["region"] = JsonPrimitive(it)
                                eventParameters.add("region" to it)
                            }
                        }
                    }
                    "price_from" -> {
                        listingData.filters.find { it.key == "current_price" && it.operation == "gte" }?.value?.let {
                            if (it != "") {
                                body["price_from"] = JsonPrimitive(it)
                                eventParameters.add("price_from" to it)
                            }
                        }
                    }
                    "price_to" -> {
                        listingData.filters.find { it.key == "current_price" && it.operation == "lte" }?.value?.let {
                            if (it != "") {
                                body["price_to"] = JsonPrimitive(it)
                                eventParameters.add("price_to" to it)
                            }
                        }
                    }
                    else ->{
                        if (field.data != null){
                            body[field.key ?: ""] = field.data!!
                        }
                    }
                }
            }

            val res = userOperations.postUserOperationsCreateSubscription(UserData.login, body)

            withContext(Dispatchers.Main) {
                if (res.success?.success == true) {
                    showToast(
                        successToastItem.copy(
                            message = res.success?.humanMessage ?: getString(strings.operationSuccess)
                        )
                    )
                    delay(1000)
                    onSuccess()
                }else {
                    onError(res.success?.humanMessage ?: "")
                }
            }
        }
    }


    fun uploadFile(item : PhotoTemp, onSuccess : (PhotoTemp) -> Unit) {
        viewModelScope.launch {
            val res = uploadFile(item)

            if (res.success != null) {
                onSuccess(res.success!!)
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

    suspend fun checkStatusSeller(id: Long) : ArrayList<String> {
        val lists = listOf("blacklist_sellers", "blacklist_buyers", "whitelist_buyers")
        val check : ArrayList<String> = arrayListOf()
        for (list in lists) {
            val found = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsGetUserList(
                    UserData.login,
                    hashMapOf("list_type" to list)
                ).success?.body?.data?.find { it.id == id }
            }

            if (found != null) {
                check.add(list)
            }
        }
        return check
    }

    fun resetScroll() {
        scrollItem.value = 0
        offsetScrollItem.value = 0
    }
}
