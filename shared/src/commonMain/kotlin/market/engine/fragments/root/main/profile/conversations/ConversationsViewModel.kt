package market.engine.fragments.root.main.profile.conversations

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.events.CabinetConversationsItemEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.CabinetConversationsItemState
import market.engine.core.data.states.SelectedOfferItemState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class ConversationsViewModel(val component: ConversationsComponent): CoreViewModel() {

    private val pagingRepository: PagingRepository<Conversations> = PagingRepository()

    private val conversationsOperations : ConversationsOperations by lazy { getKoin().get() }

    val listingBaseViewModel = ListingBaseViewModel(
        deleteSelectedItems = {
            deleteSelectsItems()
        }
    )

    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
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
                    val selectItems = listingBaseViewModel.selectItems

                    CabinetConversationsItemState(
                        conversation = conversation,
                        events = ConversationEventsImpl(
                            conversation = conversation,
                            viewModel = this,
                            component = component
                        ),
                        selectedItem = SelectedOfferItemState(
                            selected = selectItems.value,
                            onSelectionChange = { id ->
                                if (!selectItems.value.contains(id)) {
                                    listingBaseViewModel.addSelectItem(id)
                                } else {
                                    listingBaseViewModel.removeSelectItem(id)
                                }
                            }
                        )
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PagingData.empty()
        ).cachedIn(viewModelScope)

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
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.FILTERS)
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.value.data.sort != null,
                            badgeCount = null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
                            }
                        )
                    )
                }
            )
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
