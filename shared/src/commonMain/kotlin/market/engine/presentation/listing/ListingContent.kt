package market.engine.presentation.listing

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.operations.operationFavorites
import market.engine.core.repositories.UserRepository
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.presentation.base.ListingBaseContent
import market.engine.widgets.filterContents.CategoryContent
import market.engine.presentation.listing.search.SearchContent
import market.engine.presentation.listing.search.SearchViewModel
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.exceptions.onError
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
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val listingViewModel = modelState.value.listingViewModel
    val searchData = modelState.value.listingData.searchData.subscribeAsState()
    val listingData = modelState.value.listingData.data.subscribeAsState()

    val data = remember { component.model.value.pagingDataFlow }.collectAsLazyPagingItems()

    val isLoadingListing : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val promoList = listingViewModel.responseOffersRecommendedInListing.collectAsState()
    val regions = listingViewModel.regionOptions.value

    val isErrorCategory = listingViewModel.errorMessage.value

    val searchViewModel : SearchViewModel = koinViewModel()

    val windowClass = getWindowSizeClass()
    val isBigScreen = windowClass == WindowSizeClass.Big
    val userRepository: UserRepository = getKoin().get()

    val scaffoldStateSearch = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = if (listingViewModel.isOpenSearch.value) BottomSheetValue.Expanded else BottomSheetValue.Collapsed
        )
    )

    //val analyticsHelper = AnalyticsFactory.createAnalyticsHelper()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val columns =
        remember { mutableStateOf(if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2) }

    val catDef = stringResource(strings.categoryMain)
    val searchString = remember { mutableStateOf(TextFieldValue(searchData.value.searchString ?: "")) }
    val selectedCategory = remember { mutableStateOf(searchData.value.searchCategoryName ?: catDef) }
    val selectedUserLogin = remember { mutableStateOf(searchData.value.userLogin) }
    val selectedUser = remember { mutableStateOf(searchData.value.userSearch) }
    val selectedUserFinished = remember { mutableStateOf(searchData.value.searchFinished) }

    val title = remember { mutableStateOf(searchData.value.searchCategoryName ?: catDef) }

    val refresh = {
        listingData.value.resetScroll()

        title.value = searchData.value.searchCategoryName ?: catDef

        columns.value =
            if (listingData.value.listingType == 0) 1 else if (isBigScreen) 4 else 2

        listingViewModel.getCategories(searchData.value, listingData.value)

        listingViewModel.refresh()
    }

    val error : (@Composable () -> Unit)? = if (isErrorCategory.humanMessage != "") {
        { onError(isErrorCategory) { refresh() } }
    }else{
        null
    }

    val backCountClick = remember { mutableStateOf(0) }


    LaunchedEffect(listingViewModel.isOpenSearch.value) {
        snapshotFlow { listingViewModel.isOpenSearch.value }.collectLatest { isOpen ->
            if (isOpen) {
                scaffoldStateSearch.bottomSheetState.expand()

                //init new search params
                if (searchViewModel.responseHistory.value.isEmpty()) {
                    searchViewModel.getHistory()
                }
                searchString.value =
                    searchString.value.copy(text = searchData.value.searchString ?: "")
                selectedCategory.value = searchData.value.searchCategoryName ?: catDef
                selectedUserLogin.value = searchData.value.userLogin
                selectedUser.value = searchData.value.userSearch
                selectedUserFinished.value = searchData.value.searchFinished


                focusRequester.requestFocus()
            } else {
                focusManager.clearFocus()
                scaffoldStateSearch.bottomSheetState.collapse()
            }
        }
    }

    LaunchedEffect(scaffoldStateSearch.bottomSheetState.isCollapsed) {
        if (scaffoldStateSearch.bottomSheetState.isCollapsed) {
            focusManager.clearFocus()
            listingViewModel.isOpenSearch.value = false

            if (searchData.value.isRefreshing){
                when{
                    listingViewModel.activeFiltersType.value == "categories" ->
                    listingViewModel.getCategories(searchData.value, listingData.value)
                    else -> refresh()
                }
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
                searchData.value,
                focusRequester,
                searchString,
                selectedCategory,
                selectedUser,
                selectedUserLogin,
                selectedUserFinished,
                onClose = {
                    listingViewModel.isOpenSearch.value = false
                },
                goToListing = {
                    listingViewModel.isOpenSearch.value = false
                    listingViewModel.activeFiltersType.value = ""
                    searchData.value.isRefreshing = true
                },
            )
        },
    ) {
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
                    refresh()
                }
            }else {
                showNoItemLayout {
                    refresh()
                }
            }
        }
        //update item when we back
        LaunchedEffect(Unit){

            if (listingViewModel.updateItem.value != null) {
                withContext(Dispatchers.Default) {
                    val updateItem = listingViewModel.updateItem.value
                    val offer =
                        listingViewModel.getUpdatedOfferById(updateItem ?: 1L)
                    withContext(Dispatchers.Main) {
                        if (offer != null) {
                            val item =
                                data.itemSnapshotList.items.find { it.id == offer.id }
                            item?.isWatchedByMe = offer.isWatchedByMe
                        }

                        listingViewModel.updateItem.value = null
                    }
                }
            }
        }

        BaseContent(
            topBar = {
                ListingAppBar(
                    title = title.value,
                    modifier,
                    isShowNav = backCountClick.value == 0,
                    isOpenCategory = listingViewModel.activeFiltersType.value != "categories",
                    onBackClick = {
                        if(searchData.value.searchCategoryID == 1L){
                            backCountClick.value = 1
                        }
                        component.goBack()
                        listingViewModel.activeFiltersType.value = "categories"
                    },
                    closeCategory = {
                        if (listingViewModel.activeFiltersType.value == "categories"){
                            listingViewModel.activeFiltersType.value = ""
                        }else {
                            listingViewModel.activeFiltersType.value = "categories"
                        }
                    },
                    onSearchClick = {
                        listingViewModel.isOpenSearch.value = true
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
                                listingData = listingData.value,
                                regionsOptions = regions,
                                onClosed = onClose,
                            )
                        }
                        "sorting" -> {
                            SortingListingContent(
                                isRefreshingFromFilters,
                                listingData.value,
                                onClose
                            )
                        }
                        "categories" ->{
                            CategoryContent(
                                baseViewModel = listingViewModel,
                                searchData = searchData.value,
                                listingData = listingData.value,
                                goListing = {
                                    isRefreshingFromFilters.value = true
                                    listingViewModel.activeFiltersType.value = ""
                                },
                            )
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
                    listingViewModel.isOpenSearch.value = true
                }
            )
        }
    }
}
