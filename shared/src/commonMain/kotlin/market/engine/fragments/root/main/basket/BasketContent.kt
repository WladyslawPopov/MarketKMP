package market.engine.fragments.root.main.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.bars.SimpleAppBar
import market.engine.widgets.dialogs.AccessDialog
import market.engine.widgets.items.BasketItemContent
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun BasketContent(
    component: BasketComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.basketViewModel

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.firstVisibleItem.value
    )

    val basketState = viewModel.uiState.collectAsState()
    val basketItemsState = viewModel.uiDataState.collectAsState()
    val deleteIds = viewModel.deleteIds.collectAsState()

    val isLoading = remember(basketState.value.isLoading) { basketState.value.isLoading }
    val isError = remember(basketState.value.errorMessage) { basketState.value.errorMessage }

    BackHandler(
        modelState.value.backHandler
    ){

    }

    LaunchedEffect(state){
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            viewModel.firstVisibleItem.value = it
        }
    }

    val noFound: (@Composable () ->Unit)? = remember(basketItemsState.value) {
        if (basketItemsState.value.isEmpty()) {
            {
                showNoItemLayout(
                    image = drawables.cartEmptyIcon,
                    title = stringResource(strings.cardIsEmptyLabel),
                    textButton = stringResource(strings.startShoppingLabel),
                ) {
                    //go to listing
                    component.goToListing()
                }
            }
        } else {
            null
        }
    }

    val error : (@Composable () ->Unit)? = remember(isError.humanMessage) {
        if (isError.humanMessage.isNotBlank()) {
            {
                onError(
                    isError
                ) {
                    viewModel.onError(ServerErrorException())
                    viewModel.getUserCart()
                }
            }
        } else {
            null
        }
    }

    BaseContent(
        topBar = {
            if (basketState.value.appBarData != null) {
                SimpleAppBar(
                    basketState.value.appBarData!!
                )
            }
        },
        onRefresh = {
            viewModel.refresh()
        },
        error = error,
        noFound = noFound,
        isLoading = isLoading,
        toastItem = viewModel.toastItem,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumnWithScrollBars(
            modifierList = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            state = state,
            contentPadding = dimens.smallPadding
        ) {
            items(basketItemsState.value, key = { item -> item.user.id }) { itemState ->
                if (basketState.value.basketEvents != null) {
                    BasketItemContent(
                        state = itemState,
                        events = basketState.value.basketEvents!!
                    )
                }
            }
        }

        AccessDialog(
            deleteIds.value.isNotEmpty(),
            buildAnnotatedString {
                if (deleteIds.value.size == 1) {
                    append(stringResource(strings.warningDeleteOfferBasket))
                } else {
                    append(stringResource(strings.warningDeleteSelectedOfferFromBasket))
                }
            },
            onDismiss = {
                viewModel.clearDeleteIds()
            },
            onSuccess = {
                viewModel.deleteItems(
                    deleteIds.value
                ) {
                    viewModel.clearDeleteIds()
                    viewModel.refresh()
                }
            }
        )
    }
}
