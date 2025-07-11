package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.listing.ListingBaseContent
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.dialogs.AccessDialog
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
    val data = subViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = subViewModel.updateItem.collectAsState()
    val titleDialog = subViewModel.titleDialog.collectAsState()
    val deleteId = subViewModel.deleteId.collectAsState()
    val listingBaseViewModel = subViewModel.listingBaseViewModel
    val listingDataState = listingBaseViewModel.listingData.collectAsState()

    val createSubBtn = subViewModel.filterListingBtnItem.collectAsState()

    val listingData = listingDataState.value.data

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                NoItemsFoundLayout(
                    title = stringResource(strings.emptySubscriptionsLabel),
                    image = drawables.emptyFavoritesImage
                ) {
                    subViewModel.refresh()
                }
            }
        } else {
            null
        }
    }

    val err = subViewModel.errorMessage.collectAsState()

    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { OnError(err.value) { subViewModel.refresh() } }
    }else{
        null
    }

    BackHandler(modelState.value.backHandler){
        subViewModel.backClick()
    }

    BaseContent(
        topBar = null,
        onRefresh = {
            component.onRefresh()
        },
        error = error,
        noFound = null,
        isLoading = isLoading.value,
        toastItem = subViewModel.toastItem.value,
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallIconButton(
                drawables.newLotIcon,
                color = colors.positiveGreen,
                modifier = Modifier.size(dimens.smallIconSize)
            ) {
                component.goToCreateNewSubscription()
            }

            if (listingData.sort != null){
                ActiveFilterListingItem(createSubBtn.value.first())
            }

            SmallIconButton(
                drawables.sortIcon,
                color = colors.black
            ){
                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
            }
        }

        ListingBaseContent(
            data = data,
            viewModel = listingBaseViewModel,
            noFound = noFound,
            filtersContent = { activeWindowType ->
                when (activeWindowType){
                    ActiveWindowListingType.SORTING -> {
                        SortingOrdersContent(
                            listingData.sort,
                        ){ newSort ->
                            listingBaseViewModel.applySorting(newSort)
                        }
                    }
                    else -> {}
                }
            },
            item = { subscription ->
                SubscriptionItem(
                    subscription,
                    updateItem.value
                )
            }
        )

        AccessDialog(
            showDialog = deleteId.value != 1L,
            title = titleDialog.value,
            onDismiss = {
                subViewModel.closeDialog()
            },
            onSuccess = {
                subViewModel.deleteSubscription(deleteId.value)
            }
        )
    }
}
