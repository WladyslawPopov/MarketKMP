package market.engine.fragments.root.main.profile.myProposals

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
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
import kotlinx.coroutines.withContext
import market.engine.common.Platform
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.CabinetOfferItemState
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ListingOfferContentState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.OfferRepository
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import kotlin.collections.map

class MyProposalsViewModel(
    val type: LotsType,
    val component: MyProposalsComponent
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData(
        data = LD().copy(
            filters = OfferFilters.getByTypeFilter(type),
            methodServer = "get_cabinet_listing_my_price_proposals",
            objServer = "offers"
        ),
    ))

    private val _activeWindowType = MutableStateFlow(ActiveWindowListingType.LISTING)

    val pagingParamsFlow: Flow<ListingData> = combine(
        _listingData,
        updatePage
    ) { listingData, _ ->
        listingData
    }

    private val filtersCategoryModel = CategoryViewModel(
        isFilters = true,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<CabinetOfferItemState>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Offer.serializer()
            ){ tc ->
                totalCount.update {
                    tc
                }
            }.map { pagingData ->
                pagingData.map { offer ->
                    val item = offer.parseToOfferItem()
                    CabinetOfferItemState(
                        item = item,
                        offerRepository = OfferRepository(
                            offer= item,
                            events = OfferRepositoryEventsImpl(this, item, component),
                            this
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

        filtersCategoryModel.updateFromSearchData(listingData.searchData)
        filtersCategoryModel.initialize(listingData.data.filters)

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
            filtersCategoryState = CategoryState(
                openCategory = activeType == ActiveWindowListingType.CATEGORY,
                categoryViewModel = filtersCategoryModel
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
        when(activeType){
            ActiveWindowListingType.CATEGORY_FILTERS -> {
                if (filtersCategoryModel.categoryId.value != 1L){
                    filtersCategoryModel.navigateBack()
                }else{
                    _activeWindowType.value = ActiveWindowListingType.LISTING
                }
            }
            else -> {
                _activeWindowType.value = ActiveWindowListingType.LISTING
            }
        }
    }
    fun updateItem(oldItem : OfferItem){
        viewModelScope.launch {
            val offer = withContext(Dispatchers.IO) {
                getOfferById(oldItem.id)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    oldItem.setNewParams(offer)
                }else{
                    oldItem.session = null
                    oldItem.state = null
                }
                if (!isHideItem(oldItem)) {
                    updatePage()
                }
                updateItem.value = null
            }
        }
    }
    fun isHideItem(offer: OfferItem): Boolean {
        return when (type) {
            LotsType.MY_LOT_ACTIVE -> {
                offer.state != "active" && offer.session == null
            }

            LotsType.MY_LOT_INACTIVE -> {
                offer.state == "active"
            }

            LotsType.MY_LOT_IN_FUTURE -> {
                val currentDate: Long? = getCurrentDate().toLongOrNull()
                if (currentDate != null) {
                    val initD = (offer.session?.start?.toLongOrNull() ?: 1L) - currentDate

                    offer.state != "active" && initD < 0
                }else{
                    false
                }
            }

            else -> {
                false
            }
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
        OfferFilters.clearTypeFilter(type)
        _listingData.update {
            it.copy(
                data = it.data.copy(filters = OfferFilters.getByTypeFilter(type))
            )
        }
        refresh()
    }
}

data class OfferRepositoryEventsImpl(
    val viewModel: MyProposalsViewModel,
    val offer: OfferItem,
    val component: MyProposalsComponent
): OfferRepositoryEvents {
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
    }

    override fun goToProposalPage(type: ProposalType) {
        component.goToProposal(offer.id, type)
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        DefaultRootComponent.Companion.goToLogin(false)
    }

    override fun goToDialog(id: Long?) {
        component.goToDialog(id)
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {}
    override fun goToUserPage() {
        component.goToUser(offer.seller.id)
    }

    override fun openCabinetOffer() {
        component.goToOffer(offer)
    }

    override fun isHideCabinetOffer(): Boolean {
        return viewModel.isHideItem(offer)
    }

    override fun scrollToBids() {}
    override fun refreshPage() {}

    override fun updateItem(item: OfferItem) {
        viewModel.updateItem(offer)
    }
}
