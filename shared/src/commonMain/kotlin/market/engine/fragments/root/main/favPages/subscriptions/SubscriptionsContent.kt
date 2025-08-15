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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.FilterListingBtnItem
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.items.ActiveFilterListingItem
import market.engine.widgets.buttons.SmallIconButton
import market.engine.fragments.base.listing.PagingLayout
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.dialogs.AccessDialog
import market.engine.widgets.filterContents.sorts.SortingOrdersContent
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

    val listingBaseViewModel = viewModel.listingBaseViewModel

    val listingDataState by listingBaseViewModel.listingData.collectAsState()

    val activeType by listingBaseViewModel.activeWindowType.collectAsState()

    val updateItem by viewModel.updateItem.collectAsState()

    val listingData = listingDataState.data

    val listingScroll = rememberLazyScrollState(viewModel)

    val isLoading : State<Boolean> = rememberUpdatedState(data.loadState.refresh is LoadStateLoading)

    val noFound: @Composable (() -> Unit)? = remember(data.loadState.refresh, activeType) {

        when {
            activeType == ActiveWindowListingType.LISTING -> {
                if (data.loadState.refresh is LoadStateNotLoading && data.itemCount < 1) {
                    @Composable {
                        NoItemsFoundLayout(
                            title = stringResource(strings.emptySubscriptionsLabel),
                            image = drawables.emptyFavoritesImage,
                            viewModel = viewModel,
                            goToOffer = { offer ->
                                component.goToOffer(offer.id)
                            }
                        ) {
                            listingBaseViewModel.refresh()
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

    val err = viewModel.errorMessage.collectAsState()

    val error : (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            { OnError(err.value) { viewModel.refresh() } }
        }else{
            null
        }
    }

    when (activeType) {
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
                        if (listingData.sort != null) {
                            ActiveFilterListingItem(
                                FilterListingBtnItem(
                                    text = listingData.sort?.interpretation ?: "",
                                    removeFilter = {
                                        listingBaseViewModel.setListingData(
                                            listingDataState.copy(
                                                data = listingData.copy(
                                                    sort = null
                                                )
                                            )
                                        )
                                        listingBaseViewModel.refresh()
                                    },
                                    itemClick = {
                                        listingBaseViewModel.setActiveWindowType(
                                            ActiveWindowListingType.SORTING
                                        )
                                    }
                                ))
                        }

                        SmallIconButton(
                            drawables.newLotIcon,
                            color = colors.positiveGreen,
                            modifier = Modifier.size(dimens.smallIconSize)
                        ) {
                            component.goToCreateNewSubscription()
                        }

                        SmallIconButton(
                            drawables.sortIcon,
                            color = colors.black,
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
                            updateItem
                        )
                    }
                )
                val titleDialog by viewModel.titleDialog.state.collectAsState()
                val deleteId by viewModel.deleteId.state.collectAsState()

                AccessDialog(
                    showDialog = deleteId != 1L,
                    title = AnnotatedString(titleDialog),
                    onDismiss = {
                        viewModel.closeDialog()
                    },
                    onSuccess = {
                        viewModel.deleteSubscription(deleteId)
                    }
                )
            }
        }
    }
}
