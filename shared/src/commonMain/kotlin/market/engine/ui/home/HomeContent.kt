package market.engine.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import application.market.auction_mobile.ui.home.HomeComponent
import market.engine.theme.ThemeResources
import market.engine.widgets.CategoryRow
import market.engine.widgets.SearchBar

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier,
    themeResources: ThemeResources,
) {
    Box(modifier = modifier.fillMaxSize()) {

        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            themeResources,
            onSearchClick = {

            }
        )

        Column(
            modifier = Modifier
                .padding(top = 72.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val categories = listOf("Category 1", "Category 2", "Category 3", "Category 4", "Category 5")
            CategoryRow(categories = categories, modifier = modifier, themeResources = themeResources)
        }
    }
}



