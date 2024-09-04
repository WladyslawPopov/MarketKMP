package market.engine.ui.home

import application.market.auction_mobile.business.core.ServerErrorException
import application.market.auction_mobile.business.networkObjects.Category
import application.market.auction_mobile.business.networkObjects.Offer
import application.market.auction_mobile.business.networkObjects.Payload
import application.market.auction_mobile.business.networkObjects.deserializePayload
import application.market.core.network.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.business.core.network.viewModels.BaseViewModel

class HomeViewModel(private val apiService: APIService) : BaseViewModel() {
    private val defaultCategoryId = 1L

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val _responseOffersPromotedOnMainPage1 = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffersPromotedOnMainPage1: StateFlow<List<Offer>> = _responseOffersPromotedOnMainPage1.asStateFlow()

    private val _responseOffersPromotedOnMainPage2 = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffersPromotedOnMainPage2: StateFlow<List<Offer>> = _responseOffersPromotedOnMainPage2.asStateFlow()

    init {
        getCategory()
       // getOffersPromotedOnMainPage1(0, 10)
       // getOffersPromotedOnMainPage2(1, 10)
    }

    private fun getCategory(categoryId: Long = defaultCategoryId) {
        setLoading(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getPublicCategories(categoryId)
                }
                val payload: Payload<Category> = deserializePayload(response.payload)
                _responseCategory.value = payload.objects
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", "An error occurred"))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun getOffersPromotedOnMainPage1(page: Int, ipp: Int) {
        setLoading(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getOffersPromotedOnMainPage(page, ipp)
                }
                val payload: Payload<Offer> = deserializePayload(response.payload)
                _responseOffersPromotedOnMainPage1.value = payload.objects
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", "An error occurred"))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun getOffersPromotedOnMainPage2(page: Int, ipp: Int) {
        setLoading(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getOffersPromotedOnMainPage(page, ipp)
                }
                val payload: Payload<Offer> = deserializePayload(response.payload)
                _responseOffersPromotedOnMainPage2.value = payload.objects
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", "An error occurred"))
            } finally {
                setLoading(false)
            }
        }
    }
}
