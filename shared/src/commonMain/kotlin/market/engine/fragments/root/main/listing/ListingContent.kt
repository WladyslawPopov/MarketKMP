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
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.fragments.root.main.listing.search.SearchContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.bars.SimpleAppBar
import market.engine.widgets.filterContents.FilterListingContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.items.offer_Items.PromoOfferRowItem
import market.engine.widgets.items.offer_Items.PublicOfferItemGrid
import market.engine.widgets.items.offer_Items.PublicOfferItem
import org.jetbrains.compose.resources.stringResource


@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val listingViewModel = model.listingViewModel
    val uiState = listingViewModel.uiState.collectAsState()
    val uiDataState = listingViewModel.uiDataState.collectAsState()
    val uiFilterBarUiState = listingViewModel.uiFilterBarUiState.collectAsState()

    val globalLD = listingViewModel.listingData.collectAsState()
    val listingData = globalLD.value.data.value
    val searchData = listingViewModel.searchData.collectAsState()

    val data = uiDataState.value.pagingDataFlow?.collectAsLazyPagingItems()

    val isLoadingListing: State<Boolean> =
        rememberUpdatedState(data?.loadState?.refresh is LoadStateLoading)

    val promoList = uiDataState.value.promoOffers
    val regions = uiDataState.value.regions

    val uiSearchState = listingViewModel.uiSearchState.collectAsState()

    val searchCatBack = remember { mutableStateOf(false) }

    val scaffoldStateSearch = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (uiSearchState.value.openSearch) BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )

    val focusManager = LocalFocusManager.current

    val err = uiState.value.error

    val refresh = remember { {
        listingViewModel.resetScroll()
        listingViewModel.onError(ServerErrorException())
        listingViewModel.refresh()
        data?.refresh()
        searchData.value.isRefreshing = false
        listingViewModel.updateFilters.value = !listingViewModel.updateFilters.value
    } }

    LaunchedEffect(Unit) {
        component.refresh = refresh
    }

    BackHandler(model.backHandler) {
        when {
            listingViewModel.activeFiltersType.value == "categories" -> {
                listingViewModel.catBack.value = true
            }

            uiSearchState.value.openSearch -> {
                if (scaffoldStateSearch.bottomSheetState.isExpanded) {
                    searchCatBack.value = true
                } else {
                    listingViewModel.changeOpenSearch(false)
                }
            }

            listingViewModel.activeFiltersType.value != "" -> {
                listingViewModel.activeFiltersType.value = ""
            }

            else -> {
                component.goBack()
            }
        }
    }

    val error: (@Composable () -> Unit)? = if (err.humanMessage != "") {
        { onError(err) { refresh() } }
    } else {
        null
    }

    val noFound = @Composable {
        if (listingData.filters.any { it.interpretation != null && it.interpretation != "" } ||
            searchData.value.userSearch || searchData.value.searchString.isNotEmpty()
        ) {
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ) {
                searchData.value.clear(listingViewModel.catDef.value)
                listingData.filters = ListingFilters.getEmpty()
                refresh()
            }
        } else {
            showNoItemLayout {
                refresh()
            }
        }
    }

    LaunchedEffect(uiSearchState.value.openSearch) {
        snapshotFlow {
            uiSearchState.value.openSearch
        }.collectLatest {
            if (it) {
                scaffoldStateSearch.bottomSheetState.expand()
            } else {
                scaffoldStateSearch.bottomSheetState.collapse()
                delay(300)
                focusManager.clearFocus()
            }
        }
    }

    //update item when we back
    LaunchedEffect(listingViewModel.updateItem.value) {
        if (listingViewModel.updateItem.value != null) {

            val offer = withContext(Dispatchers.IO) {
                listingViewModel.getOfferById(listingViewModel.updateItem.value ?: 1L)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    val item =
                        data?.itemSnapshotList?.items?.find { it.id == offer.id }
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
                uiSearchState.value,
                model.searchEvents,
                component.searchPages
            )
        },
    ) {
        BaseContent(
            topBar = {
                if (uiState.value.appBarData != null) {
                    SimpleAppBar(
                        data = uiState.value.appBarData!!
                    )
                }

                CreateSubscribeDialog(
                    listingViewModel.errorString.value != "",
                    listingViewModel.errorString.value,
                    onDismiss = {
                        listingViewModel.errorString.value = ""
                    },
                    goToSubscribe = {
                        component.goToSubscribe()
                        listingViewModel.errorString.value = ""
                    }
                )
            },
            onRefresh = remember {
                {
                    refresh()
                }
            },
            error = error,
            noFound = null,
            isLoading = isLoadingListing.value,
            toastItem = listingViewModel.toastItem,
            modifier = modifier.fillMaxSize()
        ) {
            if (data != null) {
                ListingBaseContent(
                    columns = if (listingData.listingType == 0) 1 else if (isBigScreen.value) 3 else 2,
                    listingData,
                    searchData.value,
                    data = data,
                    baseViewModel = listingViewModel,
                    noFound = noFound,
                    onRefresh = remember {
                        {
                            refresh()
                        }
                    },
                    filtersContent = { isRefreshingFromFilters, onClose ->
                        when (listingViewModel.activeFiltersType.value) {
                            "filters" -> {
                                FilterListingContent(
                                    isRefreshingFromFilters,
                                    listingData = listingData.filters,
                                    regionsOptions = regions,
                                    onClosed = {
                                        listingViewModel.activeFiltersType.value = ""
                                    },
                                    onClear = {
                                        listingData.filters.clear()
                                        listingData.filters.addAll(ListingFilters.getEmpty())
                                        isRefreshingFromFilters.value = true
                                    }
                                )
                            }

                            "sorting" -> {
                                SortingOffersContent(
                                    isRefreshingFromFilters,
                                    listingData,
                                    isCabinet = false,
                                    onClose
                                )
                            }

                            "categories" -> {
                                CategoryContent(
                                    uiState.value.categoryViewModel,
                                    onComplete = {
                                        listingViewModel.activeFiltersType.value = ""
                                        uiState.value.categoryViewModel.run {
                                            if (categoryId.value != searchData.value.searchCategoryID) {
                                                val searchData = searchData.value.copy(
                                                    searchCategoryID = categoryId.value,
                                                    searchCategoryName = categoryName.value,
                                                    searchParentID = parentId.value,
                                                    searchIsLeaf = isLeaf.value
                                                )
                                                listingViewModel.listingData.value.searchData.value = searchData
                                                refresh()
                                            }
                                        }
                                    },
                                    onClose = onClose
                                )
                            }
                        }
                    },
                    additionalBar = { state ->
                        SwipeTabsBar(
                            isVisibility = listingViewModel.activeFiltersType.value == "categories",
                            listingData,
                            state,
                            onRefresh = {
                                refresh()
                            }
                        )

                        FiltersBar(
                            uiFilterBarUiState.value
                        )
                    },
                    item = { offer ->
                        when (listingData.listingType) {
                            0 -> {
                                PublicOfferItem(
                                    offer,
                                    updateTrigger = listingViewModel.updateItemTrigger.value,
                                    onItemClick = {
                                        component.goToOffer(offer)
                                    },
                                    addToFavorites = {
                                        listingViewModel.addToFavorites(offer) {
                                            offer.isWatchedByMe = it
                                            listingViewModel.updateItemTrigger.value++
                                        }
                                    },
                                )
                            }

                            1 -> {
                                PublicOfferItemGrid(
                                    offer,
                                    updateTrigger = listingViewModel.updateItemTrigger.value,
                                    onItemClick = {
                                        component.goToOffer(offer)
                                    },
                                    addToFavorites = {
                                        listingViewModel.addToFavorites(offer) {
                                            offer.isWatchedByMe = it
                                            listingViewModel.updateItemTrigger.value++
                                        }
                                    },
                                )
                            }
                        }
                    },
                    promoList = promoList,
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
}
