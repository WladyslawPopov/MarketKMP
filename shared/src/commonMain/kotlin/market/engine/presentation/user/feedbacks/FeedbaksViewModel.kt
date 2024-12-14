package market.engine.presentation.user.feedbacks

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.filtersObjects.ReportFilters
import market.engine.core.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Reports
import market.engine.core.network.paging.PagingRepository
import market.engine.core.types.ReportPageType
import market.engine.presentation.base.BaseViewModel

class FeedbacksViewModel(
    val apiService: APIService
) : BaseViewModel() {
    private val pagingRepository: PagingRepository<Reports> = PagingRepository()
    val listingData = ListingData()

    val currentFilter = mutableStateOf("")

    fun init(type : ReportPageType, userId : Long) : Flow<PagingData<Reports>> {
        when(type){
            ReportPageType.ALL_REPORTS ->{
                listingData.data.value.filters.addAll(ReportFilters.filtersAll)
                listingData.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            ReportPageType.FROM_BUYERS ->{
                listingData.data.value.filters = arrayListOf()
                listingData.data.value.filters.addAll(ReportFilters.filtersFromBuyers)
                listingData.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            ReportPageType.FROM_SELLERS ->{
                listingData.data.value.filters.addAll(ReportFilters.filtersFromSellers)
                listingData.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            ReportPageType.FROM_USER ->{
                listingData.data.value.filters.addAll(ReportFilters.filtersFromUsers)
                listingData.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            else -> {}
        }
        listingData.data.value.methodServer = "get_public_listing"
        listingData.data.value.objServer = "feedbacks"

        val serializer = Reports.serializer()

        return pagingRepository.getListing(listingData, apiService, serializer)
            .cachedIn(viewModelScope)
    }

    fun refresh(){
        pagingRepository.refresh()
    }
}
