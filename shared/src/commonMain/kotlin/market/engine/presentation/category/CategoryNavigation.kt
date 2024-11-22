package market.engine.presentation.category

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
import market.engine.core.navigation.children.ChildCategory
import market.engine.presentation.listing.ListingContent
import market.engine.presentation.offer.OfferContent
import market.engine.presentation.search.SearchContent


@Composable
fun CategoryNavigation (
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildCategory>>
) {
    val stack by childStack.subscribeAsState()
    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildCategory.SearchChild ->{
                SearchContent(screen.component, modifier)
            }
            is ChildCategory.ListingChild ->{
                ListingContent(screen.component, modifier)
            }
            is ChildCategory.CategoryChild ->{
                CategoryContent(screen.component, modifier)
            }

            is ChildCategory.OfferChild -> {
                OfferContent(screen.component, modifier)
            }
        }
    }
}

