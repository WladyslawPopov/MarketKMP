package market.engine.fragments.root.main.offer

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.data.types.OfferStates
import market.engine.core.data.types.ProposalType
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.UserOperations
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.User
import market.engine.core.repositories.OfferBaseViewModel
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.CoreViewModel
import market.engine.shared.marketDb
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.toString

@Serializable
data class OfferViewState(
    val statusList: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val columns: StaggeredGridCells = StaggeredGridCells.Fixed(1),
    val countString : String = "",
    val buyNowCounts : List<String> = emptyList(),
    val dealTypeString : String = "",
    val deliveryMethodString : String = "",
    val paymentMethodString : String = "",

    val isMyOffer: Boolean = false,
    val offerState: OfferStates = OfferStates.ACTIVE
)

class OfferViewModel(
    private val dataBase: marketDb,
    private val component: OfferComponent,
    val offerId : Long = 1,
    val isSnapshot : Boolean = false,
    private val savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle)
{
    private val _responseHistory = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseHistory: StateFlow<List<OfferItem>> = _responseHistory.asStateFlow()
    private val _responseOurChoice = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseOurChoice: StateFlow<List<OfferItem>> = _responseOurChoice.asStateFlow()
    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())
    val responseCatHistory: StateFlow<List<Category>> = _responseCatHistory.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _scrollPosition = MutableStateFlow(0)
    val scrollPosition: StateFlow<Int> = _scrollPosition.asStateFlow()

    val userOperations : UserOperations by lazy { getKoin().get() }
    val offerOperations : OfferOperations by lazy { getKoin().get() }

    private var timerJob: Job? = null
    private var timerBidsJob: Job? = null
    private var eventParameters: Map<String, Any?> = mapOf()

    private val _showDialog = MutableStateFlow("")
    val showDialog = _showDialog.asStateFlow()

    val goToBids = 5

    private val offerRepositoryEvents = OfferRepositoryEventsImpl(component, this)

    val offerBaseViewModel = OfferBaseViewModel(
        offer = Offer(),
        listingData = ListingData(),
        events = offerRepositoryEvents,
        savedStateHandle = savedStateHandle
    )

    private val _responseOfferView: MutableStateFlow<OfferViewState> = MutableStateFlow(
        OfferViewState()
    )

    val responseOfferView: StateFlow<OfferViewState> = _responseOfferView.asStateFlow()

    init {
        refreshPage()
    }

    fun refreshPage() {
        getOffer(offerId, isSnapshot)
        refresh()
    }

    fun editNote(){
        viewModelScope.launch {
            offerBaseViewModel.menuList.value.find { it.id == "edit_note" }?.onClick()
        }
    }

    fun deleteNote(id: Long){
        viewModelScope.launch {
            this@OfferViewModel.deleteNote(id) {
                refreshPage()
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

    fun getOffer(offerId: Long, isSnapshot: Boolean = false) {
        viewModelScope.launch {
            try {
                setLoading(true)
                getHistory(offerId)
                getOurChoice(offerId)

                val res =
                    withContext(Dispatchers.IO) { offerOperations.getOffer(offerId, isSnapshot) }
                val data = res.success
                withContext(Dispatchers.Main) {
                    setLoading(false)

                    data?.let { offer ->

                        getCategoriesHistory(offer.catpath)
                        val initTimer =
                            ((offer.session?.end?.toLongOrNull()
                                ?: 1L) - (getCurrentDate().toLongOrNull()
                                ?: 1L)) * 1000

                        val images = when {
                            offer.images?.isNotEmpty() == true -> offer.images?.map { it.urls?.big?.content.orEmpty() }
                                ?: emptyList()

                            offer.externalImages?.isNotEmpty() == true -> offer.externalImages
                            else -> listOf("empty")
                        }
                        eventParameters = mapOf(
                            "lot_id" to offer.id,
                            "lot_name" to offer.title,
                            "lot_city" to offer.freeLocation,
                            "auc_delivery" to offer.safeDeal,
                            "lot_category_id" to offer.catpath.lastOrNull(),
                            "seller_id" to offer.sellerData?.id,
                            "lot_price_start" to offer.currentPricePerItem,
                            "visitor_id" to UserData.userInfo?.id
                        )

                        val offerState = when {
                            isSnapshot -> {
                                analyticsHelper.reportEvent("view_item_snapshot", eventParameters)
                                OfferStates.SNAPSHOT
                            }

                            offer.isPrototype -> {
                                analyticsHelper.reportEvent("view_item_prototype", eventParameters)
                                OfferStates.PROTOTYPE
                            }

                            offer.state == "active" -> {
                                analyticsHelper.reportEvent("view_item", eventParameters)
                                when {
                                    (offer.session?.start?.toLongOrNull()
                                        ?: 1L) > getCurrentDate().toLong() -> OfferStates.FUTURE

                                    (offer.session?.end?.toLongOrNull()
                                        ?: 1L) - getCurrentDate().toLong() > 0 -> OfferStates.ACTIVE

                                    else -> OfferStates.COMPLETED
                                }
                            }

                            offer.state == "sleeping" -> {
                                analyticsHelper.reportEvent("view_item", eventParameters)
                                if (offer.session == null || offer.buyerData != null) OfferStates.COMPLETED else OfferStates.INACTIVE
                            }

                            else -> {
                                analyticsHelper.reportEvent("view_item", eventParameters)
                                OfferStates.ACTIVE
                            }
                        }

                        if (offer.saleType != "buy_now" && offerState == OfferStates.ACTIVE) {
                            startTimerUpdateBids(offerBaseViewModel.offerState.value)
                        }

                        if (initTimer < 24 * 60 * 60 * 1000 && offerState == OfferStates.ACTIVE) {
                            startTimer(initTimer) {
                                getOffer(offer.id, isSnapshot)
                            }
                        } else {
                            _remainingTime.value = initTimer
                        }

                        offer.sellerData =
                            getUserInfo(offer.sellerData?.id ?: 1) ?: offer.sellerData

                        val columns =
                            if (isBigScreen.value) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(
                                1
                            )

                        val counts = (1..offer.currentQuantity).map { it.toString() }

                        _responseOfferView.value = OfferViewState(
                            statusList = checkStatusSeller(offer.sellerData?.id ?: 0),
                            columns = columns,
                            images = images,
                            countString = getCountString(offerState, offer),
                            buyNowCounts = counts,
                            isMyOffer = offer.sellerData?.login == UserData.userInfo?.login,
                            offerState = offerState,
                            dealTypeString = offer.dealTypes?.joinToString(separator = ". ") {
                                it.name ?: ""
                            }
                                ?: "",
                            deliveryMethodString = formatDeliveryMethods(offer.deliveryMethods),
                            paymentMethodString = offer.paymentMethods?.joinToString(separator = ". ") {
                                it.name ?: ""
                            } ?: "",
                        )

                        offerBaseViewModel.setNewOfferData(offer.parseToOfferItem())

                        updateUserState(offer.sellerData?.id ?: 1)
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Unknown error", ""))
            }
        }
    }

    suspend fun getUserInfo(id: Long) : User? {
        return try {
            val res = withContext(Dispatchers.IO) {
                userOperations.getUsers(id)
            }

            withContext(Dispatchers.Main) {
                val user = res.success?.firstOrNull()
                val error = res.error
                if (user != null) {
                    return@withContext user
                } else {
                    error?.let { throw it }
                }
            }
        } catch (exception: ServerErrorException) {
            onError(exception)
            null
        } catch (exception: Exception) {
            onError(
                ServerErrorException(
                    errorCode = exception.message.toString(),
                    humanMessage = exception.message.toString()
                )
            )
            null
        }
    }

    private fun getHistory(currentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val queries = dataBase.offerVisitedHistoryQueries

                val historyIds = queries.selectAll(UserData.login).executeAsList()
                    .filter { it != currentId }

                val offerItems = historyIds.mapNotNull { id ->
                    try {
                        offerOperations.getOffer(id).success?.parseToOfferItem()
                    } catch (_: Exception) {
                        null
                    }
                }

                _responseHistory.value = offerItems

            } catch (_: Exception) { }
        }
    }

    suspend fun addHistory(offerId: Long) {
        withContext(Dispatchers.IO) {
            val queries = dataBase.offerVisitedHistoryQueries
            queries.transaction {
                queries.deleteById(offerId, UserData.login)

                queries.insertEntry(offerId, UserData.login)

                val currentHistoryIds = queries.selectAll(UserData.login).executeAsList()

                if (currentHistoryIds.size > 17) {
                    val idsToDelete = currentHistoryIds.subList(17, currentHistoryIds.size)
                    idsToDelete.forEach { oldId ->
                        queries.deleteById(oldId, UserData.login)
                    }
                }
            }
        }
    }

    private fun getOurChoice(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getOurChoiceOffers(id)
                val serializer = Payload.serializer(Offer.serializer())
                val ourChoice = deserializePayload(response.payload, serializer).objects
                _responseOurChoice.value = ourChoice.map { it.parseToOfferItem() }.toList()
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching our choice", ""))
            }
        }
    }

    private fun getCategoriesHistory(catPath: List<Long>) {
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

    private fun startTimer(initialTime: Long, onFinish: () -> Unit) {
        _remainingTime.value = initialTime
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0) {
                delay(1000L)
                _remainingTime.value -= 1000L
            }
            onFinish()
        }
    }

    private fun startTimerUpdateBids(offer: OfferItem) {
        val initialTime =
            (offer.session?.end?.toLongOrNull()?.let { it - getCurrentDate().toLong() }
                ?: 0L) * 1000

        timerBidsJob?.cancel()
        timerBidsJob = viewModelScope.launch {
            var millisUntilFinished = initialTime
            var currentInterval = calculateNewInterval(millisUntilFinished)

            while (millisUntilFinished > 0) {
                updateBidsInfo(offer)
                val newInterval = calculateNewInterval(millisUntilFinished)
                if (newInterval != currentInterval) {
                    currentInterval = newInterval
                }

                delay(currentInterval)
                millisUntilFinished -= currentInterval
            }
            timerBidsJob?.cancel()
        }
    }

    fun updateUserState(id: Long){
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                getUserInfo(id)
            }
            withContext(Dispatchers.Main) {
                if (user != null) {
                    offerBaseViewModel.setNewOfferData(
                        offerBaseViewModel.offerState.value.copy(
                            seller = user,
                        )
                    )
                }
            }
        }
    }

    private fun calculateNewInterval(millisUntilFinished: Long): Long {
        return when {
            millisUntilFinished > 2 * 60 * 1000 -> 10_000L // Every 10 seconds
            millisUntilFinished in 1 * 60 * 1000..2 * 60 * 1000 -> 5_000L // Every 5 seconds
            millisUntilFinished in 10_000 + 1..1 * 60 * 1000 -> 2_000L // Every 2 seconds
            else -> 1_000L // Every second
        }
    }

    fun updateBidsInfo(offer: OfferItem) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    offerOperations.postGetLeaderAndPrice(offer.id, offer.version)
                }
                withContext(Dispatchers.Main) {
                    response.success?.body?.let { body ->
                        if (body.isChanged) {
                            offerBaseViewModel.setNewOfferData(
                                offerBaseViewModel.offerState.value.copy(
                                    bids = body.bids,
                                    version = JsonPrimitive(body.currentVersion),
                                    price = body.currentPrice ?: "",
                                    minimalAcceptablePrice = body.minimalAcceptablePrice ?: "",
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error updating bids info", ""))
            }
        }
    }

    fun onAddToCartClick(offer: OfferItem){
        if (UserData.token != "") {
            val bodyAddB = HashMap<String, JsonElement>()
            bodyAddB["offer_id"] = JsonPrimitive(offer.id)
            addOfferToBasket(
                bodyAddB
            ) { hm ->
                showToast(
                    successToastItem.copy(
                        message = hm
                    )
                )
            }
        } else {
            component.goToLogin()
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

    fun addToSubscriptions(offer: OfferItem, errorCallback: (String) -> Unit){
        if (UserData.token != "") {
            addNewSubscribe(
                LD(),
                SD().copy(
                    userLogin = offer.seller.login,
                    userID = offer.seller.id,
                    userSearch = true
                ),
                onSuccess = {
                    updateUserState(offer.seller.id)
                },
                errorCallback = { es ->
                    errorCallback(es)
                }
            )
        } else {
            component.goToLogin()
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

    fun scrollToBids(){
        if(offerBaseViewModel.offerState.value.bids?.isNotEmpty() == true) {
            _scrollPosition.value = goToBids
        }
    }

    fun clearScrollPosition(){
        _scrollPosition.value = 0
    }

    fun clearTimers() {
        timerJob?.cancel()
        timerBidsJob?.cancel()
    }

    private suspend fun getCountString(offerState: OfferStates, offer: Offer): String {
        val saleType = offer.saleType
        val isCompleted = offerState == OfferStates.COMPLETED
        val isCompletedOrInActive = isCompleted || offerState == OfferStates.INACTIVE

        val quantityInfo = "${getString(strings.quantityParameterName)}: ${offer.currentQuantity}"

        val qFullInfo = "${getString(strings.quantityParameterName)}: ${offer.currentQuantity} ${getString(strings.fromParameterName)} ${offer.originalQuantity}"

        return when {
            saleType == "buy_now" -> {
                if (isCompletedOrInActive) {
                    quantityInfo
                } else {
                    qFullInfo
                }
            }

            saleType == "auction_with_buy_now" && offer.quantity > 1 -> {
                if (isCompletedOrInActive) {
                    quantityInfo
                } else {
                    qFullInfo
                }
            }

            else -> ""
        }
    }

    private suspend fun formatDeliveryMethods(deliveryMethods: List<DeliveryMethod>?): String {
        val currencySign = getString(strings.currencySign)
        val withCountry = getString(strings.withinCountry)
        val withWorld = getString(strings.withinWorld)
        val withCity = getString(strings.withinCity)
        val comment = getString(strings.commentLabel)

        return deliveryMethods?.joinToString(separator = "\n\n") { dm ->
            buildString {
                append(dm.name)
                dm.priceWithinCity?.let { price ->
                    if (price.toDouble().toLong() != -1L) {
                        append("\n$withCity $price$currencySign")
                    }
                }
                dm.priceWithinCountry?.let { price ->
                    if (price.toDouble().toLong() != -1L) {
                        append("\n$withCountry $price$currencySign")
                    }
                }
                dm.priceWithinWorld?.let { price ->
                    if (price.toDouble().toLong() != -1L) {
                        append("\n$withWorld $price$currencySign")
                    }
                }
                if (!dm.comment.isNullOrEmpty()) {
                    append("\n$comment ${dm.comment}")
                }
            }
        } ?: ""
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
}

data class OfferRepositoryEventsImpl(
    val component: OfferComponent,
    val viewModel: OfferViewModel,
): OfferRepositoryEvents
{
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
        component.goToCreateOffer(type, catpath, id, externalImages)
    }

    override fun goToProposalPage(
        offerId: Long,
        type: ProposalType
    ) {
        component.goToProposalPage(type)
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        component.goToDynamicSettings(type, id)
    }

    override fun goToLogin() {
        component.goToLogin()
    }

    override fun goToDialog(id: Long?) {
        component.goToDialog(id)
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {
        component.goToCreateOrder(item)
    }

    override fun goToUserPage(sellerId: Long) {}

    override fun openCabinetOffer(offer: OfferItem) {}

    override fun scrollToBids() {
        viewModel.scrollToBids()
    }

    override fun refreshPage() {
        viewModel.refreshPage()
    }

    override fun updateBidsInfo(item: OfferItem) {
        viewModel.updateBidsInfo(viewModel.offerBaseViewModel.offerState.value)
    }
}
