package market.engine.fragments.root.main.home

import androidx.compose.runtime.mutableStateOf
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.TopCategory
import market.engine.core.network.networkObjects.Category
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val promoOffers1: List<OfferItem> = emptyList(),
    val promoOffers2: List<OfferItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: ServerErrorException = ServerErrorException(),
    val unreadNotificationsCount: Int? = null
)

class HomeViewModel : BaseViewModel() {

    private val _responseOffersPromotedOnMainPage1 = MutableStateFlow<List<OfferItem>>(emptyList())
    private val _responseOffersPromotedOnMainPage2 = MutableStateFlow<List<OfferItem>>(emptyList())
    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())

    val uiState: StateFlow<HomeUiState> = combine(
        _responseCategory,
        _responseOffersPromotedOnMainPage1,
        _responseOffersPromotedOnMainPage2,
        isShowProgress,
        errorMessage
    ) { categories, promoOffers1, promoOffers2, isLoading, error ->
        HomeUiState(
            categories = categories,
            promoOffers1 = promoOffers1,
            promoOffers2 = promoOffers2,
            isLoading = isLoading,
            error = error,
            unreadNotificationsCount = getUnreadNotificationsCount()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState()
    )

    val listFooter = mutableStateOf<List<TopCategory>>(emptyList())
    val listAppBar = mutableStateOf<List<NavigationItem>>(emptyList())
    val drawerList = mutableStateOf<List<NavigationItem>>(emptyList())


    fun setCategory(category: List<Category>) {
        _responseCategory.value = category
    }

    fun getOffersPromotedOnMainPage(page: Int, ipp: Int) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val response = withContext(Dispatchers.IO) {
                    apiService.getOffersPromotedOnMainPage(page, ipp)
                }
                val serializer = Payload.serializer(Offer.serializer())
                val payload: Payload<Offer> = deserializePayload(response.payload, serializer)
                when(page){
                    0 -> _responseOffersPromotedOnMainPage1.value = payload.objects.map { it.parseToOfferItem() }
                    1 -> _responseOffersPromotedOnMainPage2.value = payload.objects.map { it.parseToOfferItem() }
                }
            } catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError(ServerErrorException(exception.message ?: "Unknown error", "An error occurred"))
            } finally {
                setLoading(false)
            }
        }
    }
}
