package market.engine.presentation.category

import market.engine.core.network.ServerErrorException
import market.engine.core.networkObjects.Category
import market.engine.core.networkObjects.Payload
import market.engine.core.networkObjects.deserializePayload
import market.engine.core.network.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.network.functions.CategoryOperations
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class CategoryViewModel(private val apiService: APIService) : BaseViewModel() {
    private var defaultCategoryId = 1L

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val categoryOperations : CategoryOperations = getKoin().get()


    fun getCategory(categoryId: Long = defaultCategoryId) {
        if (categoryId != defaultCategoryId)
            defaultCategoryId = categoryId
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

    fun updateCategory(categoryId: Long){
        if (categoryId != defaultCategoryId){
            getCategory(categoryId)
        }
    }
}
