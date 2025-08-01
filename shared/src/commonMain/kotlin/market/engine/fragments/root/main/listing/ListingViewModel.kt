package market.engine.fragments.root.main.listing

import androidx.lifecycle.SavedStateHandle
import androidx.paging.cachedIn
import androidx.paging.map
import app.cash.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.Platform
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.successToastItem
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.NavigationItemUI
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.repositories.OfferBaseViewModel
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import kotlin.String

@OptIn(ExperimentalCoroutinesApi::class)
class ListingViewModel(val component: ListingComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())
    val regionOptions : StateFlow<List<Options>> = _regionOptions.asStateFlow()

    private val _listingDataState = MutableStateFlow(SimpleAppBarData())
    val listingDataState : StateFlow<SimpleAppBarData> = _listingDataState.asStateFlow()

    val errorString = MutableStateFlow("")

    val listingBaseVM = ListingBaseViewModel(true, component, savedStateHandle = savedStateHandle)

    val listingCategoryModel = CategoryViewModel(savedStateHandle = savedStateHandle)

    val ld = listingBaseVM.listingData
    val activeType = listingBaseVM.activeWindowType

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    val pagingDataFlow: Flow<PagingData<OfferBaseViewModel>> = pagingParamsFlow.flatMapLatest{ listingData ->
        pagingRepository.getListing(
            listingData,
            apiService,
            Offer.serializer(),
            onTotalCountReceived = {
                listingBaseVM.setTotalCount(it)
            }
        ).map { pagingData ->
            pagingData.map { offer ->
                if (listingData.searchData.userID != 1L &&
                    listingData.searchData.userLogin.isNullOrEmpty()
                ) {
                    listingData.searchData.userLogin = offer.sellerData?.login
                }

                if (offer.promoOptions != null && offer.sellerData?.id != UserData.login) {
                    val isBackLight =
                        offer.promoOptions.find { it.id == "backlignt_in_listing" }
                    if (isBackLight != null) {
                        val eventParameters = mapOf(
                            "catalog_category" to offer.catpath.lastOrNull(),
                            "lot_category" to if (offer.catpath.isEmpty()) 1 else offer.catpath.firstOrNull(),
                            "offer_id" to offer.id,
                        )

                        analyticsHelper.reportEvent("show_top_lots", eventParameters)
                    }
                }

                OfferBaseViewModel(
                    offer,
                    listingData,
                    events = OfferRepositoryEventsImpl(this, component),
                    savedStateHandle
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PagingData.empty()
    ).cachedIn(viewModelScope)

    fun init(ld: ListingData) {
        viewModelScope.launch {
            ld.data.methodServer = "get_public_listing"
            ld.data.objServer = "offers"

            if (ld.data.filters.isEmpty()) {
                ld.data.filters = ListingFilters.getEmpty()
            }

            listingBaseVM.setListingType(settings.getSettingValue("listingType", 0) ?: 0)

            listingBaseVM.setListingData(ld)

            listingBaseVM.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val menuString = getString(strings.chooseAction)

                    val filters = ld.data.filters.filter { it.value != "" &&
                            it.interpretation?.isNotBlank() == true }

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
                            hasNews = ld.data.sort != null,
                            badgeCount = null,
                        )
                    )
                    add(
                        NavigationItem(
                            title = menuString,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = true,
                        )
                    )
                }
            )

            getRegions()

            getOffersRecommendedInListing(ld.searchData.searchCategoryID)

            val searchData = ld.searchData
            val subs = getString(strings.subscribersLabel)
            val searchTitle = getString(strings.searchTitle)

            _listingDataState.value = SimpleAppBarData(
                onBackClick = {
                    component.goBack()
                },
                listItems = listOf(
                    NavigationItemUI(
                        NavigationItem(
                            title = "",
                            hasNews = false,
                            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                            badgeCount = null,
                        ),
                        icon = drawables.recycleIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = { refresh() }
                    ),
                    NavigationItemUI(
                        NavigationItem(
                            title = subs,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = (searchData.searchCategoryID != 1L || searchData.userSearch || searchData.searchString != ""),
                        ),
                        icon = drawables.favoritesIcon,
                        tint = colors.inactiveBottomNavIconColor,
                        onClick = {
                            if (UserData.token != "") {
                                addNewSubscribe(
                                    ld.data,
                                    searchData,
                                    onSuccess = {},
                                    errorCallback = { es ->
                                        errorString.value = es
                                    }
                                )
                            } else {
                                goToLogin(false)
                            }
                        }
                    ),
                    NavigationItemUI(
                        NavigationItem(
                            title = searchTitle,
                            hasNews = false,
                            badgeCount = null,
                        ),
                        icon = drawables.searchIcon,
                        tint = colors.black,
                        onClick = { listingBaseVM.changeOpenSearch(true) }
                    )
                )
            )
        }
    }

    fun changeOpenCategory(complete: Boolean = false) {
        listingCategoryModel.run {
            if (activeType.value == ActiveWindowListingType.LISTING) {
                listingBaseVM.setActiveWindowType(ActiveWindowListingType.CATEGORY)
                val eventParameters = mapOf(
                    "category_name" to ld.value.searchData.searchCategoryName,
                    "category_id" to ld.value.searchData.searchCategoryID,
                )
                analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
            } else {
                if (complete) {
                    listingCategoryModel.run {
                        if (ld.value.searchData.searchCategoryID != searchData.value.searchCategoryID) {
                            listingBaseVM.setListingData(
                                ld.value.copy(
                                    searchData = searchData.value
                                )
                            )

                            refresh()
                        }
                    }
                }

                listingBaseVM.setActiveWindowType(ActiveWindowListingType.LISTING)
            }
        }
    }

    private fun getOffersRecommendedInListing(categoryID:Long) {
        viewModelScope.launch{
            try {
                val response = withContext(Dispatchers.IO){
                    apiService.getOffersRecommendedInListing(categoryID)
                }

                withContext(Dispatchers.Main) {
                    try {
                        val serializer = Payload.serializer(Offer.serializer())
                        val payload : Payload<Offer> = deserializePayload(response.payload, serializer)
                        listingBaseVM.setPromoList(payload.objects.map { it.parseToOfferItem() }.toList())
                    }catch (_ : Exception){
                        throw ServerErrorException(response.errorCode.toString(), response.humanMessage.toString())
                    }
                }
            }  catch (exception: ServerErrorException) {
                onError(exception)
            } catch (exception: Exception) {
                onError (
                    ServerErrorException(
                        errorCode = exception.message.toString(),
                        humanMessage = exception.message.toString()
                    )
                )
            }
        }
    }

    private fun getRegions(){
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                categoryOperations.getRegions()
            }
            withContext(Dispatchers.Main) {
                res?.firstOrNull()?.options?.sortedBy { it.weight }?.let { _regionOptions.value = it }
            }
        }
    }

    fun addNewSubscribe(
        listingData : LD,
        searchData : SD,
        onSuccess: () -> Unit,
        errorCallback: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = operationsMethods.getOperationFields(
                UserData.login,
                "create_subscription",
                "users"
            )

            val eventParameters : ArrayList<Pair<String, Any?>> = arrayListOf(
                "buyer_id" to UserData.login.toString(),
            )
            analyticsHelper.reportEvent("click_subscribe_query", eventParameters.toMap())

            val body = HashMap<String, JsonElement>()
            response.success?.fields?.forEach { field ->
                when(field.key) {
                    "category_id" -> {
                        if (searchData.searchCategoryID != 1L) {
                            body["category_id"] = JsonPrimitive(searchData.searchCategoryID)
                            eventParameters.add("category_id" to searchData.searchCategoryID.toString())
                        }
                    }
                    "offer_scope" -> {
                        body["offer_scope"] = JsonPrimitive(1)
                    }
                    "search_query" -> {
                        if(searchData.searchString != "") {
                            body["search_query"] = JsonPrimitive(searchData.searchString)
                            eventParameters.add("search_query" to searchData.searchString)
                        }
                    }
                    "seller" -> {
                        if(searchData.userSearch) {
                            body["seller"] = JsonPrimitive(searchData.userLogin)
                            eventParameters.add("seller" to searchData.userLogin.toString())
                        }
                    }
                    "saletype" -> {
                        when (listingData.filters.find { it.key == "sale_type" }?.value) {
                            "buynow" -> {
                                body["saletype"] = JsonPrimitive(0)
                            }
                            "auction" -> {
                                body["saletype"] = JsonPrimitive(1)
                            }
                        }
                        eventParameters.add("saletype" to listingData.filters.find { it.key == "sale_type" }?.value.toString())
                    }
                    "region" -> {
                        listingData.filters.find { it.key == "region" }?.value?.let {
                            if (it != "") {
                                body["region"] = JsonPrimitive(it)
                                eventParameters.add("region" to it)
                            }
                        }
                    }
                    "price_from" -> {
                        listingData.filters.find { it.key == "current_price" && it.operation == "gte" }?.value?.let {
                            if (it != "") {
                                body["price_from"] = JsonPrimitive(it)
                                eventParameters.add("price_from" to it)
                            }
                        }
                    }
                    "price_to" -> {
                        listingData.filters.find { it.key == "current_price" && it.operation == "lte" }?.value?.let {
                            if (it != "") {
                                body["price_to"] = JsonPrimitive(it)
                                eventParameters.add("price_to" to it)
                            }
                        }
                    }
                    else ->{
                        if (field.data != null){
                            body[field.key ?: ""] = field.data!!
                        }
                    }
                }
            }

            val res = operationsMethods.postOperationFields(
                UserData.login,
                "create_subscription",
                "users",
                body
            )

            val buf = res.success
            val err = res.error

            withContext(Dispatchers.Main) {
                if (buf != null) {
                    showToast(
                        successToastItem.copy(
                            message = res.success?.operationResult?.message ?: getString(strings.operationSuccess)
                        )
                    )
                    delay(1000)
                    onSuccess()
                }else {
                    errorCallback(err?.humanMessage ?: "")
                }
            }
        }
    }

    fun backClick(onBack: () -> Unit) {
        when {
            activeType.value == ActiveWindowListingType.CATEGORY_FILTERS &&
                    listingBaseVM.searchCategoryModel.searchData.value.searchCategoryID!= 1L -> {
                listingBaseVM.searchCategoryModel.navigateBack()
            }

            activeType.value == ActiveWindowListingType.CATEGORY &&
                    listingBaseVM.searchCategoryModel.searchData.value.searchCategoryID!= 1L -> {
                listingCategoryModel.navigateBack()
            }

            activeType.value != ActiveWindowListingType.LISTING -> {
                listingBaseVM.setActiveWindowType(ActiveWindowListingType.LISTING)
            }

            else -> {
                onBack()
            }
        }
    }

    fun clearErrorSubDialog() {
        errorString.value = ""
    }
}

data class OfferRepositoryEventsImpl(
    val viewModel: ListingViewModel,
    val component: ListingComponent
): OfferRepositoryEvents
{
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {}

    override fun goToProposalPage(
        offerId: Long,
        type: ProposalType
    ) {}

    override fun goToDynamicSettings(type: String, id: Long) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        goToLogin(false)
    }

    override fun goToDialog(id: Long?) {
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {
    }

    override fun goToUserPage(sellerId: Long) {
    }

    override fun openCabinetOffer(offer: OfferItem) {
        component.goToOffer(offer)
    }

    override fun scrollToBids() {}
    override fun refreshPage() {}
    override fun updateBidsInfo(item: OfferItem) {}
}

