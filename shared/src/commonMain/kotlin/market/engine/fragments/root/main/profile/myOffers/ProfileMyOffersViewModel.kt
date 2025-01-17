package market.engine.fragments.root.main.profile.myOffers

import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.items.ListingData
import market.engine.core.data.types.LotsType
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.paging.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class ProfileMyOffersViewModel(
    val type: LotsType,
    val apiService: APIService,
    val userRepository: UserRepository
) : BaseViewModel() {

    private val offerPagingRepository: PagingRepository<Offer> = PagingRepository()

    fun init(listingData : ListingData): Flow<PagingData<Offer>> {
        when(type){
            LotsType.MYLOT_ACTIVE -> {
                listingData.data.value.filters.clear()
                listingData.data.value.filters.addAll( OfferFilters.filtersMyLotsActive.toList())
            }
            LotsType.MYLOT_UNACTIVE -> {
                listingData.data.value.filters.clear()
                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsUnactive.toList())
            }
            LotsType.MYLOT_FUTURE -> {
                listingData.data.value.filters.clear()
                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsFuture.toList())
            }
            else -> {
                listingData.data.value.filters.clear()
                listingData.data.value.filters.addAll(OfferFilters.filtersMyLotsActive.toList())
            }
        }

        listingData.data.value.methodServer = "get_cabinet_listing"
        listingData.data.value.objServer = "offers"

        return offerPagingRepository.getListing(listingData, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        offerPagingRepository.refresh()
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateToken()
            userRepository.updateUserInfo()
        }
    }
}
