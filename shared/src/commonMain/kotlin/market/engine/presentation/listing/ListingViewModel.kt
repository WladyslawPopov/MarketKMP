package market.engine.presentation.listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.globalData.CategoryBaseFilters
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.network.networkObjects.deserializePayload
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.presentation.base.BaseViewModel
import org.koin.mp.KoinPlatform.getKoin

class ListingViewModel(
    private val apiService: APIService,
    private val offerPagingRepository: OfferPagingRepository
) : BaseViewModel() {

    val settings : Settings = getKoin().get()

    var firstVisibleItemIndex by mutableStateOf(0)
    var firstVisibleItemScrollOffset by mutableStateOf(0)

    var globalData : CategoryBaseFilters = getKoin().get()
    var listingData = globalData.listingData

    // StateFlow for the UI state
    val state: StateFlow<UiState>
    val pagingDataFlow : Flow<PagingData<Offer>>
    // Function to accept UI actions
    private val accept: (UiAction) -> Unit

    init {
        listingData.data.value.methodServer = "get_public_listing"

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

        if (settings.keys.find { it == "listingType" } != null){
            val listingType = settings["listingType", 0]
            listingData.data.value.listingType = listingType
        }else{
            settings["listingType"] = listingData.data.value.listingType
        }
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

    private val defaultCategoryId = 1L
    private var _responseOffersRecommendedInListing = MutableStateFlow<ArrayList<Offer>?>(null)
    val responseOffersRecommendedInListing : StateFlow<ArrayList<Offer>?> = _responseOffersRecommendedInListing.asStateFlow()

    fun getOffersRecommendedInListing(categoryID:Long=defaultCategoryId) {
        viewModelScope.launch{
            try {
                withContext(Dispatchers.IO){
                    val response = apiService.getOffersRecommendedInListing(categoryID)

                    withContext(Dispatchers.Main) {
                        try {
                            val payload : Payload<Offer> = deserializePayload(response.payload)
                            _responseOffersRecommendedInListing.value = payload.objects
                        }catch (e : Exception){
                            throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                        }
                    }
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(errorCode = exception.message.toString(), humanMessage = exception.message.toString()))
            }
        }
    }
}
