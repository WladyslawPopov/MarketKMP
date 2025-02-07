package market.engine.fragments.root.main.profile.conversations

import androidx.compose.runtime.mutableStateOf
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel

class ConversationsViewModel(
    private val conversationsOperations: ConversationsOperations
) : BaseViewModel() {

    private val conversationsPagingRepository: PagingRepository<Conversations> = PagingRepository()

    val listingData = mutableStateOf(ListingData())

    fun init(): Flow<PagingData<Conversations>> {
        listingData.value.data.value.filters = MsgFilters.filters
        listingData.value.data.value.methodServer = "get_cabinet_listing"
        listingData.value.data.value.objServer = "conversations"

        return conversationsPagingRepository.getListing(listingData.value, apiService, Conversations.serializer()).cachedIn(viewModelScope)
    }

    fun onRefresh(){

        conversationsPagingRepository.refresh()
    }

    suspend fun getConversation(id : Long) : Conversations? {
        try {
            val res = withContext(Dispatchers.IO) {
                conversationsOperations.getConversation(id)
            }

            return withContext(Dispatchers.Main) {
                return@withContext res
            }
        }catch (e : ServerErrorException){
            onError(e)
            return null
        }catch (e : Exception){
            onError(ServerErrorException(e.message ?: "", ""))
            return null
        }
    }

    suspend fun deleteConversation(id : Long) : Boolean {
        try {
            val res = withContext(Dispatchers.IO) {
                conversationsOperations.postDeleteForInterlocutor(id)
            }

            return withContext(Dispatchers.Main) {
                return@withContext res != null
            }
        }catch (e : ServerErrorException){
            onError(e)
            return false
        }catch (e : Exception){
            onError(ServerErrorException(e.message ?: "", ""))
            return false
        }
    }

    fun markReadConversation(id : Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Unconfined) {
                    conversationsOperations.postMarkAsReadByInterlocutor(id)
                }
            } catch (e: ServerErrorException) {
                onError(e)
            } catch (e: Exception) {
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }
}
