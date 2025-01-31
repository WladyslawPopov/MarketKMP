package market.engine.fragments.root.main.home

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
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.TopCategory
import market.engine.fragments.base.BaseContent
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val homeViewModel = model.homeViewModel
    val isLoading = homeViewModel.isShowProgress.collectAsState()

    val err = homeViewModel.errorMessage.collectAsState()

    val categories = homeViewModel.responseCategory.collectAsState()
    val promoOffer1 = homeViewModel.responseOffersPromotedOnMainPage1.collectAsState()
    val promoOffer2 = homeViewModel.responseOffersPromotedOnMainPage2.collectAsState()

    val errorContent: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
                { onError(err.value) { component.onRefresh() } }
            } else {
                null
            }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState,
                modifier,
                goToContactUs = {
                    component.goToContactUs()
                },
                goToLogin = {
                    component.goToLogin()
                }
            )
        },
        gesturesEnabled = drawerState.isOpen,
    ) {
        BaseContent(
            topBar = {
                HomeAppBar(
                    modifier,
                    drawerState,
                    goToMessenger = {
                        component.goToMessenger()
                    }
                )
            },
            isLoading = isLoading.value,
            onRefresh = { component.onRefresh() },
            floatingActionButton = {
                floatingCreateOfferButton {
                    component.goToCreateOffer()
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
                            component.goToNewSearch()
                        }
                    )

                    val defCat = stringResource(strings.categoryMain)
                    CategoryList(
                        categories = categories.value
                    ) { category ->
                        val cat = TopCategory(
                            id = category.id,
                            parentId = category.parentId,
                            name = category.name ?: defCat,
                            parentName = null,
                            icon = drawables.infoIcon
                        )
                        component.goToCategory(cat)
                    }

                    GridPromoOffers(
                        promoOffer1.value,
                        onOfferClick = {
                            component.goToOffer(it.id)
                        },
                        onAllClickButton = {
                          component.goToAllPromo()
                        }
                    )

                    GridPopularCategory(listTopCategory) { topCategory ->
                        component.goToCategory(topCategory)
                    }

                    GridPromoOffers(
                        promoOffer2.value,
                        onOfferClick = {
                            component.goToOffer(it.id)
                        },
                        onAllClickButton = {
                            component.goToAllPromo()
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


