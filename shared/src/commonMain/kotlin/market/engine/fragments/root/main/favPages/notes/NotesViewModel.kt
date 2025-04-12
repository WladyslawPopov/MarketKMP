package market.engine.fragments.root.main.favPages.notes

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class NotesViewModel : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Offer>> {
        listingData.value.data.value.methodServer = "get_cabinet_listing_my_notes"
        listingData.value.data.value.objServer = "offers"

        return pagingRepository.getListing(listingData.value, apiService, Offer.serializer()).cachedIn(viewModelScope)
    }

    fun refresh(){
        updateUserInfo()
        pagingRepository.refresh()
    }
}
