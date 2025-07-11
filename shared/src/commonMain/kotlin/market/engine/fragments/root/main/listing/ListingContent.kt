package market.engine.fragments.root.main.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.fragments.base.listing.ListingBaseContent
import market.engine.widgets.filterContents.search.SearchContent
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.fragments.base.listing.rememberListingState
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
    modifier: Modifier = Modifier
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
    val selectedItems = listingBaseModel.selectItems.collectAsState()

    val catDef = remember(listingBaseModel.catDef.value) { listingBaseModel.catDef.value }

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

    val listingState = rememberListingState(
        onScroll = viewModel::updateScroll,
        scrollStateData = scrollStateData.value
    )

    BaseContent(
        modifier = modifier.fillMaxSize(),
        isLoading = isLoadingListing.value && activeWindowType.value != ActiveWindowListingType.SEARCH,
        toastItem = toastItem.value,
        onRefresh = viewModel::updatePage,
        error = error,
        noFound = noFound,
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        colors.primaryColor.copy(if(!listingState.areBarsVisible.value) 0.8f else 1f),
                        MaterialTheme.shapes.small
                    ),
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
                        visible = listingState.areBarsVisible.value,
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
        }
    ) { contentPadding ->
        ListingBaseContent(
            viewModel = listingBaseModel,
            contentPadding = contentPadding,
            data = data,
            scrollState = listingState.scrollState,
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
