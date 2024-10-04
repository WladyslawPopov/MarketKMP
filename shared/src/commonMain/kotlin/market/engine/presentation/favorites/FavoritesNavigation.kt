package market.engine.presentation.favorites

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
import market.engine.presentation.main.MainComponent

@Composable
fun FavoritesNavigation(
    modifier: Modifier = Modifier,
    childStack: Value<ChildStack<*, MainComponent.ChildFavorites>>
) {
    val stack by childStack.subscribeAsState()
    Children(
        stack = stack,
        modifier = modifier
            .fillMaxSize(),
        animation = stackAnimation(fade())
    ) { child ->
        when (val screen = child.instance) {
            is MainComponent.ChildFavorites.FavoritesChild -> FavoritesContent(screen.component)
        }
    }
}

