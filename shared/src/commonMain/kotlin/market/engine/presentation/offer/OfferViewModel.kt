package market.engine.presentation.offer

import application.market.agora.business.core.network.functions.OfferOperations
import application.market.agora.business.core.network.functions.UserOperations
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.globalData.UserData
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.util.getCurrentDate
import market.engine.presentation.base.BaseViewModel
import market.engine.shared.MarketDB

class OfferViewModel(
    private val apiService: APIService,
    private val dataBase: MarketDB,
    private val categoryOperations: CategoryOperations,
    private val offerOperations: OfferOperations,
    private val userOperations: UserOperations
) : BaseViewModel() {

    private val _responseOffer : MutableStateFlow<Offer?> = MutableStateFlow(null)
    val responseOffer: StateFlow<Offer?> = _responseOffer.asStateFlow()

    private val _responseHistory = MutableStateFlow<List<Offer>>(emptyList())
    val responseHistory: StateFlow<List<Offer>> = _responseHistory.asStateFlow()

    private val _responseOurChoice = MutableStateFlow<List<Offer>>(emptyList())
    val responseOurChoice: StateFlow<List<Offer>> = _responseOurChoice.asStateFlow()

    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())
    val responseCatHistory: StateFlow<List<Category>> = _responseCatHistory.asStateFlow()

    private val _statusList = MutableStateFlow<String?>(null)
    val statusList: StateFlow<String?> = _statusList.asStateFlow()

    val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private var timerJob: Job? = null
    private var timerBidsJob: Job? = null

    fun getOffer(id: Long) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val offer = withContext(Dispatchers.IO) {
                    val response = apiService.getOffer(id)
                    deserializePayload<ArrayList<Offer>>(response.payload).firstOrNull()
                }
                offer?.let {
                    _responseOffer.value = it
                    initializeOfferData(it)
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Unknown error", ""))
            }
        }
    }

    private fun initializeOfferData(offer: Offer) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    launch { getHistory(offer.id) }
                    launch { getOurChoice(offer.id) }
                    launch { getCategoriesHistory(offer.catpath) }
                    launch { checkStatusSeller(offer.sellerData?.id ?: 0) }
                    launch { if (responseOffer.value != null) setLoading(false) }
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "Initialization error", ""))
            }
        }
    }

    private fun getHistory(currentId: Long) {
        viewModelScope.launch {
            try {
                val history = withContext(Dispatchers.IO) {
                    val queries = dataBase.offerVisitedHistoryQueries
                    queries.selectAll(UserData.login).executeAsList().apply {
                        if (size > 17) queries.deleteById(first(), UserData.login)
                        queries.insertEntry(currentId, UserData.login)
                    }
                }
                val offers = withContext(Dispatchers.IO) {
                    history.mapNotNull { id ->
                        apiService.getOffer(id).let { deserializePayload<ArrayList<Offer>>(it.payload).firstOrNull() }
                    }
                }
                _responseHistory.value = offers
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
        viewModelScope.launch {
            try {
                val ourChoice = withContext(Dispatchers.IO) {
                    val response = apiService.getOurChoiceOffers(id)
                    deserializePayload<Payload<Offer>>(response.payload).objects
                }
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

    private suspend fun checkStatusSeller(id: Long) {
        val lists = listOf("blacklist_sellers", "blacklist_buyers", "whitelist_buyers")
        for (list in lists) {
            val found = withContext(Dispatchers.IO) {
                userOperations.getUsersOperationsGetUserList(UserData.login, hashMapOf("list_type" to list))
                    .success?.body?.data?.find { it.id == id }
            }
            if (found != null) {
                _statusList.value = list
                return
            }
        }
        _statusList.value = null
    }

    fun startTimer(initialTime: Long, onFinish: () -> Unit) {
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


    fun startTimerUpdateBids(offer: Offer, finishTimer: () -> Unit) {
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

            finishTimer()
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

    private suspend fun updateBidsInfo(offer: Offer) {
        try {
            val response = offerOperations.postGetLeaderAndPrice(offer.id, offer.version)
            response.success?.body?.let { body ->
                if (body.isChanged) {
                    offer.apply {
                        bids = body.bids
                        version = JsonPrimitive(body.currentVersion)
                        currentPricePerItem = body.currentPrice
                        minimalAcceptablePrice = body.minimalAcceptablePrice
                    }
                    _responseOffer.value = offer
                }
            }
        } catch (e: Exception) {
            onError(ServerErrorException(e.message ?: "Error updating bids info", ""))
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearTimers()
    }

    fun clearTimers() {
        timerJob?.cancel()
        timerBidsJob?.cancel()
    }
}
