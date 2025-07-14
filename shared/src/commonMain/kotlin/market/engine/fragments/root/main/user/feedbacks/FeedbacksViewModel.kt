package market.engine.fragments.root.main.user.feedbacks

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.filtersObjects.ReportFilters
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ReportPageType
import market.engine.core.network.networkObjects.Reports
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import org.jetbrains.compose.resources.getString

class FeedbacksViewModel(val type : ReportPageType, val userId : Long) : CoreViewModel() {

    private val pagingRepository: PagingRepository<Reports> = PagingRepository()

    val listingBaseViewModel = ListingBaseViewModel()

    private val listingData = listingBaseViewModel.listingData

    val filters = mutableListOf<String>()

    val currentFilter = mutableStateOf("")

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                filters.addAll(
                    listOf(
                        getString(strings.allFilterParams),
                        getString(strings.positiveFilterParams),
                        getString(strings.negativeFilterParams),
                        getString(strings.neutralFilterParams)
                    )
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
        currentFilter.value = if(
            listingData.data.filters.find {
                it.key == "evaluation" }?.value == "" ||
            listingData.data.filters.find { it.key == "evaluation" }?.value == null
        ) {
            filters[0]
        }else{
            filters[(listingData.data.filters.find { it.key == "evaluation" }?.value?.toInt() ?: 0) + 1]
        }

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
    }.stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        PagingData.empty()
    ).cachedIn(viewModelScope)

    fun refreshListing(){
        refresh()
        ReportFilters.clearTypeFilter(type)
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
        currentFilter.value = filter

        val listingData = listingData.value.data.copy()

        when (filters.indexOf(filter)) {
            0 -> {
                listingData.filters.find { it.key == "evaluation" }?.value = ""
                listingData.filters.find { it.key == "evaluation" }?.interpretation = null
            }

            1 -> {
                listingData.filters.find { it.key == "evaluation" }?.value = "0"
                listingData.filters.find { it.key == "evaluation" }?.interpretation = ""
            }

            2 -> {
                listingData.filters.find { it.key == "evaluation" }?.value = "1"
                listingData.filters.find { it.key == "evaluation" }?.interpretation = ""
            }

            3 -> {
                listingData.filters.find { it.key == "evaluation" }?.value = "2"
                listingData.filters.find { it.key == "evaluation" }?.interpretation = ""
            }
        }

        listingBaseViewModel.setListingData(
            listingBaseViewModel.listingData.value.copy(
                data = listingData
            )
        )
    }
}
