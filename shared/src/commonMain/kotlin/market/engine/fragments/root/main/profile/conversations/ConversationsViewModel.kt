package market.engine.fragments.root.main.profile.conversations

import androidx.compose.ui.text.AnnotatedString
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import market.engine.common.Platform
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.MsgFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.events.CabinetConversationsItemEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.states.CabinetConversationsItemState
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ListingOfferContentState
import market.engine.core.data.states.SelectedOfferItemState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.Conversations
import market.engine.core.network.networkObjects.Fields
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.BaseViewModel
import org.jetbrains.compose.resources.getString

class ConversationsViewModel(val component: ConversationsComponent): BaseViewModel() {

    private val pagingRepository: PagingRepository<Conversations> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData(
        data = LD().copy(
            filters = MsgFilters.filters,
            methodServer = "get_cabinet_listing",
            objServer = "conversations"
        ),
    ))

    private val _activeWindowType = MutableStateFlow(ActiveWindowListingType.LISTING)
    val showOperationsDialog = MutableStateFlow("")
    val titleDialog = MutableStateFlow(AnnotatedString(""))
    val fieldsDialog = MutableStateFlow< ArrayList<Fields>>(arrayListOf())
    val dialogItemId = MutableStateFlow(1L)

    val pagingParamsFlow: Flow<ListingData> = combine(
        _listingData,
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
                totalCount.update {
                    tc
                }
            }.map { pagingData ->
                pagingData.map { conversation  ->
                    CabinetConversationsItemState(
                        conversation = conversation,
                        events = ConversationEventsImpl(
                            conversation = conversation,
                            viewModel = this,
                            component = component
                        ),
                        selectedItem = SelectedOfferItemState(
                            selected = selectItems,
                            onSelectionChange = { id ->
                                if (!selectItems.contains(id)) {
                                    selectItems.add(id)
                                } else {
                                    selectItems.remove(id)
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

    val uiDataState: StateFlow<ListingOfferContentState> = combine(
        _activeWindowType,
        _listingData,
    ) { activeType, listingData ->
        val ld = listingData.data
        val filterString = getString(strings.filter)
        val sortString = getString(strings.sort)
        val filters = ld.filters.filter { it.value != "" && it.interpretation?.isNotBlank() == true }

        ListingOfferContentState(
            appBarData = SimpleAppBarData(
                color = colors.primaryColor,
                onBackClick = {
                    onBackNavigation(activeType)
                },
                listItems = listOf(
                    NavigationItem(
                        title = "",
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        hasNews = false,
                        isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                        badgeCount = null,
                        onClick = { refresh() }
                    ),
                )
            ),
            listingData = listingData,
            filterBarData = FilterBarUiState(
                listFiltersButtons = buildList {
                    filters.forEach { filter ->
                        filter.interpretation?.let { text ->
                            add(
                                FilterListingBtnItem(
                                    text = text,
                                    itemClick = {
                                        _activeWindowType.value = ActiveWindowListingType.FILTERS
                                    },
                                    removeFilter = {
                                        removeFilter(filter)
                                    }
                                )
                            )
                        }
                    }
                    if (ld.sort != null) {
                        add(
                            FilterListingBtnItem(
                                text = sortString,
                                itemClick = {
                                    _activeWindowType.value = ActiveWindowListingType.SORTING
                                },
                                removeFilter = {
                                    removeSort()
                                }
                            )
                        )
                    }
                },
                listNavigation = buildList {
                    add(
                        NavigationItem(
                            title = filterString,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowListingType.FILTERS
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.sort != null,
                            badgeCount = null,
                            onClick = {
                                _activeWindowType.value = ActiveWindowListingType.SORTING
                            }
                        )
                    )
                }
            ),
            listingBaseState = ListingBaseState(
                listingData = listingData.data,
                searchData = listingData.searchData,
                activeWindowType = activeType,
                columns = if(isBigScreen.value) 2 else 1,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ListingOfferContentState()
    )

    fun updatePage(){
        updatePage.value++
    }
    fun onBackNavigation(activeType: ActiveWindowListingType){
        if (activeType != ActiveWindowListingType.LISTING) {
            _activeWindowType.value = ActiveWindowListingType.LISTING
        }else{
            component.onBack()
        }
    }

    fun applyFilters(newFilters: List<Filter>) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    filters = newFilters
                )
            )
        }
        refresh()
        _activeWindowType.value = ActiveWindowListingType.LISTING
    }
    fun applySorting(newSort: Sort?) {
        _listingData.update { currentState ->
            currentState.copy(
                data = currentState.data.copy(
                    sort = newSort
                )
            )
        }
        refresh()
        _activeWindowType.value = ActiveWindowListingType.LISTING
    }
    fun removeFilter(filter: Filter){
        _listingData.update { currentListingData ->
            val currentData = currentListingData.data
            val newFilters = currentData.filters.map { filterItem ->
                if (filterItem.key == filter.key && filterItem.operation == filter.operation) {
                    filterItem.copy(value = "", interpretation = null)
                } else {
                    filterItem
                }
            }
            currentListingData.copy(
                data = currentData.copy(filters = newFilters)
            )
        }
        refresh()
    }
    fun removeSort(){
        _listingData.update {
            it.copy(data = it.data.copy(sort = null))
        }
        refresh()
    }
    fun clearAllFilters() {
        MsgFilters.clearFilters()
        _listingData.update {
            it.copy(
                data = it.data.copy(filters = MsgFilters.filters)
            )
        }
        refresh()
    }
    fun clearDialogFields(){
        dialogItemId.value = 1
        fieldsDialog.value.clear()
        showOperationsDialog.value = ""
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

                updateItem.value = null
            },
            error = {
                updateItem.value = null
            }
        )
    }


    fun deleteSelectsItems() {
        viewModelScope.launch {
            selectItems.forEach { item ->
                deleteConversation(item){
                    selectItems.remove(item)
                }
            }
            if (selectItems.isEmpty()){
                updateUserInfo()
                refresh()
            }
        }
    }

    fun clearSelection() {
        selectItems.clear()
    }
}

data class ConversationEventsImpl(
    val conversation: Conversations,
    val viewModel: ConversationsViewModel,
    val component: ConversationsComponent
) : CabinetConversationsItemEvents {
    override fun goToMessenger() {
        if (viewModel.selectItems.isNotEmpty()) {
            if (!viewModel.selectItems.contains(conversation.id)) {
                viewModel.selectItems.add(conversation.id)
            } else {
                viewModel.selectItems.remove(conversation.id)
            }
        } else {
            component.goToMessenger(conversation)
        }
    }

    override fun updateItem() {
        viewModel.updateItem(conversation)
    }
}
