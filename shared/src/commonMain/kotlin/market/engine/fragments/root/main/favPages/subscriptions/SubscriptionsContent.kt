package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.WindowType
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.getWindowType
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.bars.ActiveFilterListing
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.exceptions.BackHandler
import market.engine.widgets.exceptions.showNoItemLayout
import market.engine.widgets.filterContents.SortingOrdersContent
import org.jetbrains.compose.resources.stringResource



@Composable
fun SubscriptionsContent(
    component: SubscriptionsComponent,
    modifier: Modifier,
) {
    val modelState = component.model.subscribeAsState()
    val subViewModel = modelState.value.subViewModel
    val searchData = subViewModel.listingData.value.searchData
    val listingData = subViewModel.listingData.value.data
    val data = modelState.value.pagingDataFlow.collectAsLazyPagingItems()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val windowClass = getWindowType()
    val isBigScreen = windowClass == WindowType.Big

    val successToast = stringResource(strings.operationSuccess)

    val columns = remember { mutableStateOf(if (isBigScreen) 2 else 1) }

    val refresh = {
        subViewModel.onError(ServerErrorException())
        subViewModel.resetScroll()
        subViewModel.refresh()
    }

    val noFound = @Composable {
        if (listingData.value.filters.any {it.interpritation != null && it.interpritation != "" }){
            showNoItemLayout(
                textButton = stringResource(strings.resetLabel)
            ){
                listingData.value.filters.clear()
                listingData.value.filters.addAll(OfferFilters.filtersFav.toList())
                refresh()
            }
        }else {
            showNoItemLayout(
                title = stringResource(strings.emptySubscriptionsLabel),
                image = drawables.emptyFavoritesImage
            ) {
                refresh()
            }
        }
    }

    BackHandler(modelState.value.backHandler){
        when{
            subViewModel.activeFiltersType.value != "" ->{
                subViewModel.activeFiltersType.value = ""
            }
            else -> {

            }
        }
    }

    //update item when we back
    LaunchedEffect(subViewModel.updateItem.value) {
        if (subViewModel.updateItem.value != null) {
            withContext(Dispatchers.Main) {

            }
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
           refresh()
        },
        error = null,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = subViewModel.toastItem,
        modifier = modifier.fillMaxSize()
    ) {
        ListingBaseContent(
            listingData = listingData.value,
            data = data,
            searchData = searchData.value,
            baseViewModel = subViewModel,
            onRefresh = {
                subViewModel.resetScroll()
                data.refresh()
            },
            noFound = noFound,
            columns = columns,
            filtersContent = { isRefreshingFromFilters, onClose ->
                when (subViewModel.activeFiltersType.value){
                    "sorting" -> SortingOrdersContent(
                        isRefreshingFromFilters,
                        listingData.value,
                        onClose
                    )
                }
            },
            additionalBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallIconButton(
                        drawables.newLotIcon,
                        color = colors.inactiveBottomNavIconColor
                    ){
                        component.goToCreateNewSubscription()
                    }

                    if (listingData.value.sort != null){
                        if (listingData.value.sort != null){
                            ActiveFilterListing(
                                text = listingData.value.sort?.interpritation ?: "",
                                removeFilter = {
                                    listingData.value.sort = null
                                    refresh()
                                },
                            ){
                                subViewModel.activeFiltersType.value = "sorting"
                            }
                        }
                    }

                    SmallIconButton(
                        drawables.sortIcon,
                        color = colors.black
                    ){
                        subViewModel.activeFiltersType.value = "sorting"
                    }
                }
            },
            item = { subscription ->
                SubscriptionItem(subscription, listingData.value, searchData.value, subViewModel)
            }
        )
    }
}
