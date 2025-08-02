package market.engine.fragments.root.main.user.feedbacks

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.ReportFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ReportPageType
import market.engine.core.network.networkObjects.Reports
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.getSavedStateFlow
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import org.jetbrains.compose.resources.getString

class FeedbacksViewModel(val type : ReportPageType, val userId : Long, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    private val pagingRepository: PagingRepository<Reports> = PagingRepository()

    val listingBaseViewModel = ListingBaseViewModel(savedStateHandle = savedStateHandle)

    private val listingData = listingBaseViewModel.listingData

    private val _filters = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "filters",
        emptyList(),
        ListSerializer(String.serializer())
    )
    val filters = _filters.state

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _filters.value = listOf(
                    getString(strings.allFilterParams),
                    getString(strings.positiveFilterParams),
                    getString(strings.negativeFilterParams),
                    getString(strings.neutralFilterParams)
                )
            }
            withContext(Dispatchers.Main) {
                refreshListing()
            }
        }
    }

    val pagingParamsFlow: Flow<ListingData> = combine(
        listingData,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<Reports>> = pagingParamsFlow.flatMapLatest{ listingData ->
        val serializer = Reports.serializer()
        pagingRepository.getListing(
            listingData,
            apiService,
            serializer,
            onTotalCountReceived = {
                listingBaseViewModel.setTotalCount(it)
            }
        )
    }.cachedIn(viewModelScope)

    fun refreshListing(){
        refresh()
        val newFilters = ReportFilters.getByTypeFilter(type)
        newFilters.find { it.key == "user_id" }?.value = userId.toString()

        listingBaseViewModel.setListingData(
            ListingData(
                data = LD(
                    filters = newFilters,
                    methodServer = "get_public_listing",
                    objServer = "feedbacks"
                )
            )
        )
    }

    fun setNewFilter(filter : String){
        val allFilterKey = _filters.value.getOrNull(0)
        val positiveFilterKey = _filters.value.getOrNull(2)
        val negativeFilterKey = _filters.value.getOrNull(1)
        val neutralFilterKey = _filters.value.getOrNull(3)

        val originalFilters = listingBaseViewModel.listingData.value.data.filters

        val newFilters = originalFilters.map { filterItem ->
            if (filterItem.key == "evaluation") {
                val newEvaluationFilter = filterItem.copy()
                when (filter) {
                    allFilterKey -> {
                        newEvaluationFilter.value = ""
                        newEvaluationFilter.interpretation = null
                    }
                    positiveFilterKey -> {
                        newEvaluationFilter.value = "1"
                        newEvaluationFilter.interpretation = ""
                    }
                    negativeFilterKey -> {
                        newEvaluationFilter.value = "0"
                        newEvaluationFilter.interpretation = ""
                    }
                    neutralFilterKey -> {
                        newEvaluationFilter.value = "2"
                        newEvaluationFilter.interpretation = ""
                    }
                }
                newEvaluationFilter
            } else {
                filterItem
            }
        }

        listingBaseViewModel.applyFilters(newFilters)
    }
}
