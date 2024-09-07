package market.engine.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.business.constants.ThemeResources.colors
import market.engine.widgets.CategoryList
import market.engine.widgets.GridPromoOffers
import market.engine.widgets.SearchBar

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {

    val modelState = component.model.subscribeAsState()
    val model = modelState.value

    val isLoading = model.isLoading.collectAsState()

    Box(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colors.inactiveBottomNavIconColor
            )
        } else {
            SearchBar(
                modifier = Modifier.align(Alignment.TopCenter),
                onSearchClick = {
                    component.onItemClicked(id = 1L)
                }
            )

            Column(
                modifier = Modifier
                    .padding(top = 72.dp) // padding for SearchBar
            ) {
                model.categories.collectAsState().value.map { it }.let { categoryNames ->
                    CategoryList(
                        categories = categoryNames
                    )
                    { component.onItemClicked(id = 1L) }
                }

                model.promoOffer1.collectAsState().value.map{ it }.let { offers ->
                    GridPromoOffers(offers){

                    }
                }

                model.promoOffer2.collectAsState().value.map{ it }.let { offers ->
                    GridPromoOffers(offers) {

                    }
                }
            }
        }
    }
}



