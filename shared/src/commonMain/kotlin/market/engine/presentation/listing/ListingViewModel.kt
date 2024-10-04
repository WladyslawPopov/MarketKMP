package market.engine.presentation.listing

import androidx.paging.PagingData
import market.engine.core.network.ServerResponse
import market.engine.core.items.ListingData
import market.engine.core.networkObjects.Offer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import market.engine.core.network.ServerErrorException
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.presentation.root.BaseViewModel

class ListingViewModel(private val offerPagingRepository: OfferPagingRepository) : BaseViewModel() {

    private val _responseOffers = MutableStateFlow<List<Offer>>(emptyList())
    val responseOffers: StateFlow<List<Offer>> = _responseOffers.asStateFlow()


    fun getPage(listingData: ListingData): ServerResponse<Flow<PagingData<Offer>>> {
        return try {
            ServerResponse(success = offerPagingRepository.getListing(listingData))
        } catch (e : ServerErrorException){
            ServerResponse(error = e)
        } catch (e : Exception){
            ServerResponse(error = ServerErrorException(
                errorCode = e.message ?: "",
                humanMessage = e.message ?: "",
            )
            )
        }
    }
}
