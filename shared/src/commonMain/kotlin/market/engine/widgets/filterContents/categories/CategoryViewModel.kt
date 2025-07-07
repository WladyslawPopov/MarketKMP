package market.engine.widgets.filterContents.categories

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Category
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString

interface CategoryState {
    val categoryId: StateFlow<Long>
    val categoryName: StateFlow<String>
    val parentId: StateFlow<Long?>
    val isLeaf: StateFlow<Boolean>
    val selectedId: StateFlow<Long>
    val categories: StateFlow<List<Category>>
    val isLoading: StateFlow<Boolean>
}

interface CategoryActions {
    fun onRefresh()

    fun fetchCategories(
        searchData: SD,
        filters: List<Filter> = emptyList(),
        withoutCounter: Boolean = false
    )

    fun navigateBack()

    fun resetToRoot()

    fun selectCategory(category: Category)

    fun initialize(filters: List<Filter> = emptyList())
}

class CategoryViewModel(
    val isFilters: Boolean = false,
    val isCreateOffer: Boolean = false,
) : CategoryActions, CategoryState, CoreViewModel() {

    // State implementation
    private val _categoryId = MutableStateFlow(1L)
    override val categoryId: StateFlow<Long> = _categoryId.asStateFlow()

    private val _categoryName = MutableStateFlow("")
    override val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    private val _parentId = MutableStateFlow<Long?>(1L)
    override val parentId: StateFlow<Long?> = _parentId.asStateFlow()

    private val _isLeaf = MutableStateFlow(false)
    override val isLeaf: StateFlow<Boolean> = _isLeaf.asStateFlow()

    private val _selectedId = MutableStateFlow(1L)
    override val selectedId: StateFlow<Long> = _selectedId.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filters = MutableStateFlow<List<Filter>>(emptyList())
    val catDef = mutableStateOf("")
    val catBtn = mutableStateOf("")
    val enabledBtn = mutableStateOf(true)

    val categoryWithoutCounter = (isFilters || isCreateOffer)

    override fun onRefresh() {
        initialize(filters.value)
    }

    init {
        viewModelScope.launch {
            try {
                when {
                    isFilters -> {
                        catDef.value = getString(strings.selectCategory)
                        catBtn.value = getString(strings.actionAcceptFilters)
                    }
                    isCreateOffer -> {
                        catDef.value = getString(strings.selectCategory)
                        catBtn.value = getString(strings.continueLabel)
                        enabledBtn.value = selectedId.value != 1L
                    }
                    else -> {
                        catDef.value = getString(strings.categoryMain)
                        catBtn.value = getString(strings.categoryEnter)
                    }
                }
            }catch (_ : Exception){ }
        }
    }

    // Actions implementation
    override fun fetchCategories(
        searchData: SD,
        filters: List<Filter>,
        withoutCounter: Boolean
    ) {
        _isLoading.value = true
        val sd = searchData.copy(
            searchCategoryID = _categoryId.value,
            searchCategoryName = _categoryName.value,
            searchParentID = _parentId.value,
            searchIsLeaf = _isLeaf.value
        )
        val ld = LD(filters = filters.toMutableList() as ArrayList<Filter>)

        viewModelScope.launch {
            getCategories(sd, ld, withoutCounter) {
                _categories.value = it
                _isLoading.value = false
            }
        }
    }

    override fun navigateBack() {
        if (_categoryId.value != 1L) {
            _isLoading.value = true
            viewModelScope.launch {
                onCatBack(_parentId.value ?: 1L) { newCat ->
                    setUpNewParams(newCat)
                    fetchCategories(
                        SD(
                            searchCategoryID = _categoryId.value,
                            searchCategoryName = _categoryName.value,
                            searchParentID = _parentId.value,
                            searchIsLeaf = _isLeaf.value
                        ),
                        withoutCounter = categoryWithoutCounter,
                        filters = filters.value
                    )
                }
            }
        }
    }

    override fun resetToRoot() {
        if (_categoryId.value != 1L) {
            setUpNewParams(Category(id = 1L, name = catDef.value))
        }
        fetchCategories(
            SD(
                searchCategoryID = _categoryId.value,
                searchCategoryName = _categoryName.value,
                searchParentID = _parentId.value,
                searchIsLeaf = _isLeaf.value
            ),
            withoutCounter = categoryWithoutCounter,
            filters = filters.value
        )
    }

    override fun selectCategory(category: Category) {
        setUpNewParams(category)

        if (!category.isLeaf) {
            fetchCategories(
                SD(
                    searchCategoryID = _categoryId.value,
                    searchCategoryName = _categoryName.value,
                    searchParentID = _parentId.value,
                    searchIsLeaf = _isLeaf.value
                ),
                withoutCounter = categoryWithoutCounter,
                filters = filters.value
            )
        } else {
            _selectedId.value = category.id
        }
    }

    override fun initialize(filters: List<Filter>) {
        this.filters.value = filters
        if (_isLeaf.value) {
            _isLoading.value = true
            viewModelScope.launch {
                onCatBack(_parentId.value ?: 1L) { newCat ->
                    val cat = if (_parentId.value == newCat.id && newCat.isLeaf) {
                        newCat.copy(id = newCat.parentId, isLeaf = false)
                    } else {
                        newCat
                    }
                    setUpNewParams(cat)
                    fetchCategories(
                        SD(
                            searchCategoryID = _categoryId.value,
                            searchCategoryName = _categoryName.value,
                            searchParentID = _parentId.value,
                            searchIsLeaf = _isLeaf.value
                        ),
                        withoutCounter = categoryWithoutCounter,
                        filters = filters
                    )
                }
            }
        } else {
            fetchCategories(
                SD(
                    searchCategoryID = _categoryId.value,
                    searchCategoryName = _categoryName.value,
                    searchParentID = _parentId.value,
                    searchIsLeaf = _isLeaf.value
                ),
                withoutCounter = categoryWithoutCounter,
                filters = filters
            )
        }
    }

    private fun setUpNewParams(newCat: Category) {
        _categoryId.value = newCat.id
        _categoryName.value = newCat.name ?: catDef.value
        _parentId.value = newCat.parentId
        _isLeaf.value = newCat.isLeaf
        _selectedId.value = 1L
    }

    fun updateFromSearchData(searchData: SD) {
        _categoryId.value = searchData.searchCategoryID
        _categoryName.value = searchData.searchCategoryName
        _parentId.value = searchData.searchParentID
        _isLeaf.value = searchData.searchIsLeaf
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
