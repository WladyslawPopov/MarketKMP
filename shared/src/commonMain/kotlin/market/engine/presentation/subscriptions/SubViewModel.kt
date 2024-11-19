package market.engine.presentation.subscriptions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import market.engine.core.network.APIService
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.presentation.base.BaseViewModel

class SubViewModel(
    private val apiService: APIService,
    private val offerPagingRepository: OfferPagingRepository
) : BaseViewModel() {


//    var globalData : FavBaseFilters = getKoin().get()
//    var listingData = globalData.listingData

    // StateFlow for the UI state
    val state: StateFlow<UiState>
//    val pagingDataFlow : Flow<PagingData<Subscription>>
    // Function to accept UI actions
    private val accept: (UiAction) -> Unit

    init {
//        listingData.data.value.methodServer = ""

        val actionStateFlow = MutableSharedFlow<UiAction>(replay = 1)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

        val searches = actionStateFlow
            .filterIsInstance<UiAction.UpdateCurrentListingData>()

        @OptIn(ExperimentalCoroutinesApi::class)
//        pagingDataFlow = searches
//            .flatMapLatest {
////                offerPagingRepository.getListing(it.listingData)
//            }.cachedIn(viewModelScope)

        state = searches.map {
            UiState(
                it.listingData
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState("")
        )
    }

    fun updateCurrentListingData() {
        viewModelScope.launch {
            accept(UiAction.UpdateCurrentListingData(""))
        }
    }

    data class UiState(
        val listingData: String
    )

    sealed class UiAction {
        data class UpdateCurrentListingData(val listingData: String) : UiAction()
    }
}
