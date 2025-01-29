package market.engine.fragments.user.feedbacks

import androidx.compose.runtime.mutableStateOf
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.filtersObjects.ReportFilters
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.ReportPageType
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Reports
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class FeedbacksViewModel(
    val apiService: APIService
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Reports> = PagingRepository()

    val listingData = mutableStateOf(ListingData())
    val currentFilter = mutableStateOf("")

    fun init(type : ReportPageType, userId : Long) : Flow<PagingData<Reports>> {
        when(type){
            ReportPageType.ALL_REPORTS ->{
                listingData.value.data.value.filters.addAll(ReportFilters.filtersAll)
                listingData.value.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            ReportPageType.FROM_BUYERS ->{
                listingData.value.data.value.filters = arrayListOf()
                listingData.value.data.value.filters.addAll(ReportFilters.filtersFromBuyers)
                listingData.value.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            ReportPageType.FROM_SELLERS ->{
                listingData.value.data.value.filters.addAll(ReportFilters.filtersFromSellers)
                listingData.value.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            ReportPageType.FROM_USER ->{
                listingData.value.data.value.filters.addAll(ReportFilters.filtersFromUsers)
                listingData.value.data.value.filters.find { it.key == "user_id" }?.value = userId.toString()
            }
            else -> {}
        }
        listingData.value.data.value.methodServer = "get_public_listing"
        listingData.value.data.value.objServer = "feedbacks"

        val serializer = Reports.serializer()


        return pagingRepository.getListing(listingData.value, apiService, serializer)
            .cachedIn(viewModelScope)

    }

    fun refresh(){
        pagingRepository.refresh()
    }
}
