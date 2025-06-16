package market.engine.fragments.root.main.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
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
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.filterContents.search.SearchContent
import market.engine.widgets.bars.FiltersBar
import market.engine.widgets.bars.SwipeTabsBar
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
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
    val listingViewModel = model.listingViewModel
    val uiState = listingViewModel.listingDataState.collectAsState()
    val searchDataState = listingViewModel.searchDataState.collectAsState()

    val updateItem = listingViewModel.updateItem.collectAsState()
    val errorString = listingViewModel.errorString.collectAsState()
    val data = listingViewModel.pagingDataFlow.collectAsLazyPagingItems()

    val isLoadingListing: State<Boolean> =
        rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val err = listingViewModel.errorMessage.collectAsState()

    val listingEvents = uiState.value.listingEvents
    val listingData = uiState.value.listingData.data
    val searchData = uiState.value.listingData.searchData
    val activeWindowType = uiState.value.listingBaseState.activeWindowType

    val listingBaseData = uiState.value.listingBaseState
    val regions = uiState.value.regions

    val catDef = listingViewModel.catDef.value

    BackHandler(model.backHandler) {
        listingEvents.backClick()
    }

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { onError(err.value) { listingEvents.onRefresh() } }
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
                    showNoItemLayout(
                        textButton = stringResource(strings.resetLabel)
                    ) {
                        listingEvents.clearListingData()
                    }
                } else {
                    showNoItemLayout {
                        listingEvents.onRefresh()
                    }
                }
            }
        } else {
            null
        }
    }

    BaseContent(
        topBar = {
            when (activeWindowType) {
                ActiveWindowListingType.SEARCH -> {
                    SimpleAppBar(
                        modifier = Modifier,
                        data = searchDataState.value.appBarData,
                    ){
                        SearchTextField(
                            activeWindowType == ActiveWindowListingType.SEARCH,
                            searchDataState.value.searchString,
                            onValueChange = { newVal ->
                                searchDataState.value.searchEvents.updateSearch(
                                    newVal
                                )
                            },
                            goToListing = {
                                searchDataState.value.searchEvents.goToListing()
                            },
                            onClearSearch = {
                                listingViewModel.clearSearch()
                            }
                        )
                    }
                }

                ActiveWindowListingType.CATEGORY_FILTERS -> {
                    CloseAppBar {
                        listingViewModel.openSearchCategory(false, false)
                    }
                }

                else -> {
                    SimpleAppBar(
                        data = uiState.value.appBarData
                    ){
                        Row(
                            modifier = Modifier
                                .background(colors.white, MaterialTheme.shapes.small)
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    listingViewModel.changeOpenCategory(true)
                                }
                                .fillMaxWidth()
                                .padding(dimens.smallPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            Icon(
                                painterResource(drawables.listIcon),
                                contentDescription = null,
                                tint = colors.black,
                                modifier = Modifier.size(dimens.extraSmallIconSize)
                            )

                            Text(
                                text = if (searchData.searchCategoryName.isNotEmpty())
                                    searchData.searchCategoryName
                                else catDef,
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
        },
        onRefresh = listingEvents::onRefresh,
        error = error,
        noFound = null,
        isLoading = isLoadingListing.value && activeWindowType != ActiveWindowListingType.SEARCH,
        toastItem = listingViewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            uiState = listingBaseData,
            baseViewModel = listingViewModel,
            data = data,
            noFound = noFound,
            filtersContent = {
                when (activeWindowType) {
                    ActiveWindowListingType.FILTERS -> {
                        FilterListingContent(
                            initialFilters = listingData.filters,
                            regionsOptions = regions,
                            onClosed = { newList ->
                                listingViewModel.applyFilters(newList)
                            },
                            onClear = {
                                listingEvents.closeFilters(listingData, true)
                            }
                        )
                    }

                    ActiveWindowListingType.SORTING -> {
                        SortingOffersContent(
                            listingData,
                            isCabinet = false,
                            onClose = { update ->
                                listingEvents.closeFilters(listingData, false)
                            }
                        )
                    }

                    ActiveWindowListingType.SEARCH, ActiveWindowListingType.CATEGORY_FILTERS -> {
                        SearchContent(
                            searchDataState.value,
                            component.searchPages
                        )
                    }

                    ActiveWindowListingType.CATEGORY -> {
                        CategoryContent(
                            uiState.value.listingCategoryState.categoryViewModel,
                            onCompleted = {
                                listingEvents.clickCategory(true)
                            },
                            onClose = {
                                listingEvents.clickCategory(false)
                            }
                        )
                    }

                    else -> {}
                }
            },
            additionalBar = { state ->
                Column {
                    SwipeTabsBar(
                        isVisibility = true,
                        listingData,
                        state,
                        onRefresh = {
                            listingEvents.closeFilters(listingData, false)
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
                            updateItem.value,
                        )
                    }

                    1 -> {
                        PublicOfferItemGrid(
                            offer,
                            updateItem.value,
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
            },
        )

        CreateSubscribeDialog(
            errorString.value != "",
            errorString.value,
            onDismiss = {
                listingEvents.clearError()
            },
            goToSubscribe = {
                component.goToSubscribe()
                listingEvents.clearError()
            }
        )
    }
}
