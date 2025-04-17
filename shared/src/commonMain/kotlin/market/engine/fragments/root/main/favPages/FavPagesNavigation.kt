package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.FavScreenType
import market.engine.core.network.networkObjects.FavoriteListItem
import market.engine.fragments.root.main.favPages.favorites.DefaultFavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesContent
import market.engine.fragments.root.main.favPages.subscriptions.DefaultSubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent


@Serializable
data class FavPagesConfig(
    @Serializable
    val favItem: FavoriteListItem
)

@Composable
fun FavPagesNavigation(
    component: FavPagesComponent,
    modifier: Modifier
) {
    val select = remember {
        mutableStateOf(0)
    }

    val model = component.model.subscribeAsState()
    val viewModel = model.value.viewModel
    val favTabList = viewModel.favoritesTabList.collectAsState()
    val showPages = remember { mutableStateOf(false) }

    LaunchedEffect(favTabList.value) {
        if (favTabList.value.isNotEmpty() && component.componentsPages == null){
            component.getPages()
            showPages.value = true
        }
    }

    Column {
        FavPagesAppBar(
            select.value,
            favTabList = favTabList.value,
            navigationClick = {
                select.value = it
                component.selectPage(select.value)
            },
            onRefresh = {
                component.onRefresh()
            }
        )
        if (showPages.value && component.componentsPages != null) {
            ChildPages(
                pages = component.componentsPages!!,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    select.value = it
                    component.selectPage(select.value)
                }
            ) { _, page ->
                when (page) {
                    is FavPagesComponents.SubscribedChild -> {
                        SubscriptionsContent(
                            page.component,
                            modifier
                        )
                    }

                    is FavPagesComponents.FavoritesChild -> {
                        FavoritesContent(
                            page.component,
                            modifier
                        )
                    }
                }
            }
        }
    }
}

fun itemFavorites(
    componentContext: ComponentContext,
    selectedType : FavScreenType,
    navigateToOffer: (id: Long) -> Unit,
): FavoritesComponent {
    return DefaultFavoritesComponent(
        componentContext = componentContext,
        goToOffer = { id ->
            navigateToOffer(id)
        },
        favType = selectedType
    )
}


fun itemSubscriptions(
    componentContext: ComponentContext,
    selectedType : FavScreenType,
    navigateToCreateNewSubscription : (Long?) -> Unit,
    navigateToListing : (ListingData) -> Unit
): SubscriptionsComponent {
    return DefaultSubscriptionsComponent(
        componentContext = componentContext,
        favType = selectedType,
        navigateToCreateNewSubscription = {
            navigateToCreateNewSubscription(it)
        },
        navigateToListing = {
            navigateToListing(it)
        }
    )
}

