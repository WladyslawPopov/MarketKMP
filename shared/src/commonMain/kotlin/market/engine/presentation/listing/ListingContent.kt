package market.engine.presentation.listing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.core.types.CategoryScreenType

import market.engine.presentation.base.BaseContent
import market.engine.widgets.grids.PagingGrid
import market.engine.widgets.items.PromoLotItem
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import org.jetbrains.compose.resources.stringResource

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val searchData = component.searchData.collectAsState()
    val categoryType = searchData.value.categoryType

    when(categoryType){
        CategoryScreenType.SEARCH -> {
            component.goToSearch()
        }
        CategoryScreenType.CATEGORY -> {
            component.onBackClicked()
        }
        CategoryScreenType.LISTING -> {
            val modelState = component.model.subscribeAsState()
            val model = modelState.value
            val result = model.listing
            val offers = result.success?.collectAsLazyPagingItems()

            val isLoading : State<Boolean> = rememberUpdatedState(offers?.loadState?.refresh is LoadStateLoading)
            var error : (@Composable () -> Unit)? = null
            var noItem : (@Composable () -> Unit)? = null

            offers?.loadState?.apply {
                when {
                    refresh is LoadStateNotLoading && offers.itemCount < 1 -> {
                        noItem = {
                            showNoItemLayout {
                                component.onRefresh()
                            }
                        }
                    }

                    refresh is LoadStateError -> {
                        error = {
                            onError(
                                ServerErrorException(
                                    (offers.loadState.refresh as LoadStateError).error.message ?: "", ""
                                )
                            ) { offers.retry() }
                        }
                    }
                }
            }

            BaseContent(
                modifier = modifier,
                isLoading = isLoading,
                showVerticalScrollbar = false,
                topBar = {
                    ListingAppBar(
                        searchData.value.searchCategoryName ?: stringResource(strings.categoryMain),
                        modifier
                    ) {
                        component.onBackClicked()
                    }
                },
                onRefresh = { offers?.refresh() },
                error = error,
                noFound = noItem
            ){
                if (offers != null) {
                    val data by rememberUpdatedState(offers)
                    PagingGrid(
                        data = data,
                        content = { offer ->
                            PromoLotItem(offer) {

                            }
                        }
                    )
                }
            }
        }
    }
}
