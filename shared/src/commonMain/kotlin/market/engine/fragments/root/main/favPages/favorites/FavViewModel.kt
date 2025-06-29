package market.engine.fragments.root.main.favPages.favorites

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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.Platform
import market.engine.core.data.baseFilters.Filter
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
import market.engine.core.data.states.SelectedOfferItemState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.OfferRepository
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString


class FavViewModel(
    val favType : FavScreenType,
    val idList : Long?,
    component: FavoritesComponent
) : BaseViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData())

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
                        selectedItem = SelectedOfferItemState(
                            selected = selectItems,
                            onSelectionChange = { id ->
                                if (!selectItems.contains(id)) {
                                    selectItems.add(id)
                                } else {
                                    selectItems.remove(id)
                                }
                            }
                        ),
                        offerRepository = OfferRepository(
                            item,
                            OfferRepositoryEventsImpl(this, item, component),
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

    val favDataState: StateFlow<ListingOfferContentState> = combine(
        _activeWindowType,
        _listingData,
    )
    { activeType, listingData ->
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

    init {
        when(favType){
            FavScreenType.FAVORITES -> {
                _listingData.update {
                    it.copy(
                        data = it.data.copy(
                            filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES),
                            methodServer = "get_cabinet_listing_watched_by_me",
                            objServer = "offers"
                        )
                    )
                }
            }
            FavScreenType.NOTES ->{
                _listingData.update {
                    it.copy(
                        data = it.data.copy(
                            methodServer = "get_cabinet_listing_my_notes",
                            objServer = "offers"
                        )
                    )
                }
            }
            FavScreenType.FAV_LIST ->{
                _listingData.update {
                    it.copy(
                        data = it.data.copy(
                            filters = buildList {
                                add(
                                    Filter("list_id", "$idList", "", null)
                                )
                                addAll(it.data.filters)
                            },
                            methodServer = "get_cabinet_listing_in_list",
                            objServer = "offers"
                        )
                    )
                }
            }
            else -> {}
        }
    }

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

    fun getList(id: Long, onSuccess: (FavoriteListItem) -> Unit) {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { offersListOperations.getOffersListItem(id) }

            withContext(Dispatchers.Main) {
                val res = data.success
                if (res != null) {
                    onSuccess(res)
                }
            }
        }
    }

    fun updateItem(oldItem : OfferItem){
        viewModelScope.launch {
            idList?.let { id ->
                getList(id) { item ->
                    if (!item.offers.contains(id)) {
                        oldItem.session = null
                    }
                }
            }

            val offer = withContext(Dispatchers.IO) {
                getOfferById(oldItem.id)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    oldItem.setNewParams(offer)
                }

                if (favType != FavScreenType.FAV_LIST) {
                    val isHide = isHideItem(oldItem)
                    if (isHide) {
                        refresh()
                    }
                } else {
                    getList(idList ?: 1L) {
                        val isEmpty = oldItem.let { item ->
                            it.offers.contains(item.id)
                        }
                        if (isEmpty) {
                            refresh()
                        }
                    }
                }
                updateItem.value = null
            }
        }
    }

    fun deleteSelectsItems(ids: List<Long>) {
        viewModelScope.launch {
            ids.forEach { item ->
                val body = HashMap<String, JsonElement>()

                val type = when (favType) {
                    FavScreenType.NOTES -> {
                        "delete_note"
                    }

                    FavScreenType.FAVORITES -> {
                        "unwatch"
                    }

                    FavScreenType.FAV_LIST -> {
                        body["offers_list_id"] = JsonPrimitive(idList)
                        "remove_from_list"
                    }

                    else -> {
                        ""
                    }
                }

                postOperationFields(
                    item,
                    type,
                    "offers",
                    body,
                    onSuccess = {
                        selectItems.clear()
                        updatePage()
                    },
                    errorCallback = {

                    }
                )
            }
        }
    }

    fun isHideItem(offer: OfferItem): Boolean {
        return when (favType) {
            FavScreenType.NOTES -> {
                offer.note == null && offer.note == ""
            }

            FavScreenType.FAVORITES -> {
                !offer.isWatchedByMe
            }

            else -> {
                offer.session == null
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
        OfferFilters.clearTypeFilter(LotsType.FAVORITES)
        _listingData.update {
            it.copy(
                data = it.data.copy(filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES))
            )
        }
        refresh()
    }
}

data class OfferRepositoryEventsImpl(
    val viewModel: FavViewModel,
    val offer: OfferItem,
    val component: FavoritesComponent
): OfferRepositoryEvents {
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
        component.goToCreateOffer(type, id)
    }

    override fun goToProposalPage(type: ProposalType) {
        component.goToProposal(type, offer.id)
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        goToLogin(false)
    }

    override fun goToDialog(id: Long?) {}

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {}
    override fun goToUserPage() {}

    override fun openCabinetOffer() {
        if (viewModel.selectItems.isNotEmpty()) {
            if (viewModel.selectItems.contains(offer.id)) {
                viewModel.selectItems.remove(offer.id)
            } else {
                viewModel.selectItems.add(offer.id)
            }
        } else {
            component.goToOffer(offer)
        }
    }

    override fun isHideCabinetOffer() : Boolean {
        return viewModel.isHideItem(offer)
    }

    override fun scrollToBids() {}

    override fun update() {
        viewModel.updateItem(offer)
    }
}
