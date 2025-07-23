package market.engine.fragments.root.main.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.widgets.filterContents.search.SearchContent
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.SubCategoryBar
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.filterContents.FilterListingContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.items.offer_Items.PromoOfferRowItem
import market.engine.widgets.items.offer_Items.PublicOfferItemGrid
import market.engine.widgets.items.offer_Items.PublicOfferItem
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val viewModel = model.listingViewModel
    val uiState by viewModel.listingDataState.collectAsState()
    val errorString by viewModel.errorString.collectAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val toastItem by viewModel.toastItem.collectAsState()

    val err by viewModel.errorMessage.collectAsState()
    val listingBaseModel = viewModel.listingBaseVM

    val searchDataState = listingBaseModel.searchDataState.collectAsState()

    val isLoadingListing: State<Boolean> =
        rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val regions = viewModel.regionOptions.collectAsState()

    val categoryViewModel = viewModel.listingCategoryModel

    val listingDataState by listingBaseModel.listingData.collectAsState()
    val activeType by listingBaseModel.activeWindowType.collectAsState()
    val updateItem by viewModel.updateItem.collectAsState()

    val listingData = listingDataState.data
    val searchData = listingDataState.searchData

    val listingState = rememberLazyScrollState(viewModel)

    BackHandler(model.backHandler) {
        viewModel.backClick()
    }

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            { OnError(err) { viewModel.refresh() } }
        } else {
            null
        }
    }

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh, activeType) {
        when {
            activeType == ActiveWindowListingType.LISTING -> {
                if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
                    @Composable {
                        if (listingData.filters.any { it.interpretation?.isNotBlank() == true } ||
                            searchData.userSearch || searchData.searchString.isNotEmpty()) {
                            NoItemsFoundLayout(
                                textButton = stringResource(strings.resetLabel)
                            ) {
                                listingBaseModel.clearListingData()
                            }
                        } else {
                            NoItemsFoundLayout {
                                listingBaseModel.refresh()
                            }
                        }
                    }
                } else {
                    null
                }
            }

            else -> {
                null
            }
        }
    }

    LaunchedEffect(listingDataState) {
        categoryViewModel.updateFromSearchData(searchData)
        categoryViewModel.initialize(listingData.filters)
    }

    when (activeType) {
        ActiveWindowListingType.FILTERS -> {
            FilterListingContent(
                modifier = Modifier.padding(top = TopAppBarDefaults.TopAppBarExpandedHeight),
                initialFilters = listingData.filters,
                regionsOptions = regions.value,
                onClosed = { newList ->
                    listingBaseModel.applyFilters(newList)
                },
                onClear = {
                    listingBaseModel.clearAllFilters()
                }
            )
        }

        ActiveWindowListingType.SORTING -> {
            SortingOffersContent(
                listingData.sort,
                isCabinet = false,
                modifier = Modifier.padding(top = TopAppBarDefaults.TopAppBarExpandedHeight),
                onClose = { newSort ->
                    listingBaseModel.applySorting(newSort)
                }
            )
        }

        ActiveWindowListingType.SEARCH, ActiveWindowListingType.CATEGORY_FILTERS -> {
            searchDataState.value?.let {
                SearchContent(
                    it,
                    component.searchPages,
                )
            }
        }

        else -> {
            EdgeToEdgeScaffold(
                modifier = modifier.fillMaxSize(),
                isLoading = isLoadingListing.value && activeType == ActiveWindowListingType.LISTING,
                toastItem = toastItem,
                onRefresh = viewModel::updatePage,
                error = error,
                noFound = noFound,
                topBar = {
                    SimpleAppBar(
                        data = uiState.appBarData,
                        color = if (!listingState.areBarsVisible.value)
                            colors.primaryColor.copy(alphaBars)
                        else
                            colors.primaryColor
                    )
                    {
                        SubCategoryBar(
                            searchData.searchCategoryName
                        ){
                            viewModel.changeOpenCategory()
                        }
                    }

                    val filterBarUiState by listingBaseModel.filterBarUiState.collectAsState()

                    FiltersBar(
                        filterBarUiState,
                        isVisible = listingBaseModel.isHideFilterBar(listingState, noFound != null)
                    )
                }
            ) {
                contentPadding ->
                when (activeType) {
                    ActiveWindowListingType.CATEGORY -> {
                        AnimatedVisibility(
                            activeType == ActiveWindowListingType.CATEGORY,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            CategoryContent(
                                categoryViewModel,
                                modifier = Modifier.padding(top = contentPadding.calculateTopPadding()),
                                onCompleted = {
                                    viewModel.changeOpenCategory(true)
                                },
                                onClose = {
                                    viewModel.changeOpenCategory()
                                }
                            )
                        }
                    }

                    else -> {
                        PagingLayout(
                            viewModel = listingBaseModel,
                            contentPadding = contentPadding,
                            data = data,
                            state = listingState.scrollState,
                            content = { offer ->
                                if (listingBaseModel.listingType.value == 0) {
                                    PublicOfferItem(
                                        offer,
                                        updateItem,
                                    )
                                } else {
                                    PublicOfferItemGrid(
                                        offer,
                                        updateItem,
                                    )
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
                    errorString != "",
                    errorString,
                    onDismiss = {
                        viewModel.clearErrorSubDialog()
                    },
                    goToSubscribe = {
                        component.goToSubscribe()
                        viewModel.clearErrorSubDialog()
                    }
                )
            }
        }
    }
}
