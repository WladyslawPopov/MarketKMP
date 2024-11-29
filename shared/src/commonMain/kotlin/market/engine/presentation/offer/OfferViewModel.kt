package market.engine.presentation.offer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.globalData.UserData
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.presentation.base.BaseViewModel
import market.engine.shared.MarketDB

class OfferViewModel(
    val apiService: APIService,
    private val dataBase : MarketDB,
    private val categoryOperations: CategoryOperations,
) : BaseViewModel() {
    private val _responseOffer = MutableStateFlow(Offer())
    val responseOffer: StateFlow<Offer> = _responseOffer.asStateFlow()

    private val _responseHistory = MutableStateFlow<List<Offer>>(emptyList())
    val responseHistory: StateFlow<List<Offer>> = _responseHistory.asStateFlow()

    private val _responseOurChoice = MutableStateFlow<List<Offer>>(emptyList())
    val responseOurChoice: StateFlow<List<Offer>> = _responseOurChoice.asStateFlow()

    private val _responseCatHistory = MutableStateFlow<List<Category>>(emptyList())
    val responseCatHistory: StateFlow<List<Category>> = _responseCatHistory.asStateFlow()

    fun getOffer(id: Long) {
        viewModelScope.launch {
            try {
                setLoading(true)

                getHistory(id)
                getOurChoice(id)

                val response = withContext(Dispatchers.IO) {
                    apiService.getOffer(id)
                }

                val payload: ArrayList<Offer> = deserializePayload(response.payload)
                getCategoriesHistory(payload[0].catpath)
                _responseOffer.value = payload[0]
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun getOurChoice(id: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = apiService.getOurChoiceOffers(id)
                    withContext(Dispatchers.Main) {
                        try {
                            val payload : Payload<Offer> =
                                deserializePayload(response.payload)
                            _responseOurChoice.value = payload.objects
                        }catch (e : Exception){
                            throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                        }
                    }
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }

    private fun getHistory(currentId : Long = 1) {
        try {
            viewModelScope.launch {
                val sh = dataBase.offerVisitedHistoryQueries
                var offersHistory: List<Long> =
                    sh.selectAll(UserData.login).executeAsList()

                if (offersHistory.size > 17) {
                    removeHistory(offersHistory[0])
                }

                val current =
                    offersHistory.filter { it == currentId }

                if (current.isNotEmpty()) {
                    current.forEach {
                        removeHistory(it)
                    }
                }

                offersHistory = sh.selectAll(UserData.login).executeAsList()

                val deferredOffers = mutableListOf<Offer>()

                withContext(Dispatchers.IO) {
                    // Process each category sequentially to preserve order
                    for (offer in offersHistory) {
                        try {
                            val res = apiService.getOffer(offer)
                            val payload: ArrayList<Offer> = deserializePayload(res.payload)
                            if(payload.firstOrNull() != null){
                                deferredOffers.add(payload.first())
                            }
                        } catch (e: Exception) {
                            throw  e
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (deferredOffers.isNotEmpty()) {
                        _responseHistory.value = deferredOffers
                    }
                }
            }
        } catch (e: Exception) {
            onError(ServerErrorException(e.message.toString(), ""))
        }
    }

    private fun getCategoriesHistory(catpath: List<Long>) {
        viewModelScope.launch {
            val cats = catpath.reversed() // Reverse the category path
            try {
                val orderedCategories = mutableListOf<Category>()

                withContext(Dispatchers.IO) {
                    // Process each category sequentially to preserve order
                    for (cat in cats) {
                        try {
                            val res = categoryOperations.getCategoryInfo(cat)
                            res.success?.let { category ->
                                orderedCategories.add(category)
                            }
                        } catch (e: Exception) {
                            throw  e
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    // Update the state with the ordered list
                    _responseCatHistory.value = orderedCategories.toList()
                }
            } catch (e: Exception) {
                onError(ServerErrorException(e.message.toString(), ""))
            }
        }
    }

    fun addHistory(id : Long){
        val sh = dataBase.offerVisitedHistoryQueries
        sh.insertEntry(id, UserData.login)
    }

    private fun removeHistory(id : Long){
        val sh = dataBase.offerVisitedHistoryQueries
        sh.deleteById(id, UserData.login)
    }
}
