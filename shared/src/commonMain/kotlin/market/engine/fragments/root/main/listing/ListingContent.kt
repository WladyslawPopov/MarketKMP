package market.engine.fragments.root.main.listing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.listing.search.SearchContent
import market.engine.widgets.filterContents.CategoryContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.FilterListingContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.items.PromoOfferRowItem
import org.jetbrains.compose.resources.stringResource


@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val listingViewModel = model.listingViewModel

    val searchData = listingViewModel.listingData.value.searchData
    val listingData = listingViewModel.listingData.value.data

    val data = remember { component.model.value.pagingDataFlow }.collectAsLazyPagingItems()

    val title = remember { mutableStateOf(searchData.value.searchCategoryName) }

    val isLoadingListing : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val promoList = listingViewModel.responseOffersRecommendedInListing.collectAsState()
    val regions = listingViewModel.regionOptions.value

    val openSearchCategoryBottomSheet = remember { mutableStateOf(false) }

    val scaffoldStateSearch = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (listingViewModel.openSearch.value) BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )

    val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()
    val focusManager = LocalFocusManager.current

    val columns =
        remember { mutableStateOf(if (listingData.value.listingType == 0) 1 else if (isBigScreen) 3 else 2) }

    val catDef = stringResource(strings.categoryMain)

    val updateFilters = remember { mutableStateOf(0) }

    val err = listingViewModel.errorMessage.collectAsState()

    val searchCatBack = remember { mutableStateOf(false) }

    val refresh = {
        listingViewModel.resetScroll()

        title.value = searchData.value.searchCategoryName

        columns.value =
            if (listingData.value.listingType == 0) 1 else if (isBigScreen) 3 else 2

        listingViewModel.onError(ServerErrorException())
        listingViewModel.refresh()
        data.refresh()
        searchData.value.isRefreshing = false
        updateFilters.value++
    }

    BackHandler(model.backHandler){
        when{
            listingViewModel.activeFiltersType.value == "categories" -> {
                listingViewModel.catBack.value = true
            }
            listingViewModel.openSearch.value -> {
                if (openSearchCategoryBottomSheet.value){
                    searchCatBack.value = true
                }else{
                    listingViewModel.openSearch.value = false
                }
            }

            listingViewModel.activeFiltersType.value != "" ->{
                listingViewModel.activeFiltersType.value = ""
            }
            else -> {
                component.goBack()
            }
        }
    }

    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err) { refresh() } }
    }else{
        null
    }

    val noFound = @Composable {
        if (listingData.value.filters.any {it.interpretation != null && it.interpretation != "" } ||
            searchData.value.userSearch || searchData.value.searchString.isNotEmpty()
        ){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                searchData.value.clear(catDef)
                listingData.value.filters = ListingFilters.getEmpty()
                refresh()
            }
        }else {
            showNoItemLayout {
                refresh()
            }
        }
    }

    LaunchedEffect(listingViewModel.openSearch.value) {
        snapshotFlow { listingViewModel.openSearch.value }.collectLatest { isOpen ->
            if (isOpen) {
                val eventParameters = mapOf(
                    "search_string" to searchData.value.searchString,
                    "category_id" to searchData.value.searchCategoryID,
                    "category_name" to searchData.value.searchCategoryName,
                    "user_login" to searchData.value.userLogin,
                    "user_search" to searchData.value.userSearch,
                    "user_finished" to searchData.value.searchFinished
                )
                analyticsHelper.reportEvent("open_search_listing", eventParameters)

                scaffoldStateSearch.bottomSheetState.expand()
            } else {
                listingViewModel.openSearch.value = false
                scaffoldStateSearch.bottomSheetState.collapse()
                delay(300)
                focusManager.clearFocus()
            }
        }
    }

    LaunchedEffect(listingViewModel.activeFiltersType.value){
        if (listingViewModel.activeFiltersType.value == "categories"){
            val eventParameters = mapOf(
                "category_name" to searchData.value.searchCategoryName,
                "category_id" to searchData.value.searchCategoryID,
            )
            analyticsHelper.reportEvent("open_catalog_listing", eventParameters)
        }
    }
    //update item when we back
    LaunchedEffect(listingViewModel.updateItem.value){
        if (listingViewModel.updateItem.value != null) {
            val offer = withContext(Dispatchers.IO) {
                listingViewModel.getOfferById(listingViewModel.updateItem.value ?: 1L)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    val item =
                        data.itemSnapshotList.items.find { it.id == offer.id }
                    item?.isWatchedByMe = offer.isWatchedByMe
                }
                listingViewModel.updateItemTrigger.value++
                listingViewModel.updateItem.value = null
            }
        }
    }


    BottomSheetScaffold(
        scaffoldState = scaffoldStateSearch,
        modifier = Modifier.fillMaxSize(),
        sheetContentColor = colors.primaryColor,
        sheetBackgroundColor = colors.primaryColor,
        contentColor = colors.primaryColor,
        backgroundColor = colors.primaryColor,
        sheetPeekHeight = 0.dp,
        sheetGesturesEnabled = false,
        sheetContent = {
            SearchContent(
                listingViewModel.openSearch,
                openSearchCategoryBottomSheet,
                searchData.value,
                listingViewModel,
                searchCatBack,
                closeSearch = {
                    listingViewModel.openSearch.value = false
                },
                goToListing = {
                    listingViewModel.openSearch.value = false
                    listingViewModel.activeFiltersType.value = ""

                    if (searchData.value.isRefreshing) {
                        refresh()
                    }
                },
            )
        },
    ) {
        BaseContent(
            topBar = {
                val errorString = remember { mutableStateOf("") }

                ListingAppBar(
                    title = if(title.value != "") title.value else catDef,
                    onBackClick = {
                        if (listingViewModel.activeFiltersType.value.isEmpty()) {
                                component.goBack()
                                listingViewModel.openSearch.value = true
                        }else{
                            listingViewModel.activeFiltersType.value = ""
                        }
                    },
                    closeCategory = {
                        if (listingViewModel.activeFiltersType.value == "categories"){
                            listingViewModel.activeFiltersType.value = ""
                        }else {
                            listingViewModel.activeFiltersType.value = "categories"
                            listingViewModel.openCategory.value = true
                        }
                    },
                    isShowSubscribes = (searchData.value.searchCategoryID != 1L || searchData.value.userSearch || searchData.value.searchString != ""),
                    onSearchClick = {
                        listingViewModel.openSearch.value = true
                        listingViewModel.openSearch.value = true
                    },
                    onSubscribesClick = {
                        if(UserData.token != "") {
                            listingViewModel.addNewSubscribe(
                                listingData.value,
                                searchData.value,
                                onSuccess = {},
                                onError = { es ->
                                    errorString.value = es
                                }
                            )
                        }else{
                            goToLogin(false)
                        }
                    }
                )

                CreateSubscribeDialog(
                    errorString.value != "",
                    errorString.value,
                    onDismiss = {
                        errorString.value = ""
                    },
                    goToSubscribe = {
                        component.goToSubscribe()
                        errorString.value = ""
                    }
                )
            },
            onRefresh = {
                refresh()
            },
            error = error,
            noFound = null,
            isLoading = isLoadingListing.value,
            toastItem = listingViewModel.toastItem,
            modifier = modifier.fillMaxSize()
        ) {
            ListingBaseContent(
                columns = columns,
                listingData.value,
                searchData.value,
                data = data,
                baseViewModel = listingViewModel,
                noFound = noFound,
                onRefresh = {
                    refresh()
                },
                filtersContent = { isRefreshingFromFilters, onClose ->
                    when (listingViewModel.activeFiltersType.value){
                        "filters" -> {
                            FilterListingContent(
                                isRefreshingFromFilters,
                                listingData = listingData.value.filters,
                                regionsOptions = regions,
                                onClosed = onClose,
                            )
                        }
                        "sorting" -> {
                            SortingOffersContent(
                                isRefreshingFromFilters,
                                listingData.value,
                                onClose
                            )
                        }
                        "categories" ->{
                            CategoryContent(
                                isOpen = listingViewModel.openCategory,
                                searchData = searchData.value,
                                filters = listingData.value.filters,
                                isRefresh = isRefreshingFromFilters,
                                baseViewModel = listingViewModel,
                                onBackClicked = listingViewModel.catBack
                            ){
                                listingViewModel.activeFiltersType.value = ""
                            }
                        }
                    }
                },
                additionalBar = { state ->
                    SwipeTabsBar(
                        isVisibility = listingViewModel.activeFiltersType.value == "categories",
                        listingData.value,
                        state,
                        onRefresh = {
                            refresh()
                        }
                    )

                    FiltersBar(
                        searchData.value,
                        listingData.value,
                        updateFilters.value,
                        isShowGrid = true,
                        onChangeTypeList = {
                            listingViewModel.settings.setSettingValue("listingType", it)
                            listingData.value.listingType = it
                            refresh()
                        },
                        onFilterClick = {
                            listingViewModel.activeFiltersType.value = "filters"
                        },
                        onSortClick = {
                            listingViewModel.activeFiltersType.value = "sorting"
                        },
                        onSearchClick = {
                            listingViewModel.openSearch.value = true
                        },
                        onRefresh = {
                            updateFilters.value++
                            refresh()
                        }
                    )
                },
                item = { offer ->
                    OfferItem(
                        offer,
                        isGrid = listingData.value.listingType == 1,
                        baseViewModel = listingViewModel,
                        isShowFavorites = true,
                        updateTrigger = listingViewModel.updateItemTrigger.value,
                    ) {
                        component.goToOffer(offer)
                    }
                },
                promoList = promoList.value,
                promoContent = { offer ->
                    PromoOfferRowItem(
                        offer
                    ) {
                        component.goToOffer(offer, true)
                    }
                }
            )
        }
    }
}
