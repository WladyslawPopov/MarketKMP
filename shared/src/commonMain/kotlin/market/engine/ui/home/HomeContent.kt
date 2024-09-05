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
import market.engine.widgets.CategoryRow
import market.engine.widgets.SearchBar

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val modelState = component.model.subscribeAsState()
    val model = modelState.value

    // Подписываемся на isShowProgress для отображения индикатора загрузки
    val isLoading = model.isLoading.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {

        // SearchBar — всегда отображаем наверху
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            onSearchClick = {
                // Логика поиска
            }
        )

        // Если идёт загрузка, отображаем CircularProgressIndicator
        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colors.inactiveBottomNavIconColor
            )
        } else {
            // После загрузки данных отображаем категории
            Column(
                modifier = Modifier
                    .padding(top = 72.dp) // Отступ для SearchBar
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Отображаем список категорий
                model.categories.collectAsState().value.map { it.name }.let { categoryNames ->
                    CategoryRow(
                        categories = categoryNames, // Преобразуем в список имён
                        modifier = modifier
                    )
                }

                // Дополнительный контент, если есть данные по промо-оферам
                if (model.promoOffer1.collectAsState().value.isNotEmpty()) {
                    // Отображаем PromoOffersSection1
                }

                if (model.promoOffer2.collectAsState().value.isNotEmpty()) {
                    // Отображаем PromoOffersSection2
                }
            }
        }
    }
}



