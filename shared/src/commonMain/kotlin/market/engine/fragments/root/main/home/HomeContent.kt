package market.engine.fragments.root.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.listTopCategory
import market.engine.core.data.states.ScrollDataState
import market.engine.fragments.base.BaseContent
import market.engine.widgets.rows.CategoryList
import market.engine.widgets.rows.FooterRow
import market.engine.widgets.grids.GridPopularCategory
import market.engine.widgets.grids.GridPromoOffers
import market.engine.widgets.bars.SearchBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.OnError
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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

    val isLoading = homeViewModel.isShowProgress.collectAsState()
    val error = homeViewModel.errorMessage.collectAsState()

    val listTopCategory = remember(listTopCategory) { listTopCategory }

    val errorContent: (@Composable () -> Unit)? = remember(error.value.humanMessage) {
        if (error.value.humanMessage.isNotBlank()) {
            { OnError(error.value) { homeViewModel.updateModel() } }
        } else {
            null
        }
    }

    BackHandler(model.backHandler){}

    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = homeViewModel.scrollState.value.scrollItem,
        initialFirstVisibleItemScrollOffset = homeViewModel.scrollState.value.scrollItem
    )

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            homeViewModel.scrollState.value = ScrollDataState(index, offset)
        }
    }

    BaseContent(
        topBar = {
            DrawerAppBar(
                data = state.appBarData,
                drawerState = drawerState
            ){
                Image(
                    painter = painterResource(drawables.logo),
                    contentDescription = stringResource(strings.homeTitle),
                    modifier = Modifier.size(140.dp, 68.dp),
                )
            }
        },
        isLoading = isLoading.value,
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
                            homeViewModel.goToCategory(category)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers1,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                homeViewModel.goToAllPromo()
                            }
                        )
                    }
                    item {
                        GridPopularCategory(listTopCategory) { topCategory ->
                            homeViewModel.goToCategory(topCategory)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers2,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                homeViewModel.goToAllPromo()
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
