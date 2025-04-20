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
import kotlinx.serialization.json.buildJsonObject
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
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.SettingsRepository
import market.engine.core.data.types.ToastType
import market.engine.core.network.ServerResponse
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.DeliveryAddress
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.ListItem
import market.engine.core.network.networkObjects.Operations
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
    val catBack = mutableStateOf(false)
    val openFiltersCat = mutableStateOf(false)
    var activeFiltersType : MutableState<String> = mutableStateOf("")
    var bottomSheetState : MutableState<BottomSheetValue> = mutableStateOf(BottomSheetValue.Collapsed)
    var scrollItem : MutableState<Int> = mutableStateOf(0)
    var offsetScrollItem : MutableState<Int> = mutableStateOf(0)

    val apiService = getKoin().get<APIService>()
    val userRepository: UserRepository = getKoin().get()
    val offerOperations : OfferOperations = getKoin().get()
    val categoryOperations : CategoryOperations = getKoin().get()
    val conversationsOperations: ConversationsOperations = getKoin().get()

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    val userOperations : UserOperations = getKoin().get()

    private val _errorMessage = MutableStateFlow(ServerErrorException())
    val errorMessage: StateFlow<ServerErrorException> = _errorMessage.asStateFlow()

    val toastItem = mutableStateOf(ToastItem(message = "", type = ToastType.WARNING, isVisible = false))

    private val _isShowProgress = MutableStateFlow(false)
    val isShowProgress: StateFlow<Boolean> = _isShowProgress.asStateFlow()

    val viewModelScope = CoroutineScope(Dispatchers.Default)

    val settings : SettingsRepository = getKoin().get()

    val catDef = mutableStateOf("")

    init {
        viewModelScope.launch {
            catDef.value = getString(strings.categoryMain)
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
                userRepository.updateToken()
                userRepository.updateUserInfo()
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
                    hashMapOf("list_type" to list)
                ).success?.body?.data?.find { it.id == id }
            }

            if (found != null) {
                check.add(list)
            }
        }
        return check
    }

    suspend fun getDeliveryCards(): List<DeliveryAddress>? {
        try {
            val response = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsAddressCards(UserData.login)
            }

            val payload = response.success
            val err = response.error

            if (payload?.body?.addressCards != null) {
                return payload.body.addressCards
            } else {
                throw err ?: ServerErrorException(errorCode = "Error", humanMessage = "")
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            return null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            return null
        }
    }

    suspend fun getDeliveryFields(): ArrayList<Fields>? {
        val res = withContext(Dispatchers.IO) {
            userOperations.getUsersOperationsSetAddressCards(UserData.login)
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
            val res = withContext(Dispatchers.IO) {
                val b = HashMap<String, Long>()
                b["id_as_ts"] = card.id
                userOperations.postUsersOperationsSetAddressCardsDefault(UserData.login, b)
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    if (buffer.success) {

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        delay(2000)

                        onSuccess()
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = buffer.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                    }
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    fun updateDeleteCard(card: DeliveryAddress, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                val b = HashMap<String, Long>()
                b["id_as_ts"] = card.id
                userOperations.postUsersOperationsDeleteAddressCards(UserData.login, b)
            }
            withContext(Dispatchers.Main) {
                val buffer = res.success
                val err = res.error

                if (buffer != null) {
                    if (buffer.success) {
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        onSuccess()
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = buffer.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                    }
                } else {
                    err?.let { onError(it) }
                }
            }
        }
    }

    fun saveDeliveryCard(deliveryFields: List<Fields>, cardId: Long?, onSaved: () -> Unit, onError: (List<Fields>) -> Unit) {
        setLoading(true)
        viewModelScope.launch {
            val jsonBody = buildJsonObject {
                deliveryFields.forEach { field ->
                    when (field.widgetType) {
                        "input" -> {
                            if (field.data != null) {
                                put(field.key.toString(), field.data!!)
                            }
                        }

                        "hidden" -> {
                            if (cardId != null) {
                                put(field.key.toString(), JsonPrimitive(cardId))
                            }
                        }

                        else -> {}
                    }
                }
            }

            val res = withContext(Dispatchers.IO) {
                userOperations.postUsersOperationsSetAddressCards(UserData.login, jsonBody)
            }

            withContext(Dispatchers.Main) {
                val payload = res.success
                val err = res.error

                if (payload != null) {
                    if (payload.status == "operation_success") {

                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to jsonBody
                        )
                        analyticsHelper.reportEvent(
                            "save_address_cards_success",
                            eventParameters
                        )

                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        delay(2000)
                        onSaved()
                    } else {
                        val eventParameters = mapOf(
                            "user_id" to UserData.login,
                            "profile_source" to "settings",
                            "body" to jsonBody
                        )
                        analyticsHelper.reportEvent(
                            "save_address_cards_failed",
                            eventParameters
                        )
                        payload.recipe?.fields?.let { onError(it) }

                        showToast(
                            errorToastItem.copy(
                                message = getString(strings.operationFailed)
                            )
                        )
                    }
                } else {
                    err?.let { onError(it) }
                }

                setLoading(false)
            }
        }
    }

    fun getBlocList(type : String, onSuccess: (ArrayList<ListItem>) -> Unit) {
        viewModelScope.launch {
            val res =  withContext(Dispatchers.IO){
                val body = HashMap<String,String>()
                when(type){
                    "add_to_seller_blacklist" -> {
                        body["list_type"] = "blacklist_sellers"
                    }
                    "add_to_buyer_blacklist" -> {
                        body["list_type"] = "blacklist_buyers"
                    }
                    "add_to_whitelist" -> {
                        body["list_type"] = "whitelist_buyers"
                    }
                }
                userOperations.getUsersOperationsGetUserList(UserData.login, body)
            }

            withContext(Dispatchers.Main) {
                val buffer = res.success
                val resErr = res.error

                if (buffer != null) {
                    if(!buffer.body?.data.isNullOrEmpty()) {
                        onSuccess(buffer.body?.data!!)
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
                userOperations.postUsersOperationRemoveFromList(UserData.login, body, list)
            }

            withContext(Dispatchers.Main) {
                if (res.success == true){
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
                userOperations.postUsersOperationsSetWatermarkEnabled(UserData.login)
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
                userOperations.postUsersOperationsSetWatermarkDisabled(UserData.login)
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
                userOperations.postUsersOperationsSetBlockRatingEnabled(UserData.login)
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
                userOperations.postUsersOperationsSetBlockRatingDisabled(UserData.login)
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

    fun cancelAllBids(offerId: Long, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val body = HashMap<String, String>()
            body["comment"] = comment

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
                offerOperations.postOfferOperationsCancelAllBids(
                    offerId,
                    body
                )
            }

            val payload = res.success
            val resErr = res.error

            withContext(Dispatchers.Main) {
                if (payload != null) {
                    if (payload.success) {
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

                withContext(Dispatchers.Main) {
                    if (res != null) {
                        onSuccess(res)
                    }else{
                        error()
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


    fun getNotesField(
        offerId : Long,
        type: String,
        onSuccess: (
            fields: ArrayList<Fields>
        ) -> Unit
    ) {
        viewModelScope.launch {
            val postRes = withContext(Dispatchers.IO) {
                when (type) {
                    "create_note" -> {
                        offerOperations.getOfferOperationsCreateNote(offerId)
                    }

                    "edit_note" -> {
                        offerOperations.getOfferOperationsEditNote(offerId)
                    }

                    else -> {
                        null
                    }
                }
            }

            val bufPost = postRes?.success
            val err = postRes?.error
            withContext(Dispatchers.Main) {
                if (bufPost != null) {
                    onSuccess(bufPost)
                }else{
                    if (err != null) {
                        onError(err)
                    }
                }
            }
        }
    }

    fun getOfferListFieldForOffer(
        offerId : Long,
        type: String,
        onSuccess: (
            fields: ArrayList<Fields>
        ) -> Unit
    ) {
        viewModelScope.launch {
            val postRes = withContext(Dispatchers.IO) {
                when (type) {
                    "add_to_list" -> {
                        offerOperations.getOfferOperationsAddToList(offerId)
                    }

                    "remove_from_list" -> {
                        offerOperations.getOfferOperationsRemoveToList(offerId)
                    }

                    else -> {
                        null
                    }
                }
            }

            val bufPost = postRes?.success
            val err = postRes?.error
            withContext(Dispatchers.Main) {
                if (bufPost != null) {
                    onSuccess(bufPost)
                }else{
                    if (err != null) {
                        onError(err)
                    }
                }
            }
        }
    }

    fun postOfferListFieldForOffer(
        offerId : Long,
        type : String,
        body : HashMap<String, JsonElement>,
        onSuccess: () -> Unit,
        onError: (ArrayList<Fields>) -> Unit
    ) {
        viewModelScope.launch {
            val buf = withContext(Dispatchers.IO) {
                when(type){
                    "add_to_list" -> {
                        offerOperations.postOfferOperationsAddOfferToList(offerId,body)
                    }
                    "remove_from_list" -> {
                        offerOperations.postOfferOperationsRemoveOfferToList(offerId,body)
                    }
                    else -> {
                        null
                    }
                }
            }

            val res = buf?.success

            withContext(Dispatchers.Main) {
                if (res != null) {
                    if (res.status == "operation_success") {
                        analyticsHelper.reportEvent(
                            "${type}_success",
                            eventParameters = mapOf(
                                "lot_id" to offerId,
                                "body" to body
                            )
                        )
                        showToast(
                            ToastItem(
                                isVisible = true,
                                type = ToastType.SUCCESS,
                                message = getString(strings.operationSuccess)
                            )
                        )
                        onSuccess()
                    } else {
                        res.recipe?.fields?.let { onError(it) }
                    }
                }
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

    fun postNotes(
        offerId : Long,
        type : String,
        body : HashMap<String, JsonElement>,
        onSuccess: () -> Unit,
        onError: (ArrayList<Fields>) -> Unit
    ) {
        viewModelScope.launch {
            val buf = withContext(Dispatchers.IO) {
                when(type){
                    "create_note" -> {
                        offerOperations.postOfferOperationsCreateNote(offerId,body)
                    }
                    "edit_note" -> {
                        offerOperations.postOfferOperationsEditNote(offerId,body)
                    }
                    else -> {
                        null
                    }
                }
            }

            val res = buf?.success

            withContext(Dispatchers.Main) {
                if (res != null) {
                    if (res.status == "operation_success") {
                        analyticsHelper.reportEvent(
                            "${type}_note_success",
                            eventParameters = mapOf(
                                "lot_id" to offerId,
                                "body" to body
                            )
                        )
                        showToast(
                            ToastItem(
                                isVisible = true,
                                type = ToastType.SUCCESS,
                                message = getString(strings.operationSuccess)
                            )
                        )
                        onSuccess()
                    } else {
                        res.recipe?.fields?.let { onError(it) }
                    }
                }
            }
        }
    }

    fun deleteNote(offerId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                offerOperations.postOfferOperationsDeleteNote(offerId)
            }
            withContext(Dispatchers.Main) {
                if (res.success != null) {
                    if (res.success?.success == true) {
                        showToast(
                            successToastItem.copy(
                                message = getString(strings.operationSuccess)
                            )
                        )
                        delay(2000)
                        onSuccess()
                    }else {
                        showToast(
                            errorToastItem.copy(
                                message = res.success?.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                    }
                }
            }
        }
    }

    fun getOfferOperations(offerId: Long, onSuccess: (List<Operations>) -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                offerOperations.getOperationsOffer(offerId)
            }
            val buf = res.success
            val err = res.error
            withContext(Dispatchers.Main) {
                if (buf != null) {
                    val filtered = res.success?.filter {
                        it.id in listOf(
                            "watch",
                            "unwatch",
                            "create_note",
                            "edit_note",
                            "delete_note",
                            "prolong_offer",
                            "activate_offer_for_future",
                            "activate_offer",
                            "set_anti_sniper",
                            "unset_anti_sniper",
                            "delete_offer",
                            "cancel_all_bids",
                            "remove_bids_of_users",
                            "copy_offer_without_old_photo",
                            "finalize_session",
                            "edit_offer",
                            "copy_offer",
                            "act_on_proposal",
                            "make_proposal",
                            "cancel_all_bids",
                            "remove_bids_of_users",
                            "remove_from_list",
                            "add_to_list"
                        )
                    }
                    onSuccess(filtered ?: emptyList())
                }else{
                    if (err != null){
                        onError(err)
                    }
                }
            }
        }
    }

    fun resetScroll() {
        scrollItem.value = 0
        offsetScrollItem.value = 0
    }
}
