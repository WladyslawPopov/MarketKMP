package market.engine.fragments.root.main.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.listTopCategory
import market.engine.core.data.items.TopCategory
import market.engine.fragments.base.BaseContent
import market.engine.widgets.rows.CategoryList
import market.engine.widgets.rows.FooterRow
import market.engine.widgets.grids.GridPopularCategory
import market.engine.widgets.grids.GridPromoOffers
import market.engine.widgets.bars.SearchBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
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

    val defCat = stringResource(strings.categoryMain)

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
                { onError(err) { component.onRefresh() } }
            } else {
                null
            }

    BackHandler(model.backHandler){}

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = homeViewModel.scrollItem.value,
        initialFirstVisibleItemScrollOffset = homeViewModel.offsetScrollItem.value
    )

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            homeViewModel.scrollItem.value = index
            homeViewModel.offsetScrollItem.value = offset
        }
    }

    BaseContent(
        topBar = {
            HomeAppBar(
                drawerState = drawerState,
                viewModel = homeViewModel,
                goToMessenger = {
                    component.goToMessenger()
                },
                goToMyProposals = {
                    component.goToMyProposals()
                },
                goToNotifications = {
                    component.goToNotificationHistory()
                },
                onRefresh = {
                    component.onRefresh()
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
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    drawerState,
                    goToContactUs = {
                        component.goToContactUs()
                    },
                    goToLogin = {
                        component.goToLogin()
                    },
                    goToSettings = {
                        component.goToAppSettings()
                    }
                )
            },
            gesturesEnabled = drawerState.isOpen,
        ) {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar {
                    component.goToNewSearch()
                }

                LazyColumnWithScrollBars(
                    state = scrollState,
                ) {
                    item {
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
                    }
                    item {
                        GridPromoOffers(
                            promoOffer1.value,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                component.goToAllPromo()
                            }
                        )
                    }
                    item {
                        GridPopularCategory(listTopCategory) { topCategory ->
                            component.goToCategory(topCategory)
                        }
                    }
                    item {
                        GridPromoOffers(
                            promoOffer2.value,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                component.goToAllPromo()
                            }
                        )
                    }
                    item {
                        FooterRow(listFooterItem)
                    }
                }
            }
        }
    }
}
