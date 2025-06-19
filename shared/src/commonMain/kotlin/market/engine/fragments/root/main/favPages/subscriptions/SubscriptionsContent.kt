package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import market.engine.fragments.base.ListingBaseContent
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
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
    val uiState = subViewModel.subContentState.collectAsState()
    val listingData = uiState.value.listingData.data
    val activeWindowType = uiState.value.listingBaseState.activeWindowType
    val data = subViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = subViewModel.updateItem.collectAsState()
    val titleDialog = subViewModel.titleDialog.collectAsState()
    val deleteId = subViewModel.deleteId.collectAsState()

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                showNoItemLayout(
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
        { onError(err.value) { subViewModel.refresh() } }
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
                            listingData.sort,
                        ){ newSort ->
                            subViewModel.applySorting(newSort)
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
