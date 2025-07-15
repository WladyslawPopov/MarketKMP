package market.engine.widgets.filterContents.categories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Category
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

@Serializable
data class CategoryPageState(
    val catDef : String = "",
    val catBtn : String = "",
    val enabledBtn : Boolean = true,
    val categoryWithoutCounter : Boolean = false,
)

class CategoryViewModel(
    val isFilters: Boolean = false,
    val isCreateOffer: Boolean = false,
) : CoreViewModel() {

    // State implementation
    private val _searchData = MutableStateFlow(SD())
    val searchData: StateFlow<SD> = _searchData.asStateFlow()

    private val _filters = MutableStateFlow<List<Filter>>(emptyList())
    val filters = _filters.asStateFlow()

    private val _selectedId = MutableStateFlow(1L)
    val selectedId: StateFlow<Long> = _selectedId.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    private val _pageState = MutableStateFlow(CategoryPageState())
    val pageState = _pageState.asStateFlow()


    init {
        viewModelScope.launch {
            try {
                _pageState.update {

                    var catDef = ""
                    var catBtn = ""
                    var enabledBtn = true

                    when {
                        isFilters -> {
                            catDef = getString(strings.selectCategory)
                            catBtn = getString(strings.actionAcceptFilters)
                        }

                        isCreateOffer -> {
                            catDef = getString(strings.selectCategory)
                            catBtn = getString(strings.continueLabel)
                            enabledBtn = selectedId.value != 1L
                        }

                        else -> {
                            catDef = getString(strings.categoryMain)
                            catBtn = getString(strings.categoryEnter)
                        }
                    }

                    it.copy(
                        catDef = catDef,
                        catBtn = catBtn,
                        enabledBtn = enabledBtn,
                        categoryWithoutCounter = (isFilters || isCreateOffer)
                    )
                }
                fetchCategories()
            } catch (_ : Exception){ }
        }
    }

    fun onRefresh() {
        initialize(filters.value)
    }

    // Actions implementation
    fun fetchCategories() {
        _isLoading.value = true

        viewModelScope.launch {
            getCategories(
                searchData.value,
                LD(filters.value),
                pageState.value.categoryWithoutCounter
            ) {
                _categories.value = it
                _isLoading.value = false
            }
        }
    }

    fun navigateBack() {
        if (searchData.value.searchCategoryID != 1L) {
            _isLoading.value = true
            viewModelScope.launch {
                onCatBack(searchData.value.searchParentID ?: 1L) { newCat ->
                    setUpNewParams(newCat)
                    fetchCategories()
                }
            }
        }
    }

    fun resetToRoot() {
        if (searchData.value.searchCategoryID != 1L) {
            setUpNewParams(Category(id = 1L, name = pageState.value.catDef))
        }
        fetchCategories()
    }

    fun selectCategory(category: Category) {
        setUpNewParams(category)

        if (!category.isLeaf) {
            fetchCategories()
        } else {
            _selectedId.value = category.id
        }
    }

    fun initialize(filters: List<Filter> = this.filters.value) {
        _filters.value = filters

        if (searchData.value.searchIsLeaf) {
            _isLoading.value = true
            viewModelScope.launch {
                onCatBack(searchData.value.searchParentID ?: 1L) { newCat ->
                    val cat = if (searchData.value.searchParentID == newCat.id && newCat.isLeaf) {
                        newCat.copy(id = newCat.parentId, isLeaf = false)
                    } else {
                        newCat
                    }
                    setUpNewParams(cat)
                    fetchCategories()
                }
            }
        } else {
            fetchCategories()
        }
    }

    private fun setUpNewParams(newCat: Category) {
        _searchData.update {
            it.copy(
                searchCategoryID = newCat.id,
                searchCategoryName = newCat.name ?: pageState.value.catDef,
                searchParentID = newCat.parentId,
                searchIsLeaf = newCat.isLeaf
            )
        }
        _selectedId.value = 1L
    }

    fun updateFromSearchData(searchData: SD) {
        _searchData.update {
            searchData.copy()
        }
    }

    fun onCatBack(
        uploadId: Long,
        onSuccess: (Category) -> Unit
    ) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                categoryOperations.getCategoryInfo(
                    uploadId
                )
            }
            if (response.success != null){
                onSuccess(response.success!!)
            }
        }
    }
}
