package market.engine.ui.search

import market.engine.business.core.ServerErrorException
import application.market.auction_mobile.business.networkObjects.Category
import application.market.auction_mobile.business.networkObjects.Offer
import application.market.auction_mobile.business.networkObjects.Payload
import application.market.auction_mobile.business.networkObjects.deserializePayload
import market.engine.business.core.network.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.business.core.network.functions.CategoryOperations
import market.engine.business.globalObjects.listingData
import market.engine.business.globalObjects.searchData
import market.engine.root.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class SearchViewModel(private val apiService: APIService) : BaseViewModel() {
    private val defaultCategoryId = searchData.searchCategoryID ?: 1L

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val categoryOperations : CategoryOperations = getKoin().get()


    fun getCategory(categoryId: Long = defaultCategoryId) {
        onError(ServerErrorException())
        setLoading(true)
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getPublicCategories(categoryId)
                }
                val payload: Payload<Category> = deserializePayload(response.payload)

                val categoriesWithLotCounts = payload.objects.map { category ->
                    val lotCount = withContext(Dispatchers.IO) {
                        categoryOperations.getTotalCount(category.id)
                    }
                    category.copy(estimatedActiveOffersCount = lotCount.success ?: 0)
                }

                val categories = categoriesWithLotCounts.filter { it.estimatedActiveOffersCount > 0 }

                _responseCategory.value = categories
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
