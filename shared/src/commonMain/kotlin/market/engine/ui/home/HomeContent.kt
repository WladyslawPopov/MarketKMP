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
import auctionkmp.shared.generated.resources.Res
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.items.TopCategory
import market.engine.widgets.CategoryList
import market.engine.widgets.FooterRow
import market.engine.widgets.GridPopularCategory
import market.engine.widgets.GridPromoOffers
import market.engine.widgets.SearchBar

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {

    val listTopCategory = listOf(
        TopCategory(
            id = 48393,
            parentId = 48341,
            name = strings.categoryCoin,
            parentName = strings.categoryCollection,
            icon = drawables.coinPng
        ),
        TopCategory(
            id = 48522,
            parentId = 48341,
            name = strings.categoryBanknotes,
            parentName = strings.categoryCollection,
            icon = drawables.banknotePng
        ),
        TopCategory(
            id = 48343,
            parentId = 48341,
            name = strings.categoryMarks,
            parentName = strings.categoryCollection,
            icon = drawables.markPng
        ),
        TopCategory(
            id = 100247,
            parentId = 48341,
            name = strings.categoryMedals,
            parentName = strings.categoryCollection,
            icon = drawables.medalPng
        ),
        TopCategory(
            id = 100236,
            parentId = 48260,
            name = strings.categoryPorcelain,
            parentName = strings.categoryArt,
            icon = drawables.porcelainPng
        ),
        TopCategory(
            id = 108682,
            parentId = 1,
            name = strings.categoryBooks,
            parentName = strings.categoryMain,
            icon = drawables.booksPng
        ),
        TopCategory(
            id = 64823,
            parentId = 64821,
            name = strings.categoryPhone,
            parentName = strings.categoryPhone,
            icon = drawables.phonesPng
        ),
        TopCategory(
            id = 48302,
            parentId = 11,
            name = strings.categoryNotebooks,
            parentName = strings.categoryElectronic,
            icon = drawables.notebookPng
        ),
        TopCategory(
            id = 124975,
            parentId = 11,
            name = strings.categoryAppliances,
            parentName = strings.categoryElectronic,
            icon = drawables.appliancesPng
        )
    )

    val listFooterItem = listOf(
        TopCategory(
            id = 1,
            name = strings.homeFixAuction,
            icon = drawables.auctionFixIcon
        ),
        TopCategory(
            id = 2,
            name = strings.homeManyOffers,
            icon = drawables.manyOffersIcon
        ),
        TopCategory(
            id = 3,
            name = strings.verifySellers,
            icon = drawables.verifySellersIcon
        ),
        TopCategory(
            id = 4,
            name = strings.everyDeyDiscount,
            icon = drawables.discountBigIcon
        ),
        TopCategory(
            id = 5,
            name = strings.freeBilling,
            icon = drawables.freeBillingIcon
        ),
    )

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
            Column{
                SearchBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onSearchClick = {
                        component.onItemClicked(id = 1L)
                    }
                )

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

                GridPopularCategory(listTopCategory){ topCategory ->
                    component.onItemClicked(id = topCategory.id)
                }

                model.promoOffer2.collectAsState().value.map{ it }.let { offers ->
                    GridPromoOffers(offers) {

                    }
                }

                FooterRow(listFooterItem)
            }
        }
    }
}



