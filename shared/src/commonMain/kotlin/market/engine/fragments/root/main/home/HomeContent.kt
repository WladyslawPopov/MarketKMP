package market.engine.fragments.root.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.listTopCategory
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.rows.FooterRow
import market.engine.widgets.grids.GridPopularCategory
import market.engine.widgets.grids.GridPromoOffers
import market.engine.widgets.bars.SearchBar
import market.engine.widgets.buttons.floatingCreateOfferButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.listing.rememberLazyScrollState
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.appBars.DrawerAppBar
import market.engine.widgets.items.CategoryItem
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.rows.LazyRowWithScrollBars
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
    val viewModel = model.homeViewModel

    val uiState = viewModel.uiState.collectAsState()
    val state = uiState.value

    val isLoading = viewModel.isShowProgress.collectAsState()
    val error = viewModel.errorMessage.collectAsState()
    val toastItem = viewModel.toastItem.collectAsState()

    val listingState = rememberLazyScrollState(viewModel)

    val listTopCategory = remember(listTopCategory) { listTopCategory }

    val errorContent: (@Composable () -> Unit)? = remember(error.value.humanMessage) {
        if (error.value.humanMessage.isNotBlank()) {
            { OnError(error.value) { viewModel.updateModel() } }
        } else {
            null
        }
    }

    BackHandler(model.backHandler) {}

    Scaffold {
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
            gesturesEnabled = true,
        )
        {
            EdgeToEdgeScaffold(
                topBar = {
                    DrawerAppBar(
                        data = state.appBarData,
                        drawerState = drawerState,
                        color = colors.white.copy(alphaBars)
                    ) {
                        Image(
                            painter = painterResource(drawables.logo),
                            contentDescription = stringResource(strings.homeTitle),
                            modifier = Modifier.size(140.dp, 68.dp),
                        )
                    }

                    SearchBar {
                        component.goToNewSearch()
                    }
                },
                isLoading = isLoading.value,
                onRefresh = { viewModel.updateModel() },
                floatingActionButton = {
                    floatingCreateOfferButton {
                        component.goToCreateOffer()
                    }
                },
                error = errorContent,
                noFound = null,
                toastItem = toastItem.value,
                modifier = Modifier.fillMaxSize()
            ) { contentPadding ->
                LazyColumnWithScrollBars(
                    state = listingState.scrollState,
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                )
                {
                    item {
                        LazyRowWithScrollBars(
                            heightMod = Modifier.fillMaxWidth()
                        ) {
                            items(state.categories) { category ->
                                CategoryItem(category = category) {
                                    viewModel.goToCategory(category)
                                }
                            }
                        }
                    }

                    item {
                        GridPromoOffers(
                            state.promoOffers1,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                viewModel.goToAllPromo()
                            }
                        )
                    }
                    item {
                        GridPopularCategory(listTopCategory) { topCategory ->
                            viewModel.goToCategory(topCategory)
                        }
                    }
                    item {
                        GridPromoOffers(
                            state.promoOffers2,
                            onOfferClick = {
                                component.goToOffer(it)
                            },
                            onAllClickButton = {
                                viewModel.goToAllPromo()
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
