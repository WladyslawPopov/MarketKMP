package market.engine.fragments.root.main.profile.conversations

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import market.engine.core.data.items.ListingData
import market.engine.core.network.APIService
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.repositories.PagingRepository
import market.engine.core.repositories.UserRepository
import market.engine.fragments.base.BaseViewModel

class ConversationsViewModel(
    val apiService: APIService,
    val userRepository: UserRepository
) : BaseViewModel() {

    private val conversationsPagingRepository: PagingRepository<Conversations> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Conversations>> {

        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "conversations"

        return conversationsPagingRepository.getListing(listingData.value, apiService, Conversations.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){
        conversationsPagingRepository.refresh()
    }

    fun updateUserInfo(){
        viewModelScope.launch {
            userRepository.updateToken()
            userRepository.updateUserInfo()
        }
    }
}
