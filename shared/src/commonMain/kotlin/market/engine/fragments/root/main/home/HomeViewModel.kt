package market.engine.fragments.root.main.home

import androidx.compose.runtime.mutableStateOf
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.TopCategory
import market.engine.core.network.networkObjects.Category
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class HomeViewModel : BaseViewModel() {

    private val _responseOffersPromotedOnMainPage1 = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseOffersPromotedOnMainPage1: StateFlow<List<OfferItem>> = _responseOffersPromotedOnMainPage1.asStateFlow()

    private val _responseOffersPromotedOnMainPage2 = MutableStateFlow<List<OfferItem>>(emptyList())
    val responseOffersPromotedOnMainPage2: StateFlow<List<OfferItem>> = _responseOffersPromotedOnMainPage2.asStateFlow()

    private val _responseCategory = MutableStateFlow<List<Category>>(emptyList())
    val responseCategory: StateFlow<List<Category>> = _responseCategory.asStateFlow()

    val listFooter = mutableListOf<TopCategory>()

    val listAppBar = mutableStateOf<List<NavigationItem>>(emptyList())

    val drawerList = mutableStateOf<List<NavigationItem>>(emptyList())

    init {
        viewModelScope.launch {
            listFooter.clear()
            listFooter.addAll(
                listOf(
                    TopCategory(
                        id = 1,
                        name = getString(strings.homeFixAuction),
                        icon = drawables.auctionFixIcon
                    ),
                    TopCategory(
                        id = 2,
                        name = getString(strings.homeManyOffers),
                        icon = drawables.manyOffersIcon
                    ),
                    TopCategory(
                        id = 3,
                        name = getString(strings.verifySellers),
                        icon = drawables.verifySellersIcon
                    ),
                    TopCategory(
                        id = 4,
                        name = getString(strings.everyDeyDiscount),
                        icon = drawables.discountBigIcon
                    ),
                    TopCategory(
                        id = 5,
                        name = getString(strings.freeBilling),
                        icon = drawables.freeBillingIcon
                    ),
                )
            )
        }
    }

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
