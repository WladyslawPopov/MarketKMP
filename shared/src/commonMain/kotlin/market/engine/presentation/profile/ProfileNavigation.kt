package market.engine.presentation.profile

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
import market.engine.core.navigation.children.ChildProfile
import market.engine.presentation.profileMyOffers.ProfileMyOffersNavigation

@Composable
fun ProfileNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, ChildProfile>>
) {
    val stack by childStack.subscribeAsState()
    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is ChildProfile.ProfileChild -> ProfileContent(screen.component, modifier)
            is ChildProfile.MyOffersChild -> ProfileMyOffersNavigation(screen.component, modifier)
        }
    }
}
