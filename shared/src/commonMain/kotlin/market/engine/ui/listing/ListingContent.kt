package market.engine.ui.listing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.collectAsLazyPagingItems
import application.market.auction_mobile.business.networkObjects.Offer
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.SwipeRefreshContent
import market.engine.widgets.PagingGrid
import market.engine.widgets.PromoLotItem

@Composable
fun ListingContent(
    component: ListingComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val offers = model.listing.collectAsLazyPagingItems()
    val result by rememberUpdatedState(offers)

    SwipeRefreshContent(
        isRefreshing = result.loadState.refresh is LoadStateLoading,
        onRefresh = {
            component.onRefresh()
        }
    ) {
        Box(modifier = Modifier.fillMaxSize())
        {
            PagingGrid(
                data = result,
                content = { offer ->
                    PromoLotItem(offer){

                    }
                }
            )
        }
    }
}
