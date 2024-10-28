package market.engine.presentation.category

import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.network.functions.CategoryOperations
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class CategoryViewModel(private val apiService: APIService) : BaseViewModel() {

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val categoryOperations : CategoryOperations = getKoin().get()

    val globalData: CategoryBaseFilters = getKoin().get()
    val searchData = globalData.listingData.searchData

    fun getCategory(categoryId: Long = searchData.value.searchCategoryID ?: 1L) {
        onError(ServerErrorException())
        setLoading(true)
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response =
                        apiService.getPublicCategories(categoryId)

                    withContext(Dispatchers.Main){
                        val payload: Payload<Category> = deserializePayload(response.payload)

                        withContext(Dispatchers.IO) {
                            val categoriesWithLotCounts = payload.objects.map { category ->
                                val lotCount = categoryOperations.getTotalCount(category.id)

                                category.copy(estimatedActiveOffersCount = lotCount.success ?: 0)
                            }
                            withContext(Dispatchers.Main){
                                val categories =
                                    categoriesWithLotCounts.filter { it.estimatedActiveOffersCount > 0 }

                                _responseCategory.value = categories
                            }
                        }
                    }
                }
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
