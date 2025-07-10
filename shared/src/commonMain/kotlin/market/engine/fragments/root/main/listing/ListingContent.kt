package market.engine.fragments.root.main.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.constants.PAGE_SIZE
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.ScrollDataState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.filterContents.search.SearchContent
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.OnError
import market.engine.fragments.base.NoItemsFoundLayout
import market.engine.widgets.bars.DeletePanel
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.appBars.CloseAppBar
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.filterContents.FilterListingContent
import market.engine.widgets.filterContents.SortingOffersContent
import market.engine.widgets.filterContents.categories.CategoryContent
import market.engine.widgets.items.offer_Items.PromoOfferRowItem
import market.engine.widgets.items.offer_Items.PublicOfferItemGrid
import market.engine.widgets.items.offer_Items.PublicOfferItem
import market.engine.widgets.textFields.SearchTextField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = dimens.bottomBar,
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val viewModel = model.listingViewModel
    val uiState = viewModel.listingDataState.collectAsState()
    val errorString = viewModel.errorString.collectAsState()
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val toastItem = viewModel.toastItem.collectAsState()

    val err = viewModel.errorMessage.collectAsState()
    val listingBaseModel = viewModel.listingBaseVM
    val updateItem = listingBaseModel.updateItem.collectAsState()
    val searchDataState = listingBaseModel.searchDataState.collectAsState()

    val isLoadingListing: State<Boolean> =
        rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val activeWindowType = listingBaseModel.activeWindowType.collectAsState()

    val regions = viewModel.regionOptions.collectAsState()
    val categoryViewModel = viewModel.listingCategoryModel
    val listingDataState = listingBaseModel.listingData.collectAsState()
    val listingData = listingDataState.value.data
    val searchData = listingDataState.value.searchData

    val scrollStateData = viewModel.scrollState.collectAsState()
    val activeType = listingBaseModel.activeWindowType.collectAsState()
    val filterBarUiState = listingBaseModel.filterBarUiState.collectAsState()

    val catDef = remember(listingBaseModel.catDef.value) { listingBaseModel.catDef.value }

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollStateData.value.scrollItem,
        initialFirstVisibleItemScrollOffset = scrollStateData.value.offsetScrollItem
    )

    val selectedItems = listingBaseModel.selectItems.collectAsState()


    var previousIndex by remember { mutableStateOf(3) }

    val currentPage by remember {
        derivedStateOf {
            (scrollState.firstVisibleItemIndex / PAGE_SIZE) + 1
        }
    }

    val isTabsVisible = remember{ mutableStateOf(false) }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->

            if (index < previousIndex) {
                isTabsVisible.value = true
            } else if (index > previousIndex) {
                isTabsVisible.value = false
            }

            if (currentPage == 0) {
                isTabsVisible.value = true
            }

            if (index > previousIndex || index < previousIndex)
                previousIndex = index


            viewModel.updateScroll(ScrollDataState(index, offset))
        }
    }


    BackHandler(model.backHandler) {
        viewModel.backClick()
    }

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { OnError(err.value) { viewModel.refresh() } }
        } else {
            null
        }
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                if (listingData.filters.any { it.interpretation != null && it.interpretation != "" } ||
                    searchData.userSearch || searchData.searchString.isNotEmpty()
                ) {
                    NoItemsFoundLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        listingBaseModel.clearListingData()
                    }
                } else {
                    NoItemsFoundLayout {
                        viewModel.refresh()
                    }
                }
            }
        } else {
            null
        }
    }

    BaseContent(
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        colors.primaryColor.copy(if(!isTabsVisible.value) 0.8f else 1f),
                        MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .zIndex(15f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            )
            {
                when (activeWindowType.value) {
                    ActiveWindowListingType.SEARCH -> {
                        searchDataState.value?.run {
                            SimpleAppBar(
                                modifier = Modifier,
                                data = appBarData
                            ) {
                                SearchTextField(
                                    activeWindowType.value == ActiveWindowListingType.SEARCH,
                                    searchString,
                                    onValueChange = { newVal ->
                                        searchEvents.updateSearch(
                                            newVal
                                        )
                                    },
                                    goToListing = {
                                        searchEvents.goToListing()
                                    },
                                    onClearSearch = {
                                        listingBaseModel.clearSearch()
                                    }
                                )
                            }
                        }
                    }
                    ActiveWindowListingType.CATEGORY_FILTERS -> {
                        CloseAppBar {
                            listingBaseModel.openSearchCategory(value = false, complete = false)
                        }
                    }
                    else -> {
                        SimpleAppBar(
                            data = uiState.value.appBarData,
                            color = colors.transparent
                        )
                        {
                            Row(
                                modifier = Modifier
                                    .background(colors.white, MaterialTheme.shapes.small)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        viewModel.changeOpenCategory()
                                    }
                                    .fillMaxWidth()
                                    .padding(dimens.smallPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            )
                            {
                                Icon(
                                    painterResource(drawables.listIcon),
                                    contentDescription = null,
                                    tint = colors.black,
                                    modifier = Modifier.size(dimens.extraSmallIconSize)
                                )

                                Text(
                                    text = searchData.searchCategoryName.ifEmpty { catDef },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.black,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Icon(
                                    painterResource(drawables.nextArrowIcon),
                                    contentDescription = null,
                                    tint = colors.black,
                                    modifier = Modifier.size(dimens.extraSmallIconSize)
                                )
                            }
                        }
                    }
                }

                DeletePanel(
                    selectedItems.value.size,
                    onCancel = {
                        listingBaseModel.clearSelectedItems()
                    },
                    onDelete = {
                        listingBaseModel.deleteSelectedItems()
                    }
                )

                if (activeType.value == ActiveWindowListingType.LISTING ||
                    activeType.value == ActiveWindowListingType.CATEGORY
                ) {
                    AnimatedVisibility(
                        visible = isTabsVisible.value,
                        enter = fadeIn(),
                        exit = fadeOut()
                    )
                    {
                        FiltersBar(
                            filterBarUiState.value
                        )
                    }
                }
            }
        },
        onRefresh = viewModel::updatePage,
        error = error,
        noFound = null,
        isLoading = isLoadingListing.value && activeWindowType.value != ActiveWindowListingType.SEARCH,
        toastItem = toastItem.value,
        modifier = modifier.fillMaxSize()
    ) { appBarPadding ->
        ListingBaseContent(
            viewModel = listingBaseModel,
            contentPadding = PaddingValues(top = appBarPadding, bottom = bottomPadding),
            data = data,
            scrollState = scrollState,
            noFound = noFound,
            filtersContent = { bottomSheetContentType ->
                when (bottomSheetContentType) {
                    ActiveWindowListingType.FILTERS -> {
                        FilterListingContent(
                            modifier = Modifier.padding(top = dimens.extraLargeSpacer, bottom = dimens.extraLargeSpacer),
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
                            modifier = Modifier.padding(top = dimens.extraLargeSpacer, bottom = dimens.extraLargeSpacer),
                            onClose = { newSort ->
                                listingBaseModel.applySorting(newSort)
                            }
                        )
                    }

                    ActiveWindowListingType.SEARCH, ActiveWindowListingType.CATEGORY_FILTERS -> {
                        searchDataState.value?.let{
                            SearchContent(
                                it,
                                component.searchPages,
                                modifier = Modifier.padding(top = dimens.extraLargeSpacer, bottom = dimens.appBar),
                            )
                        }
                    }

                    ActiveWindowListingType.CATEGORY -> {
                        CategoryContent(
                            categoryViewModel,
                            modifier = Modifier
                                .padding(top = dimens.appBar, bottom = dimens.extraLargeSpacer*2)
                                .background(colors.primaryColor),
                            onCompleted = {
                                viewModel.changeOpenCategory(true)
                            },
                            onClose = {
                                viewModel.changeOpenCategory()
                            }
                        )
                    }

                    else -> {}
                }
            },
            item = { offer ->
                if (listingBaseModel.listingType.value == 0) {
                    PublicOfferItem(
                        offer,
                        updateItem.value,
                    )
                } else {
                    PublicOfferItemGrid(
                        offer,
                        updateItem.value,
                    )
                }
            },
            promoContent = { offer ->
                PromoOfferRowItem(
                    offer
                ) {
                    component.goToOffer(offer, true)
                }
            },
        )

        CreateSubscribeDialog(
            errorString.value != "",
            errorString.value,
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
