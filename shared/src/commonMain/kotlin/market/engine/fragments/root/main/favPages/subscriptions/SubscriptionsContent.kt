package market.engine.fragments.root.main.favPages.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
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
    val viewModel = modelState.value.subViewModel
    val data = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val updateItem = viewModel.updateItem.collectAsState()
    val titleDialog = viewModel.titleDialog.collectAsState()
    val deleteId = viewModel.deleteId.collectAsState()
    val listingBaseViewModel = viewModel.listingBaseViewModel
    val listingDataState = listingBaseViewModel.listingData.collectAsState()

    val createSubBtn = viewModel.filterListingBtnItem.collectAsState()
    val activeType = listingBaseViewModel.activeWindowType.collectAsState()

    val listingData = listingDataState.value.data

    val listingScroll = rememberLazyScrollState(viewModel)

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val noFound = remember(data.loadState.refresh) {
        if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
            @Composable {
                NoItemsFoundLayout(
                    title = stringResource(strings.emptySubscriptionsLabel),
                    image = drawables.emptyFavoritesImage
                ) {
                    viewModel.refresh()
                }
            }
        } else {
            null
        }
    }

    val err = viewModel.errorMessage.collectAsState()

    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { OnError(err.value) { viewModel.refresh() } }
        }else{
            null
        }
    }

    BackHandler(modelState.value.backHandler){
        viewModel.backClick()
    }

    when (activeType.value) {
        ActiveWindowListingType.SORTING -> {
            SortingOrdersContent(
                listingData.sort,
                modifier
            ){ newSort ->
                listingBaseViewModel.applySorting(newSort)
            }
        }

        else -> {
            EdgeToEdgeScaffold(
                topBar = {
                    Row(
                        modifier = Modifier.background(colors.primaryColor).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.mediumPadding,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallIconButton(
                            drawables.newLotIcon,
                            color = colors.positiveGreen,
                            modifier = Modifier.size(dimens.smallIconSize)
                        ) {
                            component.goToCreateNewSubscription()
                        }

                        if (listingData.sort != null) {
                            ActiveFilterListingItem(createSubBtn.value.first())
                        }

                        SmallIconButton(
                            drawables.sortIcon,
                            color = colors.black
                        ) {
                            listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
                        }
                    }
                },
                onRefresh = {
                    viewModel.refresh()
                },
                error = error,
                noFound = noFound,
                isLoading = isLoading.value,
                toastItem = viewModel.toastItem.value,
                modifier = modifier.fillMaxSize()
            ) { contentPadding ->
                PagingLayout(
                    data = data,
                    viewModel = listingBaseViewModel,
                    state = listingScroll.scrollState,
                    contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()),
                    content = { subscription ->
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
                        viewModel.closeDialog()
                    },
                    onSuccess = {
                        viewModel.deleteSubscription(deleteId.value)
                    }
                )
            }
        }
    }
}
