package market.engine.fragments.root.main.profile.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.events.CabinetConversationsItemEvents
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.CabinetConversationsItemState
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class ConversationsViewModel(val component: ConversationsComponent, savedStateHandle: SavedStateHandle): CoreViewModel(savedStateHandle) {

    private val pagingRepository: PagingRepository<Conversations> = PagingRepository()

    private val conversationsOperations : ConversationsOperations by lazy { getKoin().get() }

    val listingBaseViewModel = ListingBaseViewModel(
        deleteSelectedItems = {
            deleteSelectsItems()
        },
        savedStateHandle = savedStateHandle
    )

    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<CabinetConversationsItemState>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Conversations.serializer()
            ){ tc ->
                listingBaseViewModel.setTotalCount(tc)
            }.map { pagingData ->
                pagingData.map { conversation  ->
                    CabinetConversationsItemState(
                        conversation = conversation,
                        events = ConversationEventsImpl(
                            conversation = conversation,
                            viewModel = this,
                            component = component
                        )
                    )
                }
            }
        }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            listingBaseViewModel.setListingData(
                listingBaseViewModel.listingData.value.copy(
                    data = LD(
                        filters = MsgFilters.filters,
                        methodServer = "get_cabinet_listing",
                        objServer = "conversations"
                    )
                )
            )
            listingBaseViewModel.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val filters = ld.value.data.filters.filter {
                        it.value != "" &&
                                it.interpretation?.isNotBlank() == true
                    }

                    add(
                        NavigationItem(
                            title = filterString,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            hasNews = ld.value.data.sort != null,
                            badgeCount = null,
                        )
                    )
                }
            )

            val eventParameters = mapOf(
                "user_id" to UserData.login.toString(),
                "profile_source" to "messages"
            )
            analyticsHelper.reportEvent("view_seller_profile", eventParameters)
        }
    }

    fun updateItem(oldItem: Conversations) {
        getConversation(
            oldItem.id,
            onSuccess = { res->
                oldItem.interlocutor = res.interlocutor
                oldItem.newMessage = res.newMessage
                oldItem.newMessageTs = res.newMessageTs
                oldItem.countUnreadMessages = res.countUnreadMessages
                oldItem.aboutObjectIcon = res.aboutObjectIcon

                setUpdateItem(null)
            },
            error = {
                setUpdateItem(null)
            }
        )
    }

    fun deleteSelectsItems() {
        val selectItems = listingBaseViewModel.selectItems
        viewModelScope.launch {
            selectItems.value.forEach { item ->
                deleteConversation(item){
                    listingBaseViewModel.removeSelectItem(item)
                }
            }
            if (selectItems.value.isEmpty()){
                updateUserInfo()
                refresh()
            }
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

    fun deleteConversation(id : Long, onSuccess : () -> Unit) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                conversationsOperations.postDeleteForInterlocutor(id)
            }

            withContext(Dispatchers.Main) {
                if(res != null){
                    onSuccess()
                }else{
                    showToast(errorToastItem.copy(message = getString(strings.operationFailed)))
                }
            }
        }
    }

    fun getConversation(id : Long, onSuccess: (Conversations) -> Unit, error: () -> Unit) {
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    conversationsOperations.getConversation(id)
                }
                val buf = res.success
                val e = res.error
                withContext(Dispatchers.Main) {
                    if (buf!= null) {
                        onSuccess(res.success!!)
                    }else{
                        error()
                        e?.let { throw it }
                    }
                }
            }catch (e : ServerErrorException){
                onError(e)
            }catch (e : Exception){
                onError(ServerErrorException(e.message ?: "", ""))
            }
        }
    }
}

data class ConversationEventsImpl(
    val conversation: Conversations,
    val viewModel: ConversationsViewModel,
    val component: ConversationsComponent
) : CabinetConversationsItemEvents {
    override fun goToMessenger() {
        val selectedItems = viewModel.listingBaseViewModel.selectItems

        if (selectedItems.value.isNotEmpty()) {
            if (!selectedItems.value.contains(conversation.id)) {
                viewModel.listingBaseViewModel.addSelectItem(conversation.id)
            } else {
                viewModel.listingBaseViewModel.removeSelectItem(conversation.id)
            }
        } else {
            component.goToMessenger(conversation)
        }
    }

    override fun updateItem() {
        viewModel.updateItem(conversation)
    }
}
