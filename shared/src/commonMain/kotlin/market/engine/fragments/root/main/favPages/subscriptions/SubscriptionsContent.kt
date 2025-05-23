package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
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

    val price = stringResource(strings.priceParameterName)
    val from = stringResource(strings.fromAboutParameterName)
    val to = stringResource(strings.toAboutParameterName)
    val currency = stringResource(strings.currencyCode)

    val columns = remember { mutableStateOf(if (isBigScreen.value) 2 else 1) }

    val defCat = stringResource(strings.categoryMain)

    val refresh = {
        subViewModel.onError(ServerErrorException())
        subViewModel.resetScroll()
        subViewModel.refresh()
        data.refresh()
    }

    val noFound = @Composable {
        showNoItemLayout(
            title = stringResource(strings.emptySubscriptionsLabel),
            image = drawables.emptyFavoritesImage
        ) {
            refresh()
        }
    }

    val err = subViewModel.errorMessage.collectAsState()
    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err) { refresh() } }
    }else{
        null
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
            subViewModel.getSubscription(subViewModel.updateItem.value!!){ item ->
                val oldItem = data.itemSnapshotList.find { it?.id == subViewModel.updateItem.value }
                if (item != null) {
                    if (oldItem != null) {
                        oldItem.catpath = item.catpath
                        oldItem.isEnabled = item.isEnabled
                        oldItem.name = item.name
                        oldItem.priceFrom = item.priceFrom
                        oldItem.priceTo = item.priceTo
                        oldItem.region = item.region
                        oldItem.searchQuery = item.searchQuery
                        oldItem.saleType = item.saleType
                    }
                } else {
                    oldItem?.id = 1L
                }
                subViewModel.updateItemTrigger.value++
                subViewModel.updateItem.value = null
            }
        }
    }

    BaseContent(
        topBar = null,
        onRefresh = {
           refresh()
        },
        error = error,
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
            filtersContent = { isRefreshingFromFilters , onClose ->
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
                        color = colors.positiveGreen
                    ) {
                        component.goToCreateNewSubscription()
                    }

                    if (listingData.value.sort != null){
                        if (listingData.value.sort != null){
                            ActiveFilterListingItem(
                                text = listingData.value.sort?.interpretation ?: "",
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
                if (subscription.id != 1L && subViewModel.updateItemTrigger.value >= 0) {
                    SubscriptionItem(
                        subscription,
                        subViewModel,
                        goToEditSubscription = {
                            component.goToCreateNewSubscription(it)
                        },
                        onUpdateItem = {
                            subViewModel.updateItem.value = subscription.id
                            subViewModel.updateItemTrigger.value++
                        },
                        onItemClick = {
                            val ld = ListingData()
                            ld.data.value.filters = ListingFilters.getEmpty()

                            if(subscription.priceTo != null) {
                                ld.data.value.filters.find {
                                    it.key == "current_price" && it.operation == "lte"
                                }?.let{
                                    it.value = subscription.priceTo ?: ""
                                    it.interpretation = "$price $from - ${subscription.priceTo} $currency"
                                }
                            }

                            if(subscription.priceFrom != null) {
                                ld.data.value.filters.find {
                                    it.key == "current_price" && it.operation == "gte"
                                }?.let{
                                    it.value = subscription.priceFrom ?: ""
                                    it.interpretation = "$price $to - ${subscription.priceFrom} $currency"
                                }
                            }

                            if(subscription.region != null) {
                                ld.data.value.filters.find {
                                    it.key == "region"
                                }?.let {
                                   it.value = (subscription.region?.code ?: "").toString()
                                   it.interpretation = subscription.region?.name ?: ""
                                }
                            }

                            if(subscription.saleType != null) {
                                ld.data.value.filters.find {
                                    it.key == "sale_type"
                                }?.let {
                                    when (subscription.saleType) {
                                        "buy_now" -> {
                                            it.value = "buynow"
                                            it.interpretation = ""
                                        }
                                        "ordinary_auction" -> {
                                            it.value = "auction"
                                            it.interpretation = ""
                                        }
                                    }
                                }
                            }

                            if(subscription.sellerData != null){
                                ld.searchData.value.userSearch = true
                                ld.searchData.value.userID = subscription.sellerData.id
                                ld.searchData.value.userLogin = subscription.sellerData.login
                            }

                            ld.searchData.value.searchString = subscription.searchQuery ?: ""
                            ld.searchData.value.searchCategoryID = subscription.catpath?.keys?.firstOrNull() ?: 1L
                            ld.searchData.value.searchCategoryName = subscription.catpath?.values?.firstOrNull() ?: defCat

                            component.goToListing(ld)
                        }
                    )
                }
            }
        )
    }
}
