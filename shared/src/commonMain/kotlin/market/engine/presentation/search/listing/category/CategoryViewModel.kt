package market.engine.presentation.search.listing.category

import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.APIService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import market.engine.core.baseFilters.LD
import market.engine.core.baseFilters.SD
import market.engine.core.items.ListingData
import market.engine.core.network.functions.CategoryOperations
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class CategoryViewModel(private val apiService: APIService) : BaseViewModel() {

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val categoryOperations : CategoryOperations = getKoin().get()

    fun getCategory(searchData : SD, listingData : LD) {
        setLoading(true)
        viewModelScope.launch {
            try {
                val response =  apiService.getPublicCategories(searchData.searchCategoryID ?: 1)
                val serializer = Payload.serializer(Category.serializer())
                val payload: Payload<Category> = deserializePayload(response.payload, serializer)

                val categoriesWithLotCounts = payload.objects.map { category ->
                    async {
                        val sd = searchData.copy()
                        sd.searchCategoryID = category.id
                        val lotCount = categoryOperations.getTotalCount(ListingData(
                            _searchData = sd,
                            _data = listingData
                        ))
                        category.copy(estimatedActiveOffersCount = lotCount.success ?: 0)
                    }
                }

                val categories = categoriesWithLotCounts.awaitAll().filter { it.estimatedActiveOffersCount > 0 }
                _responseCategory.value = categories

            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", ""))
            } finally {
                setLoading(false)
            }
        }
    }
}
