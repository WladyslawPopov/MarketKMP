package market.engine.fragments.root.main.listing

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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.states.ListingContentState
import market.engine.core.data.states.OfferItemState
import market.engine.core.data.states.SimpleAppBarData
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.network.networkObjects.Options
import market.engine.core.network.networkObjects.Payload
import market.engine.core.utils.deserializePayload
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.String

@OptIn(ExperimentalCoroutinesApi::class)
class ListingViewModel(val component: ListingComponent) : CoreViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val offerOperations : OfferOperations by lazy { getKoin().get() }

    private val _regionOptions = MutableStateFlow<List<Options>>(emptyList())
    val regionOptions : StateFlow<List<Options>> = _regionOptions.asStateFlow()

    private val _listingDataState = MutableStateFlow(ListingContentState())
    val listingDataState : StateFlow<ListingContentState> = _listingDataState.asStateFlow()

    val errorString = MutableStateFlow("")

    val listingBaseVM = ListingBaseViewModel(true, component)

    val listingCategoryModel = CategoryViewModel()

    val ld = listingBaseVM.listingData
    val activeType = listingBaseVM.activeWindowType

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        listingData
    }

    val pagingDataFlow: Flow<PagingData<OfferItemState>> = pagingParamsFlow.flatMapLatest{ listingData ->
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

                val item = offer.parseToOfferItem()

                OfferItemState(
                    item = item,
                    onItemClick = {
                        component.goToOffer(item)
                    },
                    addToFavorites = {
                        addToFavorites(item) {
                            listingBaseVM.setUpdateItem(offer.id)
                        }
                    },
                    updateItemState = {
                        updateItem(item)
                    }
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
                    val menuString = getString(strings.menuTitle)

                    val filters = ld.data.filters.filter { it.value != "" &&
                            it.interpretation?.isNotBlank() == true }

                    add(
                        NavigationItem(
                            title = filterString,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                listingBaseVM.setActiveWindowType(ActiveWindowListingType.FILTERS)
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.data.sort != null,
                            badgeCount = null,
                            onClick = {
                                listingBaseVM.setActiveWindowType(ActiveWindowListingType.SORTING)
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = menuString,
                            icon = if (listingBaseVM.listingType.value == 0) drawables.iconWidget else drawables.iconSliderHorizontal,
                            tint = colors.black,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = true,
                            onClick = {
                                val newType = if (listingBaseVM.listingType.value == 0) 1 else 0
                                settings.setSettingValue("listingType", newType)
                                listingBaseVM.setListingType(newType)
                            }
                        )
                    )
                }
            )

            getRegions()

            getOffersRecommendedInListing(ld.searchData.searchCategoryID)

            val searchData = ld.searchData
            val subs = getString(strings.subscribersLabel)
            val searchTitle = getString(strings.searchTitle)

            _listingDataState.value = ListingContentState(
                appBarData = SimpleAppBarData(
                    onBackClick = {
                        backClick()
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
                        NavigationItem(
                            title = subs,
                            icon = drawables.favoritesIcon,
                            tint = colors.inactiveBottomNavIconColor,
                            hasNews = false,
                            badgeCount = null,
                            isVisible = (searchData.searchCategoryID != 1L || searchData.userSearch || searchData.searchString != ""),
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
                        NavigationItem(
                            title = searchTitle,
                            icon = drawables.searchIcon,
                            tint = colors.black,
                            hasNews = false,
                            badgeCount = null,
                            onClick = { listingBaseVM.changeOpenSearch(true) }
                        ),
                    )
                ),
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

    fun updateItem(item : OfferItem?){
        viewModelScope.launch {
            val offer = withContext(Dispatchers.IO) {
                getOfferById(item?.id ?: 1L)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    item?.setNewParams(offer)
                }
                listingBaseVM.setUpdateItem(null)
            }
        }
    }

    suspend fun getOfferById(offerId: Long) : Offer? {
        return try {
            val response = offerOperations.getOffer(offerId)
            response.success?.let {
                return it
            }
        } catch (_: Exception) {
            null
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

    fun backClick() {
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
                component.goBack()
            }
        }
    }

    fun clearErrorSubDialog() {
        errorString.value = ""
    }
}
