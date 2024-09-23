package market.engine.ui.search

import application.market.agora.business.networkObjects.Category
import market.engine.business.core.network.APIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.business.core.network.functions.CategoryOperations
import market.engine.business.globalObjects.searchData
import market.engine.root.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class SearchViewModel(private val apiService: APIService) : BaseViewModel() {
    private val defaultCategoryId = searchData.searchCategoryID ?: 1L

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    private val categoryOperations : CategoryOperations = getKoin().get()

    fun getHistory() : StateFlow<List<String>>? {
        return null
    }

}
