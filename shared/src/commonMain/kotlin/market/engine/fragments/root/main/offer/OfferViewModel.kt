package market.engine.fragments.root.main.offer

import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.data.types.OfferStates
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.DeliveryMethod
import market.engine.core.network.networkObjects.User
import market.engine.core.repositories.OfferRepository
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString
import kotlin.toString

data class OfferViewState(
    val appBarData: SimpleAppBarData = SimpleAppBarData(
        onBackClick = {},
        listItems = emptyList(),
        menuItems = emptyList(),
        closeMenu = {}
    ),
    val offer: Offer = Offer(),

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
    private val dataBase: MarketDB,
    private val component: OfferComponent,
    val offerId : Long = 1,
    val isSnapshot : Boolean = false
) : BaseViewModel()
{
    private val _responseOffer: MutableStateFlow<Offer> = MutableStateFlow(Offer())

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

    private var timerJob: Job? = null
    private var timerBidsJob: Job? = null
    private var eventParameters: Map<String, Any?> = mapOf()

    private val _showDialog = MutableStateFlow("")
    val showDialog = _showDialog.asStateFlow()

    private val _errorString = MutableStateFlow("")
    val errorString = _errorString.asStateFlow()

    val goToBids = 6

    private val offerRepositoryEvents = OfferRepositoryEventsImpl(component, this)

    private val _offerRepository = MutableStateFlow(OfferRepository(events = offerRepositoryEvents))
    val offerRepository: StateFlow<OfferRepository> = _offerRepository.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val offerViewState : StateFlow<OfferViewState> = _responseOffer.flatMapConcat { offer ->
        //init timers
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
            startTimerUpdateBids(offer)
        }

        if (initTimer < 24 * 60 * 60 * 1000 && offerState == OfferStates.ACTIVE) {
            startTimer(initTimer) {
                getOffer(offer.id)
            }
        } else {
            _remainingTime.value = initTimer
        }

        offer.sellerData = getUserInfo(offer.sellerData?.id ?: 1) ?: offer.sellerData

        val columns =
            if (isBigScreen.value) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1)

        val counts = (1..offer.quantity).map { it.toString() }
        setLoading(false)
        flowOf(
            OfferViewState(
                offer = offer,
                statusList = checkStatusSeller(offer.sellerData?.id ?: 0),
                columns = columns,
                images = images,
                countString = getCountString(offerState, offer),
                buyNowCounts = counts,
                isMyOffer = offer.sellerData?.login == UserData.userInfo?.login,
                offerState = offerState,
                dealTypeString = offer.dealTypes?.joinToString(separator = ". ") { it.name ?: "" }
                    ?: "",
                deliveryMethodString = formatDeliveryMethods(offer.deliveryMethods),
                paymentMethodString = offer.paymentMethods?.joinToString(separator = ". ") {
                    it.name ?: ""
                } ?: "",
            )
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        OfferViewState()
    )

    init {
        refreshPage()
    }

    fun refreshPage() {
        getOffer(offerId, isSnapshot)
        refresh()
    }

    fun editNote(){
        viewModelScope.launch {
            offerRepository.value.operationsList.value.find { it.id == "edit_note" }?.onClick()
        }
    }

    fun deleteNote(id: Long){
        viewModelScope.launch {
            deleteNote(id) {
                refreshPage()
            }
        }
    }

    fun getOffer(offerId: Long, isSnapshot: Boolean = false) {
        viewModelScope.launch {
            try {
                setLoading(true)
                getHistory(offerId)
                getOurChoice(offerId)
                updateUserInfo()

                withContext(Dispatchers.IO) {
                    val response =
                        if (isSnapshot) apiService.getOfferSnapshots(offerId) else apiService.getOffer(offerId)
                    val serializer = ListSerializer(Offer.serializer())
                    val data = deserializePayload(response.payload, serializer).firstOrNull()
                    data?.let { offer ->
                        getCategoriesHistory(offer.catpath)
                        _offerRepository.value = OfferRepository(offer = offer.parseToOfferItem(), events = offerRepositoryEvents, this@OfferViewModel)
                        _responseOffer.value = offer
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
                _responseHistory.value = arrayListOf()
                val queries = dataBase.offerVisitedHistoryQueries
                val historyIds = queries.selectAll(UserData.login).executeAsList()

                // Delete the oldest entry if the history size exceeds the limit.
                if (historyIds.size >= 17) {
                    queries.deleteById(historyIds.last(), UserData.login)
                }
                // Insert the current offer ID into the history.
                queries.insertEntry(currentId, UserData.login)

                // Fetch offer details for each history ID and update the response history.
                _responseHistory.value = buildList {
                    historyIds.forEach { id ->
                        val response = apiService.getOffer(id)
                        val serializer = ListSerializer(Offer.serializer())
                        val offer = deserializePayload(response.payload, serializer).firstOrNull()
                        offer?.let {
                            // Update the response history only on the main thread.
                            withContext(Dispatchers.Main) {
                                add(it.parseToOfferItem())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching history", ""))
            }
        }
    }

    fun addHistory(id: Long) {
        val sh = dataBase.offerVisitedHistoryQueries
        sh.insertEntry(id, UserData.login)
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

    private fun startTimerUpdateBids(offer: Offer) {
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
                    _responseOffer.update {
                        it.copy(
                            sellerData = user
                        )
                    }
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

    fun updateBidsInfo(offer: Offer) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    offerOperations.postGetLeaderAndPrice(offer.id, offer.version)
                }
                withContext(Dispatchers.Main) {
                    response.success?.body?.let { body ->
                        if (body.isChanged) {
                            _responseOffer.update {
                                it.copy(
                                    bids = body.bids,
                                    version = JsonPrimitive(body.currentVersion),
                                    currentPricePerItem = body.currentPrice,
                                    minimalAcceptablePrice = body.minimalAcceptablePrice,
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error updating bids info", ""))
            }
        }
    }

    fun onAddToCartClick(offer: Offer){
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

    fun addToSubscriptions(offer: Offer){
        if (UserData.token != "") {
            addNewSubscribe(
                LD(),
                SD().copy(
                    userLogin = offer.sellerData?.login,
                    userID = offer.sellerData?.id ?: 1L,
                    userSearch = true
                ),
                onSuccess = {
                    updateUserState(offer.sellerData?.id ?: 1L)
                },
                errorCallback = { es ->
                    _errorString.value = es
                }
            )
        } else {
            component.goToLogin()
        }
    }

    fun scrollToBids(){
        _scrollPosition.value = goToBids
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

        val qFullInfo = "${getString(strings.quantityParameterName)}: ${offer.currentQuantity} ${getString(strings.fromParameterName)} ${offer.quantity}"

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

    override fun goToProposalPage(type: ProposalType) {
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

    override fun goToUserPage() {}

    override fun openCabinetOffer() {}

    override fun isHideCabinetOffer() : Boolean {
        return true
    }

    override fun scrollToBids() {
        viewModel.scrollToBids()
    }

    override fun refreshPage() {
        viewModel.refreshPage()
    }

    override fun updateItem(item: OfferItem) {}
}
