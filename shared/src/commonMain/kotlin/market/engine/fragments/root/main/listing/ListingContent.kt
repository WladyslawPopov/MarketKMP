package market.engine.fragments.root.main.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.strings
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
import market.engine.fragments.root.main.listing.ListingViewModel.ActiveWindowType
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
    val uiState = listingViewModel.listingDataState.collectAsState()

    val globalLD = listingViewModel.listingData.collectAsState()
    val listingData = globalLD.value.data
    val searchData = globalLD.value.searchData

    val data = uiState.value.pagingDataFlow?.collectAsLazyPagingItems()

    val isLoadingListing: State<Boolean> =
        rememberUpdatedState(data?.loadState?.refresh is LoadStateLoading)

    val regions = uiState.value.regions

    val uiSearchState = uiState.value.searchState
    
    val err = listingViewModel.errorMessage.collectAsState()
    
    val activeWindowType = listingViewModel.activeWindowType.collectAsState()

    val onRefresh = remember { {
        listingViewModel.resetScroll()
        listingViewModel.onError(ServerErrorException())
        listingViewModel.refresh()
        data?.refresh()
        searchData.isRefreshing = false
        listingViewModel.updateFilters.value = !listingViewModel.updateFilters.value
    } }

    LaunchedEffect(Unit) {
        component.refresh = onRefresh
    }

    BackHandler(model.backHandler) {
        when {
            listingViewModel.activeWindowType.value == ActiveWindowType.CATEGORY -> {
                listingViewModel.catBack.value = true
            }

            listingViewModel.activeWindowType.value != ActiveWindowType.LISTING -> {
                listingViewModel.activeWindowType.value = ActiveWindowType.LISTING
            }

            else -> {
                component.goBack()
            }
        }
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err.value) { onRefresh() } }
    } else {
        null
    }

    val noFound = remember(data?.loadState?.refresh) {
        if (data?.loadState?.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" } ||
                    searchData.userSearch || searchData.searchString.isNotEmpty()
                ) {
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        searchData.clear(listingViewModel.catDef.value)
                        listingData.filters = ListingFilters.getEmpty()
                        onRefresh()
                    }
                } else {
                    showNoItemLayout {
                        onRefresh()
                    }
                }
            }
        } else {
            null
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

    BaseContent(
        topBar = remember(activeWindowType.value){{
            when(activeWindowType.value) {
                    ActiveWindowType.SEARCH -> {
                        AnimatedVisibility(
                            uiSearchState.appBarData != null,
                            enter = fadeIn()
                        ) {
                            SimpleAppBar(
                                data = uiSearchState.appBarData!!
                            )
                        }
                    }

                    ActiveWindowType.CATEGORY_FILTERS -> {
                        AnimatedVisibility(
                            uiSearchState.appBarData != null,
                            enter = fadeIn()
                        ) {
                            SimpleAppBar(
                                data = uiSearchState.closeAppBar!!
                            )
                        }
                    }

                    else -> {
                        AnimatedVisibility(
                            uiSearchState.appBarData != null,
                            enter = fadeIn()
                        ) {
                            SimpleAppBar(
                                data = uiState.value.appBarData!!
                            )
                        }
                    }
                }
        }},
        onRefresh = onRefresh,
        error = error,
        noFound = null,
        isLoading = isLoadingListing.value && activeWindowType.value != ActiveWindowType.SEARCH,
        toastItem = listingViewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        if (data != null && uiState.value.listingBaseData != null && uiState.value.listingBaseEvents != null) {
            ListingBaseContent(
                uiState = uiState.value.listingBaseData!!,
                events = uiState.value.listingBaseEvents!!,
                data = data,
                noFound = noFound,
                filtersContent = {
                    when (activeWindowType.value) {
                        ActiveWindowType.FILTERS -> {
                            FilterListingContent(
                                activeWindowType.value == ActiveWindowType.FILTERS,
                                listingData = listingData.filters,
                                regionsOptions = regions,
                                onClosed = {
                                    listingViewModel.activeWindowType.value =
                                        ActiveWindowType.LISTING
                                },
                                onClear = {
                                    listingData.filters.clear()
                                    listingData.filters.addAll(ListingFilters.getEmpty())
                                    listingViewModel.activeWindowType.value =
                                        ActiveWindowType.LISTING
                                }
                            )
                        }
                        ActiveWindowType.SORTING -> {
                            SortingOffersContent(
                                activeWindowType.value == ActiveWindowType.SORTING,
                                listingData,
                                isCabinet = false,
                                onClose = {
                                    listingViewModel.activeWindowType.value = ActiveWindowType.LISTING
                                }
                            )
                        }
                        ActiveWindowType.SEARCH, ActiveWindowType.CATEGORY_FILTERS -> {
                            if ( uiState.value.searchEvents != null) {
                                SearchContent(
                                    uiSearchState,
                                    uiState.value.searchEvents!!,
                                    component.searchPages
                                )
                            }
                        }
                        ActiveWindowType.CATEGORY -> {
                            CategoryContent(
                                uiState.value.listingCategoryState.categoryViewModel,
                                onClose = {
                                    listingViewModel.changeOpenCategory()
                                }
                            )
                        }
                        ActiveWindowType.LISTING -> {}
                    }
                },
                additionalBar = { state ->
                    Column {
                        SwipeTabsBar(
                            isVisibility = true,
                            listingData,
                            state,
                            onRefresh = {
                                onRefresh()
                            }
                        )

                        FiltersBar(
                            uiState.value.filterBarData
                        )
                    }
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
}
