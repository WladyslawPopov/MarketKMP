package market.engine.fragments.root.main.profile.myBids

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import market.engine.common.Platform
import market.engine.common.clipBoardEvent
import market.engine.common.openCalendarEvent
import market.engine.common.openShare
import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.Sort
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.CabinetOfferItemEvents
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.items.MenuItem
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.states.CabinetOfferItemState
import market.engine.core.data.states.CategoryState
import market.engine.core.data.states.FilterBarUiState
import market.engine.core.data.states.ListingBaseState
import market.engine.core.data.states.ListingOfferContentState
import market.engine.core.data.states.SelectedOfferItemState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.BaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import kotlin.collections.map

class MyBidsViewModel(
    val type: LotsType,
    val component: MyBidsComponent
) : BaseViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _listingData = MutableStateFlow(ListingData(
        data = LD().copy(
            filters = OfferFilters.getByTypeFilter(type),
            methodServer = "get_cabinet_listing_with_my_bids",
            objServer = "offers"
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
    fun clearDialogFields(){
        dialogItemId.value = 1
        fieldsDialog.value.clear()
        showOperationsDialog.value = ""
    }
}

data class CabinetOfferItemEventsImpl(
    val viewModel: MyBidsViewModel,
    val offer: OfferItem,
    val component: MyBidsComponent
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
//                                                    component.goToProposal(
//                                                        ProposalType.ACT_ON_PROPOSAL,
//                                                        offer.id
//                                                    )
                                                }

                                                id == "make_proposal" -> {
//                                                    component.goToProposal(
//                                                        ProposalType.MAKE_PROPOSAL,
//                                                        offer.id
//                                                    )
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
        component.goToOffer(offer)
    }

    override fun goToCreateOffer(type: CreateOfferType) {
    }

    override fun goToDynamicSettings(type: String, id: Long?) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun onUpdateItem() {
        viewModel.updateItem(offer)
    }

    override fun goToUser() {
        component.goToUser(offer.seller.id)
    }

    override fun goToPurchase() {
        component.goToPurchases()
    }

    override fun sendMessageToUser() {
        viewModel.postOperationAdditionalData(
            offer.id,
            "checking_conversation_existence",
            "offers",
            onSuccess = {
                val dialogId = it?.operationResult?.additionalData?.conversationId
                if (dialogId != null) {
                    component.goToDialog(dialogId)
                } else {
                    viewModel.viewModelScope.launch {
                        val userName = offer.seller.login ?: getString(strings.sellerLabel)

                        val conversationTitle = getString(strings.createConversationLabel)
                        val aboutOrder = getString(strings.aboutOfferLabel)

                        viewModel.dialogItemId.value = offer.id
                        viewModel.showOperationsDialog.value = "send_message"
                        viewModel.titleDialog.value = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = colors.grayText,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(
                                    conversationTitle
                                )
                            }

                            withStyle(
                                SpanStyle(
                                    color = colors.actionTextColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(" $userName ")
                            }

                            withStyle(
                                SpanStyle(
                                    color = colors.grayText,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(aboutOrder)
                            }

                            withStyle(
                                SpanStyle(
                                    color = colors.titleTextColor,
                                )
                            ) {
                                append(" #${offer.id}")
                            }
                        }

                        val eventParameters = mapOf(
                            "seller_id" to offer.seller.id.toString(),
                            "buyer_id" to UserData.userInfo?.id.toString(),
                            "message_type" to "lot",
                            "lot_id" to offer.id.toString()
                        )

                        viewModel.analyticsHelper.reportEvent("start_message_to_seller",
                            eventParameters
                        )
                    }
                }
            }
        )
    }

    override fun isHideItem(): Boolean {
        return viewModel.isHideItem(offer)
    }
}
