package market.engine.fragments.root.main.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.ScrollDataState
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.dialogs.AccessDialog
import market.engine.widgets.items.BasketItemContent
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun BasketContent(
    component: BasketComponent,
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.basketViewModel

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.scrollState.value.scrollItem,
        initialFirstVisibleItemScrollOffset = viewModel.scrollState.value.offsetScrollItem
    )

    val basketState = viewModel.uiState.collectAsState()
    val basketItemsState = viewModel.uiDataState.collectAsState()
    val deleteIds = basketState.value.deleteIds
    val subtitle = basketState.value.subtitle

    val isLoading = viewModel.isShowProgress.collectAsState()
    val isError = viewModel.errorMessage.collectAsState()

    BackHandler(
        modelState.value.backHandler
    ){}

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.updateScroll(ScrollDataState(index, offset))
        }
    }

    val noFound: (@Composable () ->Unit)? = remember(basketItemsState.value) {
        if (basketItemsState.value.isEmpty()) {
            {
                NoItemsFoundLayout(
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

    val error : (@Composable () ->Unit)? = remember(isError.value) {
        if (isError.value.humanMessage.isNotBlank()) {
            {
                OnError(
                    isError.value
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
            SimpleAppBar(
                data = basketState.value.appBarData
            ){
                val title = stringResource(strings.yourBasketTitle)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    TextAppBar(title)

                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.titleTextColor
                        )
                    }
                }
            }
        },
        onRefresh = {
            viewModel.refreshPage()
        },
        error = error,
        noFound = noFound,
        isLoading = isLoading.value,
        toastItem = viewModel.toastItem.value,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumnWithScrollBars(
            modifierList = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            state = scrollState,
            contentPadding = dimens.smallPadding
        ) {
            items(basketItemsState.value, key = { item -> item.user.id }) { itemState ->
                BasketItemContent(
                    state = itemState,
                    events = basketState.value.basketEvents
                )
            }
        }

        AccessDialog(
            deleteIds.isNotEmpty(),
            buildAnnotatedString {
                if (deleteIds.size == 1) {
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
                    deleteIds
                ) {
                    viewModel.clearDeleteIds()
                    viewModel.refresh()
                }
            }
        )
    }
}
