package market.engine.fragments.root.main.favPages.favorites

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.CabinetOfferItemEvents
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.states.CabinetOfferItemState
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.ListingBaseState
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
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString

data class FavPagesContent(
    val appBarData: SimpleAppBarData = SimpleAppBarData(),
    val listingData: ListingData = ListingData(),
    val filterBarData: FilterBarUiState = FilterBarUiState(),
    val filtersCategoryState: CategoryState = CategoryState(),
    val listingBaseState: ListingBaseState = ListingBaseState(),
)

class FavViewModel(
    val favType : FavScreenType,
    val idList : Long?,
    component: FavoritesComponent
) : BaseViewModel() {

    private val offersListOperations = OffersListOperations(apiService)

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData())

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
                    val copyString = getString(strings.copyOfferId)
                    val copiedString = getString(strings.idCopied)
                    val defOption = listOf(
                        MenuItem(
                            id = "copyId",
                            title = copyString,
                            icon = drawables.copyIcon,
                            onClick = {
                                clipBoardEvent(offer.id.toString())

                                showToast(
                                    successToastItem.copy(
                                        message = copiedString
                                    )
                                )
                            }
                        ),
                        MenuItem(
                            id = "share",
                            title = getString(strings.shareOffer),
                            icon = drawables.shareIcon,
                            onClick = {
                                offer.publicUrl?.let { openShare(it) }
                            }
                        ),
                        MenuItem(
                            id = "calendar",
                            title = getString(strings.addToCalendar),
                            icon = drawables.calendarIcon,
                            onClick = {
                                offer.publicUrl?.let { openCalendarEvent(it) }
                            }
                        ),
                        MenuItem(
                            id = "create_blank_offer_list",
                            title = getString(strings.createNewOffersListLabel),
                            icon = drawables.addFolderIcon,
                            onClick = {
                                getFieldsCreateBlankOfferList { t, f ->
                                    titleDialog.value = AnnotatedString(t)
                                    fieldsDialog.value.clear()
                                    fieldsDialog.value.addAll(f)
                                    showOperationsDialog.value = "create_blank_offer_list"
                                }
                            }
                        ),
                    )

                    CabinetOfferItemState(
                        item = item,
                        isVisible = !isHideItem(item),
                        events = CabinetOfferItemEventsImpl(this, item, component),
                        defOptions = defOption,
                        selectedItem = SelectedOfferItemState(
                            isSelected = selectItems.contains(offer.id),
                            onSelectionChange = { value ->
                                if (value) {
                                    selectItems.add(offer.id)
                                } else {
                                    selectItems.remove(offer.id)
                                }
                            }
                        ),
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PagingData.empty()
        ).cachedIn(viewModelScope)

    val favDataState: StateFlow<FavPagesContent> = combine(
        _activeWindowType,
        _listingData,
    ) { activeType, listingData ->
        val ld = listingData.data
        val filterString = getString(strings.filter)
        val sortString = getString(strings.sort)
        val filters = ld.filters.filter { it.value != "" && it.interpretation?.isNotBlank() == true }

        filtersCategoryModel.updateFromSearchData(listingData.searchData)
        filtersCategoryModel.initialize(listingData.data.filters)

        FavPagesContent(
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
        initialValue = FavPagesContent()
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
                        if (isEmpty == true) {
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
                        refresh()
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

    fun clearDialogFields(){
        dialogItemId.value = 1
        fieldsDialog.value.clear()
        showOperationsDialog.value = ""
    }
}

data class CabinetOfferItemEventsImpl(
    val viewModel: FavViewModel,
    val offer: OfferItem,
    val component: FavoritesComponent
) : CabinetOfferItemEvents
{
    override fun getMenuOperations(
        tag: String?,
        callback: (List<MenuItem>) -> Unit
    ) {
        viewModel.viewModelScope.launch {
            val currency = getString(strings.currencySign)

            viewModel.getOfferOperations(
                offer.id,
                tag ?: "default",
            ) { listOperations ->
                callback(
                    if(tag == "promo") {
                        buildList {
                            addAll(listOperations.map { operation ->
                                MenuItem(
                                    id = operation.id ?: "",
                                    title = "${(operation.name ?: "")} (${operation.price * -1}$currency)",
                                    onClick = {
                                        viewModel.getOperationFields(
                                            offer.id,
                                            operation.id ?: "",
                                            "offers"
                                        ) { t, f ->
                                            viewModel.titleDialog.value = buildAnnotatedString {
                                                append(t)
                                                withStyle(
                                                    SpanStyle(
                                                        color = colors.notifyTextColor,
                                                    )
                                                ) {
                                                    append(" ${operation.price}$currency")
                                                }
                                            }
                                            viewModel.fieldsDialog.value.clear()
                                            viewModel.fieldsDialog.value.addAll(f)
                                            viewModel.showOperationsDialog.value =
                                                operation.id ?: ""
                                            viewModel.dialogItemId.value = offer.id
                                        }
                                    }
                                )
                            })
                        }
                    }else{
                        buildList {
                            addAll(listOperations.map { operation ->
                                MenuItem(
                                    id = operation.id ?: "",
                                    title = operation.name ?: "",
                                    onClick = {
                                        operation.run {
                                            when {
                                                id == "activate_offer_for_future" || id == "activate_offer" -> {
                                                    viewModel.titleDialog.value = AnnotatedString(name ?: "")
                                                    viewModel.showOperationsDialog.value = id
                                                    viewModel.dialogItemId.value = offer.id
                                                }

                                                id == "copy_offer_without_old_photo" -> {
                                                    goToCreateOffer(CreateOfferType.COPY_WITHOUT_IMAGE)
                                                }

                                                id == "edit_offer" -> {
                                                    goToCreateOffer(CreateOfferType.EDIT)
                                                }

                                                id == "copy_offer" -> {
                                                    goToCreateOffer(CreateOfferType.COPY)
                                                }

                                                id == "act_on_proposal" -> {
                                                    component.goToProposal(
                                                        ProposalType.ACT_ON_PROPOSAL,
                                                        offer.id
                                                    )
                                                }

                                                id == "make_proposal" -> {
                                                    component.goToProposal(
                                                        ProposalType.MAKE_PROPOSAL,
                                                        offer.id
                                                    )
                                                }

                                                id == "cancel_all_bids" -> {
                                                    goToDynamicSettings(
                                                        "cancel_all_bids",
                                                        offer.id
                                                    )
                                                }

                                                id == "remove_bids_of_users" -> {
                                                    goToDynamicSettings(
                                                        "remove_bids_of_users",
                                                         offer.id
                                                    )
                                                }

                                                isDataless == false -> {
                                                    viewModel.getOperationFields(
                                                        offer.id,
                                                        id ?: "",
                                                        "offers",
                                                    ) { t, f ->
                                                        viewModel.titleDialog.value = AnnotatedString(t)
                                                        viewModel.fieldsDialog.value.clear()
                                                        viewModel.fieldsDialog.value.addAll(f)
                                                        viewModel.showOperationsDialog.value = id ?: ""
                                                        viewModel.dialogItemId.value = offer.id
                                                    }
                                                }

                                                else -> {
                                                    viewModel.postOperationFields(
                                                        offer.id,
                                                        id ?: "",
                                                        "offers",
                                                        onSuccess = {
                                                            val eventParameters = mapOf(
                                                                "lot_id" to offer.id,
                                                                "lot_name" to offer.title,
                                                                "lot_city" to offer.location,
                                                                "auc_delivery" to offer.safeDeal,
                                                                "lot_category" to offer.catPath.firstOrNull(),
                                                                "seller_id" to offer.seller.id,
                                                                "lot_price_start" to offer.price,
                                                            )
                                                            viewModel.analyticsHelper.reportEvent(
                                                                "${id}_success",
                                                                eventParameters
                                                            )

                                                            viewModel.updateUserInfo()
                                                            viewModel.updateItem.value = offer.id
                                                        },
                                                        errorCallback = {}
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            })
                        }
                    }
                )
            }
        }
    }

    override fun onItemClick() {
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

    override fun goToCreateOffer(type: CreateOfferType) {
        component.goToCreateOffer(type, offer.id)
    }

    override fun goToDynamicSettings(type: String, id: Long?) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun onUpdateItem() {
        viewModel.updateItem(offer)
    }
}
