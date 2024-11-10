package market.engine.presentation.profile

import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import market.engine.core.baseFilters.Filter
import market.engine.core.baseFilters.ProfileBaseFilters
import market.engine.core.filtersObjects.OfferFilters
import market.engine.core.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.core.repositories.SettingsRepository
import market.engine.core.types.LotsType
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class ProfileViewModel(
    val type: LotsType,
    apiService: APIService,
    private val offerPagingRepository: OfferPagingRepository
) : BaseViewModel() {
    val settings : SettingsRepository = getKoin().get()

    private val categoryOperations : CategoryOperations = getKoin().get()
    var firstVisibleItemIndex by mutableStateOf(0)
    var firstVisibleItemScrollOffset by mutableStateOf(0)
    var isHideContent = mutableStateOf(false)
    var isFirstSetUp = mutableStateOf(true)
    var activeFiltersType = mutableStateOf("")
    @OptIn(ExperimentalMaterialApi::class)
    val bottomSheetState = mutableStateOf(BottomSheetValue.Collapsed)

    private val globalData = ProfileBaseFilters
    var listingData = globalData.listingData.copy()

    private val filtersMap: Map<LotsType, ArrayList<Filter>> = mapOf(
        LotsType.MYLOT_ACTIVE to OfferFilters.filtersMyLotsActive,
        LotsType.MYLOT_UNACTIVE to OfferFilters.filtersMyLotsUnactive,
        LotsType.MYLOT_FUTURE to OfferFilters.filtersMyLotsFuture
    )

    init {
        updateCurrentListingData()
    }

    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Offer>>

    private val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>(replay = 1)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

        val searches = actionStateFlow
            .filterIsInstance<UiAction.UpdateCurrentListingData>()

        @OptIn(ExperimentalCoroutinesApi::class)
        pagingDataFlow = searches
            .flatMapLatest {
                offerPagingRepository.getListing(it.listingData)
            }.cachedIn(viewModelScope)

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

    fun updateCurrentListingData(type: LotsType = this.type) {
        listingData.data.value.filters = arrayListOf()
        listingData.data.value.filters?.addAll(filtersMap[type] ?: arrayListOf())
        listingData.data.value.methodServer = "get_cabinet_listing"
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
