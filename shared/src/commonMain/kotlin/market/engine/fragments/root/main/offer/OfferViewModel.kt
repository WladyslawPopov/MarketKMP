package market.engine.fragments.root.main.offer

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.data.types.OfferStates
import market.engine.core.utils.getCurrentDate
import market.engine.fragments.base.BaseViewModel
import market.engine.shared.MarketDB
import org.jetbrains.compose.resources.getString

class OfferViewModel(
    private val dataBase: MarketDB
) : BaseViewModel() {

    private val _responseOffer : MutableStateFlow<Offer?> = MutableStateFlow(null)
    val responseOffer: StateFlow<Offer?> = _responseOffer.asStateFlow()

    private val _responseHistory = MutableStateFlow<ArrayList<Offer>>(arrayListOf())
    val responseHistory: StateFlow<ArrayList<Offer>> = _responseHistory.asStateFlow()

    private val _responseOurChoice = MutableStateFlow<ArrayList<Offer>>(arrayListOf())
    val responseOurChoice: StateFlow<ArrayList<Offer>> = _responseOurChoice.asStateFlow()

    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())
    val responseCatHistory: StateFlow<List<Category>> = _responseCatHistory.asStateFlow()

    private val _statusList = MutableStateFlow<ArrayList<String>>(arrayListOf())
    val statusList: StateFlow<ArrayList<String>> = _statusList.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private var timerJob: Job? = null
    private var timerBidsJob: Job? = null

    val offerState = mutableStateOf(OfferStates.ACTIVE)

    val isMyOffer = mutableStateOf(false)

    var eventParameters : Map<String, Any?> = mapOf()

    fun getOffer(id: Long, isSnapshot: Boolean = false) {
        viewModelScope.launch {
            try {
                setLoading(true)
                getHistory(id)
                getOurChoice(id)

                val offer = withContext(Dispatchers.IO) {

                    val response = if (isSnapshot) apiService.getOfferSnapshots(id) else apiService.getOffer(id)
                    val serializer = ListSerializer(Offer.serializer())
                    deserializePayload(response.payload, serializer).firstOrNull()
                }
                offer?.let { data ->
                    _responseOffer.value = data
                    initializeOfferData(data, isSnapshot)
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Unknown error", ""))
            }
        }
    }

    fun getUserInfo(id : Long) {
        viewModelScope.launch {
            try {
                val res =  withContext(Dispatchers.IO){
                    userOperations.getUsers(id)
                }

                withContext(Dispatchers.Main){
                    val user = res.success?.firstOrNull()
                    val error = res.error
                    if (user != null){
                        _responseOffer.value = _responseOffer.value?.copy(
                            sellerData = user
                        )
                        updateItemTrigger.value++
                    }else{
                        error?.let { throw it }
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }

    private fun initializeOfferData(offer: Offer, isSnapshot: Boolean) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch {
                        updateUserInfo()
                        getUserInfo(offer.sellerData?.id ?: 1L)
                    }
                    launch {
                        //init timers
                        val initTimer =
                            ((offer.session?.end?.toLongOrNull() ?: 1L) - (getCurrentDate().toLongOrNull()
                                ?: 1L)) * 1000

                        isMyOffer.value = UserData.userInfo?.login == offer.sellerData?.login

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

                        offerState.value = when {
                            isSnapshot -> {
                                analyticsHelper.reportEvent("view_item_snapshot", eventParameters)
                                OfferStates.SNAPSHOT
                            }
                            offer.isPrototype -> {
                                analyticsHelper.reportEvent("view_item_prototype", eventParameters)
                                OfferStates.PROTOTYPE
                            }
                            offer.state == "active" -> {
                                val res = offerOperations.getOperationsOffer(offer.id)
                                val buf = res.success

                                _responseOffer.value?.isProposalEnabled = buf?.find { it.id == "make_proposal" } != null

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
                                if (offer.session == null || offer.buyerData!=null) OfferStates.COMPLETED else OfferStates.INACTIVE
                            }

                            else -> {
                                analyticsHelper.reportEvent("view_item", eventParameters)
                                offerState.value
                            }
                        }

                        if (offer.saleType != "buy_now" && offerState.value == OfferStates.ACTIVE) {
                            startTimerUpdateBids(offer)
                        }

                        if (initTimer < 24 * 60 * 60 * 1000 && offerState.value == OfferStates.ACTIVE) {
                            startTimer(initTimer) {
                                getOffer(offer.id)
                            }
                        }else{
                            _remainingTime.value = initTimer
                        }
                    }
                    launch { getCategoriesHistory(offer.catpath) }
                    launch { _statusList.value = checkStatusSeller(offer.sellerData?.id ?: 0) }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Initialization error", ""))
            } finally {
                setLoading(false)
            }
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
                historyIds.forEach { id ->
                    val response = apiService.getOffer(id)
                    val serializer = ListSerializer(Offer.serializer())
                    val offer = deserializePayload(response.payload, serializer).firstOrNull()
                    offer?.let {
                        // Update the response history only on the main thread.
                        withContext(Dispatchers.Main) {
                            _responseHistory.value.add(it)
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error fetching history", ""))
            }
        }
    }

    fun addHistory(id : Long){
        val sh = dataBase.offerVisitedHistoryQueries
        sh.insertEntry(id, UserData.login)
    }

    private fun getOurChoice(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getOurChoiceOffers(id)
                val serializer = Payload.serializer(Offer.serializer())
                val ourChoice = deserializePayload(response.payload, serializer).objects
                _responseOurChoice.value = ourChoice
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
        val initialTime = (offer.session?.end?.toLongOrNull()?.let { it - getCurrentDate().toLong() } ?: 0L) * 1000

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
                            offer.apply {
                                bids = body.bids
                                version = JsonPrimitive(body.currentVersion)
                                currentPricePerItem = body.currentPrice
                                minimalAcceptablePrice = body.minimalAcceptablePrice
                            }
                            _responseOffer.value = offer
                            updateItemTrigger.value++
                        }
                    }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Error updating bids info", ""))
            }
        }
    }

    fun addToBasket(offerId: Long) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO){
                    val bodyAddB = HashMap<String,String>()
                    bodyAddB["offer_id"] = offerId.toString()
                    userOperations.postUsersOperationsAddItemToCart(UserData.login, bodyAddB)
                }
                withContext(Dispatchers.Main) {
                    if (response.success?.success == true) {
                        analyticsHelper.reportEvent(
                            "add_item_to_cart",
                            eventParameters
                        )
                        showToast(
                            successToastItem.copy(message = getString(strings.offerAddedToBasketLabel))
                        )
                        updateUserInfo()
                    } else {
                        throw ServerErrorException(
                            errorCode = response.success?.errorCode ?: "",
                            humanMessage = response.success?.humanMessage ?: ""
                        )
                    }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }

    fun addBid(
        sum: String,
        offer: Offer,
        onSuccess: () -> Unit,
        onDismiss: () -> Unit
    ){
        viewModelScope.launch {
            val res =  withContext(Dispatchers.IO) {
                val body = hashMapOf("price" to sum)
                offerOperations.postOfferOperationsAddBid(
                    offer.id,
                    body
                )
            }

            val buf = res.success
            val error = res.error

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    if (buf.success) {
                        showToast(
                            successToastItem.copy(
                                message = buf.humanMessage ?: getString(strings.operationSuccess)
                            )
                        )
                        val eventParameters = mapOf(
                            "lot_id" to offer.id,
                            "lot_name" to offer.name,
                            "lot_city" to offer.freeLocation,
                            "auc_delivery" to offer.safeDeal,
                            "lot_category" to offer.catpath.firstOrNull(),
                            "seller_id" to offer.sellerData?.id,
                            "lot_price_start" to offer.currentPricePerItem,
                            "buyer_id" to UserData.login,
                            "bid_amount" to sum,
                            "bids_all" to offer.bids?.size
                        )
                        analyticsHelper.reportEvent(
                            "bid_made",
                            eventParameters
                        )
                        onSuccess()
                    } else {
                        showToast(
                            errorToastItem.copy(
                                message = buf.humanMessage ?: getString(strings.operationFailed)
                            )
                        )
                        val eventParameters = mapOf(
                            "lot_id" to offer.id,
                            "seller_id" to offer.sellerData?.id,
                            "lot_price_start" to offer.currentPricePerItem,
                            "buyer_id" to UserData.login,
                            "bid_amount" to sum,
                            "error" to buf.humanMessage
                        )
                        analyticsHelper.reportEvent("bid_made_failed", eventParameters)

                        onDismiss()
                    }
                } else {
                    error?.let { onError(it) }
                }
            }
        }
    }

    fun clearTimers() {
        timerJob?.cancel()
        timerBidsJob?.cancel()
    }
}
