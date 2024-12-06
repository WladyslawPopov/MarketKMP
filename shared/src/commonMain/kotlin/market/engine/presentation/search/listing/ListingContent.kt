package market.engine.presentation.search.listing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.items.ListingData
import market.engine.core.navigation.children.ChildCategory
import market.engine.core.operations.operationFavorites
import market.engine.core.repositories.UserRepository
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.ListingBaseContent
import market.engine.presentation.search.listing.category.CategoryContent
import market.engine.presentation.search.listing.category.CategoryViewModel
import market.engine.presentation.search.listing.search.SearchContent
import market.engine.presentation.search.listing.search.SearchViewModel
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.FilterListingContent
import market.engine.widgets.filterContents.SortingListingContent
import market.engine.widgets.items.OfferItem
import market.engine.widgets.items.PromoOfferRowItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun ListingContent(
    stack: ChildStack<*, ChildCategory>,
    initialListingData: ListingData?=null,
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel
    val searchData = modelState.value.listingData.searchData.subscribeAsState()
    val listingData = modelState.value.listingData.data.subscribeAsState()

    val promoList = listingViewModel.responseOffersRecommendedInListing.collectAsState()
    val regions = listingViewModel.regionOptions.value

    val searchViewModel : SearchViewModel = koinViewModel()
    val categoryViewModel : CategoryViewModel = koinViewModel()

    val isOpenSearch = remember { mutableStateOf(false) }
    val isOpenCat = remember { mutableStateOf(listingViewModel.isOpenCategory.value) }

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val userRepository: UserRepository = getKoin().get()

    val scaffoldStateCategory = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (isOpenCat.value) BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )
    val scaffoldStateSearch = rememberBottomSheetScaffoldState()

    //val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val isCategorySelected = remember { mutableStateOf(listingViewModel.isCategorySelected.value) }

    LaunchedEffect(isCategorySelected.value){
        snapshotFlow {
            isCategorySelected.value
        }.collectLatest {
            listingViewModel.isCategorySelected.value = it
        }
    }

    val columns =
        remember { mutableStateOf(if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2) }

    val catDef = stringResource(strings.categoryMain)
    val searchString = remember { mutableStateOf(TextFieldValue(searchData.value.searchString ?: "")) }
    val selectedCategory = remember { mutableStateOf(searchData.value.searchCategoryName ?: catDef) }
    val selectedUserLogin = remember { mutableStateOf(searchData.value.userLogin) }
    val selectedUser = remember { mutableStateOf(searchData.value.userSearch) }
    val selectedUserFinished = remember { mutableStateOf(searchData.value.searchFinished) }

    LaunchedEffect(Unit) {
        snapshotFlow { isOpenSearch.value }.collectLatest { isOpen ->
            if (isOpen) {
                scaffoldStateSearch.bottomSheetState.expand()

                //init new search params
                if (searchViewModel.responseHistory.value.isEmpty()) {
                    searchViewModel.getHistory()
                }
                searchString.value = searchString.value.copy(text = searchData.value.searchString ?: "")
                selectedCategory.value = searchData.value.searchCategoryName ?: catDef
                selectedUserLogin.value = searchData.value.userLogin
                selectedUser.value = searchData.value.userSearch
                selectedUserFinished.value = searchData.value.searchFinished

                listingViewModel.isOpenCategory.value = true
                focusRequester.requestFocus()
            } else {
                focusManager.clearFocus()
                scaffoldStateSearch.bottomSheetState.collapse()
                listingViewModel.isOpenCategory.value = false
            }
        }
    }

    LaunchedEffect(scaffoldStateSearch.bottomSheetState.isCollapsed) {
        if (scaffoldStateSearch.bottomSheetState.isCollapsed) {
            focusManager.clearFocus()
            isOpenSearch.value = false

            if (searchData.value.isRefreshing){
                when{
                    isOpenCat.value -> categoryViewModel.getCategory(searchData.value, listingData.value)
                    else ->  listingViewModel.refresh()
                }
                searchData.value.isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { isOpenCat.value }.collectLatest { isOpen ->
            if (isOpen) {
                scaffoldStateCategory.bottomSheetState.expand()

                if(searchData.value.isRefreshing) {
                    categoryViewModel.getCategory(searchData.value, listingData.value)
                }

            } else {
                scaffoldStateCategory.bottomSheetState.collapse()
            }
        }
    }

    LaunchedEffect(scaffoldStateCategory.bottomSheetState.isCollapsed) {
        if (scaffoldStateCategory.bottomSheetState.isCollapsed) {
            isOpenCat.value = false
            if (searchData.value.isRefreshing){
                listingViewModel.refresh()
                searchData.value.isRefreshing = false
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
                focusRequester,
                searchString,
                selectedCategory,
                selectedUser,
                selectedUserLogin,
                selectedUserFinished,
                searchData.value,
                onClose = {
                    isOpenSearch.value = false
                },
                goToListing = {
                    isOpenSearch.value = false
                    isOpenCat.value = false
                    searchData.value.isRefreshing = true
                },
                goToCategory = {
                    isOpenCat.value = true
                    isOpenSearch.value = false
                }
            )
        },
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldStateCategory,
            modifier = Modifier.fillMaxSize(),
            sheetContentColor = colors.primaryColor,
            sheetBackgroundColor = colors.primaryColor,
            contentColor = colors.primaryColor,
            backgroundColor = colors.primaryColor,
            sheetPeekHeight = 0.dp,
            sheetGesturesEnabled = false,
            sheetContent = {
                CategoryContent(
                    searchData.value,
                    listingData.value,
                    onClose = {
                        isOpenCat.value = false
                        isCategorySelected.value = true
                    },
                    goListing = {
                        isOpenCat.value = false
                        isCategorySelected.value = true

                    },
                    onClearSearchClick = {
                        searchData.value.clearCategory()
                        categoryViewModel.getCategory(searchData.value, listingData.value)
                    },
                    goToSearch ={
                        isOpenSearch.value = true
                    }
                )
            },
        ){
            if (isCategorySelected.value) {
                val data = remember { listingViewModel.pagingDataFlow }.collectAsLazyPagingItems()

                //update item when we back
                LaunchedEffect(Unit) {
                    if (listingData.value.updateItem.value != null) {
                        withContext(Dispatchers.IO) {
                            val offer =
                                listingViewModel.getUpdatedOfferById(listingData.value.updateItem.value!!)
                            withContext(Dispatchers.Main) {
                                if (offer != null) {
                                    data.itemSnapshotList.items.find { it.id == offer.id }?.isWatchedByMe =
                                        offer.isWatchedByMe
                                }
                                listingData.value.updateItem.value = null
                            }
                        }
                    }
                }

                val noFound = @Composable {
                    if (listingData.value.filters.any {it.interpritation != null && it.interpritation != "" } ||
                        searchData.value.userSearch || searchData.value.searchString?.isNotEmpty() == true
                    ){
                        showNoItemLayout(
                            textButton = stringResource(strings.resetLabel)
                        ){
                            searchData.value.clear()
                            listingData.value.filters.clear()
                            listingData.value.filters.addAll(EmptyFilters.getEmpty())
                            data.refresh()
                        }
                    }else {
                        showNoItemLayout {
                            listingData.value.resetScroll()
                            listingViewModel.refresh()
                        }
                    }
                }

                ListingBaseContent(
                    columns = columns,
                    modifier = modifier,
                    listingData.value,
                    searchData.value,
                    data = data,
                    baseViewModel = listingViewModel,
                    noFound = noFound,
                    topBar = {
                        ListingAppBar(
                            isShowNav = stack.backStack.isNotEmpty(),
                            searchData.value.searchCategoryName
                                ?: stringResource(strings.categoryMain),
                            modifier,
                            onSearchClick = {
                                isOpenSearch.value = true
                            },
                            onCategoryClick = {
                                isOpenCat.value = true
                                searchData.value.isRefreshing = true
                            },
                            onBeakClick = {
                                if (searchData.value.searchIsLeaf) {
                                    searchData.value.searchCategoryID =
                                        searchData.value.searchParentID
                                    searchData.value.searchCategoryName =
                                        searchData.value.searchParentName
                                }
                                searchData.value.isRefreshing = true
                                isOpenCat.value = true
                            }
                        )
                    },
                    onRefresh = {
                        listingData.value.resetScroll()
                        columns.value =
                            if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2
                        listingViewModel.refresh()
                    },
                    filtersContent = { isRefreshingFromFilters, onClose ->
                        FilterListingContent(
                            isRefreshingFromFilters,
                            listingData.value,
                            regions,
                            onClose
                        )
                    },
                    sortingContent = { isRefreshingFromFilters, onClose ->
                        SortingListingContent(
                            isRefreshingFromFilters,
                            listingData.value,
                            onClose
                        )
                    },
                    additionalBar = { state ->
                        SwipeTabsBar(
                            listingData.value,
                            state,
                            onRefresh = {
                                data.refresh()
                            }
                        )
                    },
                    item = { offer ->
                        OfferItem(
                            offer,
                            isGrid = listingData.value.listingType == 1,
                            baseViewModel = listingViewModel,
                            onFavouriteClick = {
                                val currentOffer =
                                    data[data.itemSnapshotList.items.indexOf(
                                        it
                                    )]
                                if (currentOffer != null) {
                                    val res =
                                        operationFavorites(
                                            currentOffer,
                                            listingViewModel.viewModelScope
                                        )
                                    userRepository.updateUserInfo(listingViewModel.viewModelScope)
                                    return@OfferItem res
                                } else {
                                    return@OfferItem it.isWatchedByMe
                                }
                            }
                        ) {
                            listingData.value.updateItem.value = offer.id
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
                    },
                    isShowGrid = true,
                    onSearchClick = {
                        isOpenSearch.value = true
                    }
                )
            }
        }
    }
}



