package market.engine.core.navigation.main.children

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import market.engine.presentation.favorites.FavoritesComponent
import market.engine.presentation.favorites.FavoritesContent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.listing.ListingContent
import market.engine.presentation.offer.OfferComponent
import market.engine.presentation.offer.OfferContent
import market.engine.presentation.subscriptions.SubscribesComponent
import market.engine.presentation.subscriptions.SubscribesContent

sealed class ChildFavorites {
    class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    class SubChild(val component: SubscribesComponent) : ChildFavorites()
    class OfferChild(val component: OfferComponent) : ChildFavorites()
    class ListingChild(val component: ListingComponent) : ChildFavorites()
}

@Composable
fun FavoritesNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildFavorites>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildFavorites.FavoritesChild -> FavoritesContent(modifier, screen.component)
            is ChildFavorites.SubChild -> SubscribesContent(modifier, screen.component)
            is ChildFavorites.OfferChild -> OfferContent(screen.component, modifier)
            is ChildFavorites.ListingChild -> ListingContent(screen.component, modifier)
        }
    }
}

