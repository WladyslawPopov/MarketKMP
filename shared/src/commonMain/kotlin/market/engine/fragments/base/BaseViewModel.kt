package market.engine.fragments.base

import androidx.compose.material.BottomSheetValue
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
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.ToastItem
import market.engine.core.data.states.ScrollDataState
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.SettingsRepository
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerResponse
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.functions.OperationsMethods
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.AdditionalData
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.ListItem
import market.engine.core.network.networkObjects.Operations
import market.engine.core.network.networkObjects.PayloadExistence
import market.engine.core.repositories.UserRepository
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

open class BaseViewModel: ViewModel() {
    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    //select items and updateItem
    val selectItems : MutableList<Long> = mutableStateListOf()
    val updateItemTrigger = mutableStateOf(0)
    val updateItem = MutableStateFlow<Long?>(null)

    //filters params***
    val catDef = mutableStateOf("")
    val updatePage = MutableStateFlow(0)
    val totalCount = MutableStateFlow(0)
    val scrollState  = MutableStateFlow(ScrollDataState())
    val bottomSheetState = MutableStateFlow(BottomSheetValue.Collapsed)

    val showLogoutDialog = mutableStateOf(false)

    val apiService by lazy {  getKoin().get<APIService>() }
    val userRepository: UserRepository by lazy { getKoin().get() }
    val offerOperations : OfferOperations by lazy { getKoin().get() }
    val orderOperations : OrderOperations by lazy { getKoin().get() }
    val categoryOperations : CategoryOperations by lazy { getKoin().get() }
    val conversationsOperations: ConversationsOperations by lazy { getKoin().get() }
    val subOperations: SubscriptionOperations by lazy { getKoin().get() }
    val operationsMethods: OperationsMethods by lazy { getKoin().get() }
    val userOperations : UserOperations by lazy { getKoin().get() }
    val settings : SettingsRepository by lazy { getKoin().get() }
    val db : MarketDB by lazy { getKoin().get() }

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val toastItem = mutableStateOf(ToastItem(message = "", type = ToastType.WARNING, isVisible = false))

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            try {
                catDef.value = getString(strings.categoryMain)
            }catch (_ : Exception){ }
        }
    }

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

    fun getOperationFields(id: Long,type: String, method: String, onSuccess: (title: String, List<Fields>) -> Unit){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                operationsMethods.getOperationFields(id, type, method)
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error

                if (!res?.fields.isNullOrEmpty()){
                    onSuccess(res.description?:"", res.fields)
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun postOperationFields(
        id: Long,
        type: String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf(),
        onSuccess: () -> Unit,
        errorCallback: (List<Fields>?) -> Unit
    ){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { operationsMethods.postOperationFields(id, type, method, body) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error
                if (res != null) {
                    if (res.operationResult?.result == "ok") {
                        showToast(
                            successToastItem.copy(
                                message = getString(
                                    strings.operationSuccess
                                )
                            )
                        )
                        analyticsHelper.reportEvent(
                            "${type}_success",
                            eventParameters = mapOf(
                                "id" to id,
                            )
                        )

                        onSuccess()
                    } else {
                        analyticsHelper.reportEvent(
                            "${type}_error",
                            eventParameters = mapOf(
                                "id" to id,
                                "body" to body.toString()
                            )
                        )
                        showToast(
                            errorToastItem.copy(
                                message = getString(
                                    strings.operationFailed
                                )
                            )
                        )

                        errorCallback(res.recipe?.fields ?: res.fields)
                    }
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun refresh(){
        updateUserInfo()
        onError(ServerErrorException())
        resetScroll()
    }

    fun postOperationAdditionalData(
        id: Long,
        type: String,
        method: String,
        body: HashMap<String, JsonElement> = hashMapOf(),
        onSuccess: (PayloadExistence<AdditionalData>?) -> Unit
    ){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { operationsMethods.postOperationAdditionalData(id, type, method, body) }
            withContext(Dispatchers.Main) {
                val res = data.success
                val error = data.error
                if (res != null) {
                    if( res.operationResult?.result != null) {
                        showToast(
                            successToastItem.copy(
                                message = res.operationResult.result!!
                            )
                        )
                    }
                    analyticsHelper.reportEvent(
                        type,
                        eventParameters = mapOf(
                            "id" to id,
                        )
                    )

                    onSuccess(res)
                }else{
                    if (error != null)
                        onError(error)
                }
            }
        }
    }

    fun getOfferOperations(
        offerId: Long,
        tag : String = "default",
        onSuccess: (List<Operations>) -> Unit
    ) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                offerOperations.getOperationsOffer(offerId, tag)
            }

            withContext(Dispatchers.Main) {
                val buf = res.success?.filter {
                    it.id !in listOf(
                        "add_description",
                        "cloprec107",
                        "make_discount"
                    )
                }

                if (buf != null) {
                    onSuccess(buf)
                }
            }
        }
    }

    fun getOrderOperations(orderId : Long, onSuccess: (List<Operations>) -> Unit){
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) { orderOperations.getOperationsOrder(orderId) }
            withContext(Dispatchers.Main){
                val buf = res.success?.filter {
                    it.id !in listOf("refund")
                }
                if (buf != null) {
                    onSuccess(buf)
                }
            }
        }
    }

    fun getSubOperations(subId : Long, onSuccess: (List<Operations>) -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) { subOperations.getOperationsSubscription(subId) }
            withContext(Dispatchers.Main) {
                val buf = res.success

                if (buf != null) {
                    onSuccess(buf)
                }
            }
        }
    }

    fun getCategories(
        searchData : SD,
        listingData : LD,
        withoutCounter : Boolean = false,
        onSuccess: (List<Category>) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val id = searchData.searchCategoryID
                val response =
                    withContext(Dispatchers.IO) { apiService.getPublicCategories(id) }

                val serializer = Payload.serializer(Category.serializer())
                val payload: Payload<Category> =
                    deserializePayload(response.payload, serializer)

                if (!withoutCounter) {
                    val category = withContext(Dispatchers.IO) {
                        val categoriesWithLotCounts = payload.objects.map { category ->
                            async {
                                val sd = searchData.copy()
                                sd.searchCategoryID = category.id
                                val lotCount =
                                    categoryOperations.getTotalCount(sd, listingData)
                                category.copy(
                                    estimatedActiveOffersCount = lotCount.success ?: 0
                                )
                            }
                        }
                        categoriesWithLotCounts.awaitAll()
                            .filter { it.estimatedActiveOffersCount > 0 }
                    }

                    withContext(Dispatchers.Main) {
                        onSuccess(category)
                    }
                } else {
                    onSuccess(payload.objects)
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            }
        }
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Unconfined) {
                    userRepository.updateToken()
                    userRepository.updateUserInfo()
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            }
        }
    }

    fun onCatBack(
        uploadId: Long,
        onSuccess: (Category) -> Unit
    ) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                categoryOperations.getCategoryInfo(
                    uploadId
                )
            }
            if (response.success != null){
                onSuccess(response.success!!)
            }
        }
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

    fun addToFavorites(offer : OfferItem, onSuccess: (Boolean) -> Unit) {
        if(UserData.token != "") {
            viewModelScope.launch {
                val buf = withContext(Dispatchers.IO) {
                    operationsMethods.postOperationFields(
                        offer.id,
                        if (offer.isWatchedByMe) "unwatch" else "watch",
                        "offers"
                    )
                }

                val res = buf.success
                withContext(Dispatchers.Main) {
                    if (res != null && res.operationResult?.result == "ok") {
                        val eventParameters = mapOf(
                            "lot_id" to offer.id,
                            "lot_name" to offer.title,
                            "lot_city" to offer.location,
                            "auc_delivery" to offer.safeDeal,
                            "lot_category" to offer.catPath.firstOrNull(),
                            "seller_id" to offer.seller.id,
                            "lot_price_start" to offer.price,
                        )
                        if (!offer.isWatchedByMe) {
                            analyticsHelper.reportEvent("offer_watch", eventParameters)
                        } else {
                            analyticsHelper.reportEvent("offer_unwatch", eventParameters)
                        }

                        updateUserInfo()

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )

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
        errorCallback: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = operationsMethods.getOperationFields(
                UserData.login,
                "create_subscription",
                "users"
            )

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

            val res = operationsMethods.postOperationFields(
                UserData.login,
                "create_subscription",
                "users",
                body
            )

            val buf = res.success
            val err = res.error

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    showToast(
                        successToastItem.copy(
                            message = res.success?.operationResult?.message ?: getString(strings.operationSuccess)
                        )
                    )
                    delay(1000)
                    onSuccess()
                }else {
                    errorCallback(err?.humanMessage ?: "")
                }
            }
        }
    }

    fun uploadPhotoTemp(item : PhotoTemp, onSuccess : (PhotoTemp) -> Unit) {
        viewModelScope.launch {
            val res = uploadFile(item)

            if (res.success != null) {
                delay(1000)
                withContext(Dispatchers.Main){
                    onSuccess(res.success!!)
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

    suspend fun checkStatusSeller(id: Long) : ArrayList<String> {
        val lists = listOf("blacklist_sellers", "blacklist_buyers", "whitelist_buyers")
        val check : ArrayList<String> = arrayListOf()
        for (list in lists) {
            val found = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsGetUserList(
                    UserData.login,
                    hashMapOf("list_type" to JsonPrimitive(list))
                ).success?.body?.data?.find { it.id == id }
            }

            if (found != null) {
                check.add(list)
            }
        }
        return check
    }

    suspend fun getDeliveryFields(): List<Fields>? {
        val res = withContext(Dispatchers.IO) {
            operationsMethods.getOperationFields(
                UserData.login,
                "save_address_cards",
                "users"
            )
        }
        return withContext(Dispatchers.Main){
            val payload = res.success
            val err = res.error

            if (payload != null) {
                return@withContext payload.fields
            } else {
                if (err != null)
                    onError(err)
                return@withContext null
            }
        }
    }

    fun updateDefaultCard(card: DeliveryAddress, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val b = HashMap<String, JsonElement>()
            b["id_as_ts"] = JsonPrimitive(card.id)

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "set_default_address_card",
                    "users",
                    b
                )
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )
                    delay(2000)

                    onSuccess()
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    fun updateDeleteCard(card: DeliveryAddress, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val b = HashMap<String, JsonElement>()
            b["id_as_ts"] = JsonPrimitive(card.id)

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "remove_address_card",
                    "users",
                    b
                )
            }
            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )
                    onSuccess()
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    fun getUnreadNotificationsCount() : Int? {
        val list = db.notificationsHistoryQueries.selectAll(UserData.login).executeAsList()
        return if (list.isEmpty()) null else list.filter { it.isRead < 1 || it.isRead > 1 }.size
    }

    fun getBlocList(type : String, onSuccess: (ArrayList<ListItem>) -> Unit) {
        viewModelScope.launch {
            val res =  withContext(Dispatchers.IO){
                val body = HashMap<String, JsonElement>()
                when(type){
                    "add_to_seller_blacklist" -> {
                        body["list_type"] = JsonPrimitive("blacklist_sellers")
                    }
                    "add_to_buyer_blacklist" -> {
                        body["list_type"] = JsonPrimitive("blacklist_buyers")
                    }
                    "add_to_whitelist" -> {
                        body["list_type"] = JsonPrimitive("whitelist_buyers")
                    }
                }
                userOperations.getUsersOperationsGetUserList(UserData.login, body)
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val resErr = res.error

                if (buffer != null) {
                    if(!buffer.body?.data.isNullOrEmpty()) {
                        onSuccess(buffer.body.data)
                    }
                }else{
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun deleteFromBlocList(type : String, id : Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val list = when(type){
                "add_to_seller_blacklist" -> {
                    "seller_blacklist"
                }
                "add_to_buyer_blacklist" -> {
                    "buyer_blacklist"
                }
                "add_to_whitelist" -> {
                    "whitelist"
                }
                else -> {
                    ""
                }
            }
            val body = HashMap<String, JsonElement>()
            body["identity"] = JsonPrimitive(id)

            val res = withContext(Dispatchers.IO){
                operationsMethods.postOperationFields(
                    UserData.login,
                    "remove_from_$list",
                    "users",
                    body
                )
            }

            withContext(Dispatchers.Main) {
                if (res.success != null){
                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )
                    onSuccess()
                }else{
                    if (res.error != null) {
                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun enabledWatermark(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "enable_watermark",
                    "users"
                )
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("enabled_watermark_success", eventParameters)

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    onSuccess()
                } else {
                    if (res.error != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "human_message" to res.error?.humanMessage,
                            "error_code" to res.error?.errorCode
                        )
                        analyticsHelper.reportEvent("enabled_watermark_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun disabledWatermark(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "disable_watermark",
                    "users"
                )
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("disabled_watermark_success", eventParameters)

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    onSuccess()
                } else {
                    if (res.error != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "human_message" to res.error?.humanMessage,
                            "error_code" to res.error?.errorCode
                        )
                        analyticsHelper.reportEvent("disabled_watermark_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun enabledBlockRating(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "enable_block_rating",
                    "users"
                )
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("enabled_block_rating_success", eventParameters)

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    onSuccess()
                } else {
                    if (res.error != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "human_message" to res.error?.humanMessage,
                            "error_code" to res.error?.errorCode
                        )
                        analyticsHelper.reportEvent("enabled_block_rating_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun disabledBlockRating(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "disable_block_rating",
                    "users"
                )
            }
            withContext(Dispatchers.Main) {
                if (res.success?.status == "operation_success") {
                    val eventParameters = mapOf(
                        "user_id" to UserData.login,
                        "profile_source" to "settings",
                    )
                    analyticsHelper.reportEvent("disabled_block_rating_success", eventParameters)

                    showToast(
                        successToastItem.copy(
                            message = getString(strings.operationSuccess)
                        )
                    )

                    onSuccess()
                } else {
                    if (res.error != null) {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "human_message" to res.error?.humanMessage,
                            "error_code" to res.error?.errorCode
                        )
                        analyticsHelper.reportEvent("disabled_block_rating_failed", eventParameters)

                        onError(res.error!!)
                    }
                }
            }
        }
    }

    fun writeToSeller(offerId : Long, messageText : String, onSuccess: (Long?) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val res = operationsMethods.postOperationAdditionalData(
                offerId,
                "write_to_seller",
                "offers",
                hashMapOf("message" to JsonPrimitive(messageText))
            )
            val buffer1 = res.success
            val error = res.error
            withContext(Dispatchers.Main) {
                if (buffer1 != null) {
                    if (buffer1.operationResult?.result == "ok") {
                        onSuccess(buffer1.body?.toLongOrNull())
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = error?.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                        onSuccess(null)
                    }
                } else {
                    error?.let { onError(it) }
                    onSuccess(null)
                }
            }
        }
    }

    fun cancelAllBids(offerId: Long, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val body = HashMap<String, JsonElement>()
            body["comment"] = JsonPrimitive(comment)

            val eventParameters = mapOf(
                "user_id" to UserData.login,
                "profile_source" to "settings",
                "body" to body
            )
            analyticsHelper.reportEvent(
                "set_cancel_all_bids",
                eventParameters
            )

            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    offerId,
                    "set_cancel_all_bids",
                    "offers",
                    body
                )
            }

            val payload = res.success
            val resErr = res.error

            withContext(Dispatchers.Main) {
                if (payload != null) {
                    if (payload.operationResult?.result == "ok") {
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        delay(2000)
                        onSuccess()
                    }
                } else {
                    if (resErr != null) {
                        onError(resErr)
                    }
                }
            }
        }
    }

    fun getConversation(id : Long, onSuccess: (Conversations) -> Unit, error: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    conversationsOperations.getConversation(id)
                }
                val buf = res.success
                val e = res.error
                withContext(Dispatchers.Main) {
                    if (buf!= null) {
                        onSuccess(res.success!!)
                    }else{
                        error()
                        e?.let { throw it }
                    }
                }
            }catch (e : ServerErrorException){
                onError(e)
            }catch (e : Exception){
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    fun deleteConversation(id : Long, onSuccess : () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                conversationsOperations.postDeleteForInterlocutor(id)
            }

            withContext(Dispatchers.Main) {
                if(res != null){
                    onSuccess()
                }else{
                    showToast(errorToastItem.copy(message = getString(strings.operationFailed)))
                }
            }
        }
    }

    fun markReadConversation(id : Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Unconfined) {
                    conversationsOperations.postMarkAsReadByInterlocutor(id)
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }

    fun getOffersList(onSuccess: (List<FavoriteListItem>) -> Unit) {
        val offersListOperations = OffersListOperations(apiService)
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOffersList() }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (res != null) {
                    val buf = arrayListOf<FavoriteListItem>()
                    buf.addAll(res)
                    onSuccess(res)
                }else{
                    if (data.error != null)
                        onError(data.error!!)
                }
            }
        }
    }

    fun getFieldsCreateBlankOfferList(onSuccess: (title: String, List<Fields>) -> Unit){
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                operationsMethods.getOperationFields(
                    UserData.login,
                    "create_blank_offer_list",
                    "users"
                )
            }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (!res?.fields.isNullOrEmpty()){
                    onSuccess(res.description?:"", res.fields)
                }
            }
        }
    }

    fun deleteNote(offerId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(offerId, "delete_note", "offers")
            }
            withContext(Dispatchers.Main) {
                if (res.success != null) {
                    if (res.success?.operationResult?.result == "ok") {
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        analyticsHelper.reportEvent(
                            "delete_note_success",
                            eventParameters = mapOf(
                                "lot_id" to offerId,
                            )
                        )
                        delay(2000)
                        onSuccess()
                    }else {
                        showToast(
                            errorToastItem.copy(
                                message = res.success?.operationResult?.message ?: getString(strings.operationFailed)
                            )
                        )
                    }
                }
            }
        }
    }

    fun addOfferToBasket(body : HashMap<String, JsonElement>, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                operationsMethods.postOperationFields(
                    UserData.login,
                    "add_item_to_cart",
                    "users",
                    body
                )
            }

            val buffer = res.success
            val error = res.error

            if (buffer != null) {
                updateUserInfo()
                onSuccess(buffer.operationResult?.message ?: getString(strings.operationSuccess))
            } else {
                if (error != null) {
                    onError(error)
                }
            }
        }
    }

    fun resetScroll() {
        scrollState.value = ScrollDataState()
    }
}
