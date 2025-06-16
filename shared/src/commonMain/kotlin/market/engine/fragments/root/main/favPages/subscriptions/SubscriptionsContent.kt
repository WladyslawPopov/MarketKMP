package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.filterContents.SortingOrdersContent
import market.engine.widgets.items.SubscriptionItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubscriptionsContent(
    component: SubscriptionsComponent,
    modifier: Modifier,
) {
    val modelState = component.model.subscribeAsState()
    val subViewModel = modelState.value.subViewModel
    val uiState = subViewModel.subContentState.collectAsState()
    val listingData = uiState.value.listingData.data
    val activeWindowType = uiState.value.listingBaseState.activeWindowType
    val data = subViewModel.pagingDataFlow.collectAsLazyPagingItems()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val refresh = remember {
        {
            subViewModel.onError(ServerErrorException())
            subViewModel.resetScroll()
            subViewModel.refresh()
            data.refresh()
        }
    }

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                showNoItemLayout(
                    title = stringResource(strings.emptySubscriptionsLabel),
                    image = drawables.emptyFavoritesImage
                ) {
                    refresh()
                }
            }
        } else {
            null
        }
    }

    val err = subViewModel.errorMessage.collectAsState()

    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err.value) { refresh() } }
    }else{
        null
    }

    BackHandler(modelState.value.backHandler){
        subViewModel.backClick()
    }

    //update item when we back
    LaunchedEffect(subViewModel.updateItem.value) {
        if (subViewModel.updateItem.value != null) {
            val oldItem = data.itemSnapshotList.find { it?.id == subViewModel.updateItem.value }
            component.updateItem(oldItem)
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
            uiState = uiState.value.listingBaseState,
            data = data,
            baseViewModel = subViewModel,
            noFound = noFound,
            filtersContent = {
                when (activeWindowType){
                    ActiveWindowListingType.SORTING -> {
                        SortingOrdersContent(
                            listingData,
                        ){ update ->

                        }
                    }
                    else -> {}
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

                    if (listingData.sort != null){
                        ActiveFilterListingItem(uiState.value.activeFilterListingBtnItem)
                    }

                    SmallIconButton(
                        drawables.sortIcon,
                        color = colors.black
                    ){
                        subViewModel.openSort()
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
                            component.goToListing(subscription)
                        }
                    )
                }
            }
        )
    }
}
