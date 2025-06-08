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
    val defCat = stringResource(strings.categoryMain)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val modelState = component.model.subscribeAsState()
    val model = modelState.value
    val homeViewModel = model.homeViewModel

    val uiState = homeViewModel.uiState.collectAsState()
    val state = uiState.value

    val listTopCategory = remember { listTopCategory }
    val events = remember { model.events }

    val errorContent: (@Composable () -> Unit)? = if (state.error.humanMessage.isNotBlank()) {
        { onError(state.error) { events.onRefresh() } }
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
                listItems = homeViewModel.listAppBar.value
            )
        },
        isLoading = state.isLoading,
        onRefresh = { events.onRefresh() },
        floatingActionButton = {
            floatingCreateOfferButton {
                events.goToCreateOffer()
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
                        events.goToLogin()
                    },
                    list = homeViewModel.drawerList.value
                )
            },
            gesturesEnabled = drawerState.isOpen,
        ) {
            Column(
                modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar {
                    events.goToNewSearch()
                }

                LazyColumnWithScrollBars(
                    state = scrollState,
                ) {
                    item {
                        CategoryList(
                            categories = state.categories
                        ) { category ->
                            val cat = TopCategory(
                                id = category.id,
                                parentId = category.parentId,
                                name = category.name ?: defCat,
                                parentName = null,
                                icon = drawables.infoIcon
                            )
                            events.goToCategory(cat)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers1,
                            onOfferClick = {
                                events.goToOffer(it)
                            },
                            onAllClickButton = {
                                events.goToAllPromo()
                            }
                        )
                    }
                    item {
                        GridPopularCategory(listTopCategory) { topCategory ->
                            events.goToCategory(topCategory)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers2,
                            onOfferClick = {
                                events.goToOffer(it)
                            },
                            onAllClickButton = {
                                events.goToAllPromo()
                            }
                        )
                    }
                    item {
                        FooterRow(homeViewModel.listFooter)
                    }
                }
            }
        }
    }
}
