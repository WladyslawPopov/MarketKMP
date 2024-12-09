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
import market.engine.presentation.home.HomeComponent
import market.engine.presentation.home.HomeContent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.listing.ListingContent
import market.engine.presentation.offer.OfferComponent
import market.engine.presentation.offer.OfferContent
import market.engine.presentation.user.UserComponent
import market.engine.presentation.user.UserContent

sealed class ChildHome {
    class HomeChild(val component: HomeComponent) : ChildHome()
    class OfferChild(val component: OfferComponent) : ChildHome()
    class ListingChild(val component: ListingComponent) : ChildHome()
    class UserChild(val component: UserComponent) : ChildHome()
}

@Composable
fun HomeNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildHome>>
) {
    val stack by childStack.subscribeAsState()

    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildHome.HomeChild ->{
                HomeContent(screen.component, modifier)
            }
            is ChildHome.OfferChild ->{
                OfferContent(screen.component, modifier)
            }
            is ChildHome.ListingChild ->{
                ListingContent(screen.component, modifier)
            }
            is ChildHome.UserChild ->{
                UserContent(screen.component, modifier)
            }
        }
    }
}
