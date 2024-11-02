package market.engine.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.items.TopCategory
import market.engine.core.types.WindowSizeClass
import market.engine.core.util.getWindowSizeClass
import market.engine.presentation.base.BaseContent
import market.engine.widgets.rows.CategoryList
import market.engine.widgets.rows.FooterRow
import market.engine.widgets.grids.GridPopularCategory
import market.engine.widgets.grids.GridPromoOffers
import market.engine.widgets.bars.SearchBar
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
    val scrollState = rememberScrollState()
    val searchData = component.globalData.listingData.searchData.subscribeAsState()
    val listingData = component.globalData.listingData.data.subscribeAsState()

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(model.isError.value) { component.onRefresh() } }
    } else {
        null
    }

    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun openMenu(){
        scope.launch {
            drawerState.open()
        }
    }

    BaseContent(
        modifier = modifier,
        isLoading = isLoading,
        isShowFloatingButton = true,
        showVerticalScrollbarState = scrollState,
        drawerMethod = { component.goToLogin() },
        drawerState = drawerState,
        scope = scope,
        topBar = { HomeAppBar(modifier, showNavigationRail) { openMenu() } },
        onRefresh = { component.onRefresh() },
        error = error
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = !isLoading.value,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = modifier.verticalScroll(scrollState)
            ) {
                SearchBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .wrapContentHeight()
                        .wrapContentWidth(),
                    onSearchClick = {
                        component.navigateToSearch()
                    }
                )

                model.categories.collectAsState().value.let { categoryList ->
                    CategoryList(
                        categories = categoryList
                    ) { category ->
                        searchData.value.clear()
                        searchData.value.clearCategory()
                        searchData.value.searchCategoryID = category.id
                        searchData.value.searchParentID = category.parentId
                        searchData.value.searchCategoryName = category.name

                        component.navigateToListing()
                    }
                }
                val stringAllPromo = stringResource(strings.allPromoOffersBtn)
                model.promoOffer1.collectAsState().value.let { offers ->
                    GridPromoOffers(offers, onOfferClick = {}, onAllClickButton = {
                        listingData.value.filters = EmptyFilters.getEmpty()
                        listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.value =
                            "promo_main_page"
                        listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                            stringAllPromo
                        searchData.value.clear()
                        searchData.value.clearCategory()
                        component.navigateToListing()
                    })
                }

                GridPopularCategory(listTopCategory) { topCategory ->
                    searchData.value.clear()
                    searchData.value.clearCategory()
                    searchData.value.searchCategoryID = topCategory.id
                    searchData.value.searchParentID = topCategory.parentId
                    searchData.value.searchCategoryName = topCategory.name
                    searchData.value.searchParentName = topCategory.parentName
                    component.navigateToListing()
                }

                model.promoOffer2.collectAsState().value.let { offers ->
                    GridPromoOffers(offers, onOfferClick = {}, onAllClickButton = {
                        listingData.value.filters = EmptyFilters.getEmpty()
                        listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.value =
                            "promo_main_page"
                        listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                            stringAllPromo
                        searchData.value.clear()
                        searchData.value.clearCategory()
                        component.navigateToListing()
                    })
                }
                FooterRow(listFooterItem)
            }
        }
    }
}





