package market.engine.fragments.root.main.basket

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.base.listing.rememberLazyScrollState
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

    val scrollState = rememberLazyScrollState(viewModel)

    val basketState by viewModel.uiState.collectAsState()
    val basketItemsState by viewModel.uiDataState.collectAsState()

    val deleteIds = basketState.deleteIds
    val subtitle = basketState.subtitle

    val isLoading by viewModel.isShowProgress.collectAsState()
    val isError by viewModel.errorMessage.collectAsState()
    val toastItem by viewModel.toastItem.collectAsState()

    val noFound: (@Composable () ->Unit)? = remember(basketItemsState) {
        if (basketItemsState.isEmpty()) {
            {
                NoItemsFoundLayout(
                    image = drawables.cartEmptyIcon,
                    title = stringResource(strings.cardIsEmptyLabel),
                    textButton = stringResource(strings.startShoppingLabel),
                    viewModel = viewModel,
                    goToOffer = { offer ->
                        component.goToOffer(offer.id)
                    }
                ) {
                    //go to listing
                    component.goToListing()
                }
            }
        } else {
            null
        }
    }

    val error : (@Composable () ->Unit)? = remember(isError) {
        if (isError.humanMessage.isNotBlank()) {
            {
                OnError(
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

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = basketState.appBarData
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
        isLoading = isLoading,
        toastItem = toastItem,
        modifier = Modifier.fillMaxSize()
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            listModifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            state = scrollState.scrollState,
            contentPadding = contentPadding,
        ) {
            items(basketItemsState) { itemState ->
                BasketItemContent(
                    state = itemState,
                    events = basketState.basketEvents
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
                )
            }
        )
    }
}
