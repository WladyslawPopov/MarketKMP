package market.engine.fragments.root.main.profile.conversations

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class ConversationsViewModel: BaseViewModel() {

    private val conversationsPagingRepository: PagingRepository<Conversations> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Conversations>> {
        listingData.value.data.value.filters = MsgFilters.filters
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "conversations"

        return conversationsPagingRepository.getListing(listingData.value, apiService, Conversations.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        conversationsPagingRepository.refresh(listingData.value)
    }
}
