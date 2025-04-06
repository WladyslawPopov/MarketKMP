package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.FavScreenType
import market.engine.fragments.root.main.favPages.favorites.DefaultFavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesComponent
import market.engine.fragments.root.main.favPages.favorites.FavoritesContent
import market.engine.fragments.root.main.favPages.subscriptions.DefaultSubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsComponent
import market.engine.fragments.root.main.favPages.subscriptions.SubscriptionsContent


@Serializable
data class FavPagesConfig(
    @Serializable
    val favType: FavScreenType
)

@Composable
fun FavPagesNavigation(
    component: FavPagesComponent,
    modifier: Modifier
) {
    val select = remember {
        mutableStateOf(component.favScreenType)
    }

    Column {
        FavPagesAppBar(
            select.value,
            navigationClick = {
                select.value = it
                component.selectPage(select.value)
            },
            onRefresh = {
                component.onRefresh()
            }
        )

        ChildPages(
            pages = component.componentsPages,
            scrollAnimation = PagesScrollAnimation.Default,
            onPageSelected = {
                select.value = when(it){
                    0 -> FavScreenType.FAVORITES
                    1 -> FavScreenType.SUBSCRIBED
                    else -> {
                        FavScreenType.FAVORITES
                    }
                }
                component.selectPage(select.value)
            }
        ) { _, page ->
            when(page){
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

fun itemFavorites(
    componentContext: ComponentContext,
    selectedType : FavScreenType,
    selectedFavScreen : (FavScreenType) -> Unit,
    navigateToOffer: (id: Long) -> Unit,
): FavoritesComponent {
    return DefaultFavoritesComponent(
        componentContext = componentContext,
        goToOffer = { id ->
            navigateToOffer(id)
        },
        selectedFavScreen = {
            selectedFavScreen(it)
        },
        favType = selectedType
    )
}

fun itemSubscriptions(
    componentContext: ComponentContext,
    selectedType : FavScreenType,
    selectedFavScreen : (FavScreenType) -> Unit,
    navigateToCreateNewSubscription : (Long?) -> Unit,
    navigateToListing : (ListingData) -> Unit
): SubscriptionsComponent {
    return DefaultSubscriptionsComponent(
        componentContext = componentContext,
        selectedFavScreen = {
            selectedFavScreen(it)
        },
        favType = selectedType,
        navigateToCreateNewSubscription = {
            navigateToCreateNewSubscription(it)
        },
        navigateToListing = {
            navigateToListing(it)
        }
    )
}

