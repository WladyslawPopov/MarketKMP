package market.engine.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.ScrollBarsProvider
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
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
import market.engine.widgets.buttons.floatingCreateOfferButton
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

    val scrollState = rememberScrollState()
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val homeViewModel = model.homeViewModel

    val isLoading = homeViewModel.isShowProgress.collectAsState()
    val err = homeViewModel.errorMessage.collectAsState()

    val searchData = homeViewModel.listingData.searchData.subscribeAsState()
    val listingData = homeViewModel.listingData.data.subscribeAsState()

    val categories = homeViewModel.responseCategory.collectAsState()

    val promoOffer1 = homeViewModel.responseOffersPromotedOnMainPage1.collectAsState()
    val promoOffer2 = homeViewModel.responseOffersPromotedOnMainPage2.collectAsState()

    val errorContent: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
                { onError(err.value) { component.onRefresh() } }
            } else {
                null
            }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(drawerState, homeViewModel.viewModelScope, modifier) {
                component.goToLogin()
            }
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        BaseContent(
            topBar = {
                HomeAppBar(
                    modifier,
                    showNavigationRail,
                    drawerState
                )
            },
            isLoading = isLoading.value,
            onRefresh = { component.onRefresh() },

            floatingActionButton = {
                floatingCreateOfferButton {

                }
            },
            error = errorContent,
            noFound = null,
            toastItem = homeViewModel.toastItem,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier.fillMaxSize()
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

                    CategoryList(
                        categories = categories.value
                    ) { category ->
                        searchData.value.searchCategoryID = category.id
                        searchData.value.searchParentID = category.parentId
                        searchData.value.searchCategoryName = category.name
                        searchData.value.isRefreshing = true
                        component.navigateToListing()
                    }

                    val stringAllPromo = stringResource(strings.allPromoOffersBtn)
                    GridPromoOffers(
                        promoOffer1.value,
                        onOfferClick = {
                            component.goToOffer(it.id)
                        },
                        onAllClickButton = {
                            listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.value =
                                "promo_main_page"
                            listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                                stringAllPromo
                            searchData.value.isRefreshing = true
                            component.navigateToListing()
                        }
                    )

                    GridPopularCategory(listTopCategory) { topCategory ->
                        searchData.value.searchCategoryID = topCategory.id
                        searchData.value.searchParentID = topCategory.parentId
                        searchData.value.searchCategoryName = topCategory.name
                        searchData.value.searchParentName = topCategory.parentName
                        searchData.value.isRefreshing = true
                        component.navigateToListing()
                    }

                    GridPromoOffers(
                        promoOffer2.value,
                        onOfferClick = {
                            component.goToOffer(it.id)
                        },
                        onAllClickButton = {
                            if (listingData.value.filters.isNullOrEmpty()) {
                                listingData.value.filters = arrayListOf()
                                listingData.value.filters?.addAll(EmptyFilters.getEmpty())
                            }
                            listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.value =
                                "promo_main_page"
                            listingData.value.filters?.find { filter -> filter.key == "promo_main_page" }?.interpritation =
                                stringAllPromo
                            searchData.value.isRefreshing = true
                            component.navigateToListing()
                        }
                    )

                    FooterRow(listFooterItem)
                }


                ScrollBarsProvider().getVerticalScrollbar(
                    scrollState,
                    modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }
    }
}


