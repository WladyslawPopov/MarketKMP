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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.listTopCategory
import market.engine.fragments.base.BaseContent
import market.engine.widgets.rows.CategoryList
import market.engine.widgets.rows.FooterRow
import market.engine.widgets.grids.GridPopularCategory
import market.engine.widgets.grids.GridPromoOffers
import market.engine.widgets.bars.SearchBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.widgets.bars.DrawerAppBar
import market.engine.widgets.rows.LazyColumnWithScrollBars

@Composable
fun HomeContent(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val homeViewModel = model.homeViewModel

    val uiState = homeViewModel.uiState.collectAsState()
    val state = uiState.value

    val isLoading = remember(state.isLoading) { state.isLoading }
    val error = remember(state.error) { state.error }

    val listTopCategory = remember(listTopCategory) { listTopCategory }

    val errorContent: (@Composable () -> Unit)? = remember(error.humanMessage) {
        if (error.humanMessage.isNotBlank()) {
            { onError(error) { homeViewModel.updateModel() } }
        } else {
            null
        }
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
            if (state.appBarData != null) {
                DrawerAppBar(
                    state.appBarData,
                    drawerState = drawerState
                )
            }
        },
        isLoading = isLoading,
        onRefresh = { homeViewModel.updateModel() },
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
                    drawerState = drawerState,
                    goToLogin = {
                        component.goToLogin()
                    },
                    list = state.drawerList
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
                            categories = state.categories
                        ) { category ->
                            state.events?.goToCategory(category)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers1,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                state.events?.goToAllPromo()
                            }
                        )
                    }
                    item {
                        GridPopularCategory(listTopCategory) { topCategory ->
                            state.events?.goToCategory(topCategory)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers2,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                state.events?.goToAllPromo()
                            }
                        )
                    }
                    item {
                        FooterRow(state.listFooter)
                    }
                }
            }
        }
    }
}
