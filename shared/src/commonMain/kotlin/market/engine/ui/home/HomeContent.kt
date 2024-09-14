package market.engine.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.business.globalObjects.listingData
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.globalObjects.searchData
import market.engine.business.items.TopCategory
import market.engine.widgets.pages.BaseContent
import market.engine.widgets.rows.CategoryList
import market.engine.widgets.rows.FooterRow
import market.engine.widgets.grids.GridPopularCategory
import market.engine.widgets.grids.GridPromoOffers
import market.engine.widgets.common.SearchBar
import market.engine.widgets.exceptions.onError
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val listTopCategory = listOf(
        TopCategory(
            id = 48393,
            parentId = 48341,
            name = stringResource(strings.categoryCoin),
            parentName = stringResource(strings.categoryCollection),
            icon = drawables.coinPng
        ),
        TopCategory(
            id = 48522,
            parentId = 48341,
            name = stringResource(strings.categoryBanknotes),
            parentName = stringResource(strings.categoryCollection),
            icon = drawables.banknotePng
        ),
        TopCategory(
            id = 48343,
            parentId = 48341,
            name = stringResource(strings.categoryMarks),
            parentName = stringResource(strings.categoryCollection),
            icon = drawables.markPng
        ),
        TopCategory(
            id = 100247,
            parentId = 48341,
            name = stringResource(strings.categoryMedals),
            parentName = stringResource(strings.categoryCollection),
            icon = drawables.medalPng
        ),
        TopCategory(
            id = 100236,
            parentId = 48260,
            name = stringResource(strings.categoryPorcelain),
            parentName = stringResource(strings.categoryArt),
            icon = drawables.porcelainPng
        ),
        TopCategory(
            id = 108682,
            parentId = 1,
            name = stringResource(strings.categoryBooks),
            parentName = stringResource(strings.categoryMain),
            icon = drawables.booksPng
        ),
        TopCategory(
            id = 64823,
            parentId = 64821,
            name = stringResource(strings.categoryPhone),
            parentName = stringResource(strings.categoryPhone),
            icon = drawables.phonesPng
        ),
        TopCategory(
            id = 48302,
            parentId = 11,
            name = stringResource(strings.categoryNotebooks),
            parentName = stringResource(strings.categoryElectronic),
            icon = drawables.notebookPng
        ),
        TopCategory(
            id = 124975,
            parentId = 11,
            name = stringResource(strings.categoryAppliances),
            parentName = stringResource(strings.categoryElectronic),
            icon = drawables.appliancesPng
        )
    )
    val listFooterItem = listOf(
        TopCategory(
            id = 1,
            name = stringResource(strings.homeFixAuction),
            icon = drawables.auctionFixIcon
        ),
        TopCategory(
            id = 2,
            name = stringResource(strings.homeManyOffers),
            icon = drawables.manyOffersIcon
        ),
        TopCategory(
            id = 3,
            name = stringResource(strings.verifySellers),
            icon = drawables.verifySellersIcon
        ),
        TopCategory(
            id = 4,
            name = stringResource(strings.everyDeyDiscount),
            icon = drawables.discountBigIcon
        ),
        TopCategory(
            id = 5,
            name = stringResource(strings.freeBilling),
            icon = drawables.freeBillingIcon
        ),
    )
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val isLoading = model.isLoading.collectAsState()
    val err = model.isError.collectAsState()

    val error : (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(model.isError.value) { component.onRefresh() } }
    }else{
        null
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        onRefresh = { component.onRefresh() },
        error = error
    ){
        Column {
            SearchBar(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .wrapContentHeight()
                    .wrapContentWidth(),
                onSearchClick = {
                    component.onSearchClicked(id = 1L)
                }
            )

            model.categories.collectAsState().value.let { categoryList ->
                CategoryList(
                    categories = categoryList
                ){ category ->
                    listingData.methodServer = "get_public_listing"
                    searchData.searchCategoryID = category.id
                    searchData.searchParentID = category.parentId
                    searchData.searchCategoryName = category.name

                    component.goToListing()
                }
            }

            model.promoOffer1.collectAsState().value.let { offers ->
                GridPromoOffers(offers) {

                }
            }

            GridPopularCategory(listTopCategory) { topCategory ->
                listingData.methodServer = "get_public_listing"
                searchData.searchCategoryID = topCategory.id
                searchData.searchParentID = topCategory.parentId
                searchData.searchCategoryName = topCategory.name
                searchData.searchParentName = topCategory.parentName

                component.goToListing()
            }

            model.promoOffer2.collectAsState().value.let { offers ->
                GridPromoOffers(offers) {

                }
            }
            FooterRow(listFooterItem)
        }
    }
}




