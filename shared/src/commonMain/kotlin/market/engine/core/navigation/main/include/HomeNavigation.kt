package market.engine.core.navigation.main.include

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import market.engine.core.navigation.children.ChildHome
import market.engine.core.navigation.children.ChildSearch
import market.engine.presentation.home.HomeContent
import market.engine.presentation.listing.ListingContent
import market.engine.presentation.offer.OfferContent

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
        }
    }
}
