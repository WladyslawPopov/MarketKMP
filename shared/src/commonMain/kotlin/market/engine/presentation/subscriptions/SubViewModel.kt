package market.engine.presentation.subscriptions

import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import market.engine.core.items.ListingData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.baseFilters.FavBaseFilters
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Subscription
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class SubViewModel(
    private val apiService: APIService,
    private val offerPagingRepository: OfferPagingRepository
) : BaseViewModel() {

    var firstVisibleItemIndex by mutableStateOf(0)
    var firstVisibleItemScrollOffset by mutableStateOf(0)

    var activeFiltersType = mutableStateOf("")
    @OptIn(ExperimentalMaterialApi::class)
    val bottomSheetState = mutableStateOf(BottomSheetValue.Collapsed)

    var globalData : FavBaseFilters = getKoin().get()
    var listingData = globalData.listingData

    // StateFlow for the UI state
    val state: StateFlow<UiState>
//    val pagingDataFlow : Flow<PagingData<Subscription>>
    // Function to accept UI actions
    private val accept: (UiAction) -> Unit

    init {
        listingData.data.value.methodServer = ""

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
            initialValue = UiState(listingData.copy())
        )
    }

    fun updateCurrentListingData() {
        viewModelScope.launch {
            accept(UiAction.UpdateCurrentListingData(listingData))
        }
    }

    data class UiState(
        val listingData: ListingData
    )

    sealed class UiAction {
        data class UpdateCurrentListingData(val listingData: ListingData) : UiAction()
    }
}
