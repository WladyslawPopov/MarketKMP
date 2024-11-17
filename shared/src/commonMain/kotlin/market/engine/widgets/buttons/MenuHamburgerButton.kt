package market.engine.widgets.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MenuHamburgerButton(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    fun openMenu() {
        scope.launch {
            if (drawerState.currentValue == DrawerValue.Closed) {
                drawerState.open()
            }else{
                drawerState.close()
            }
        }
    }

    IconButton(
        modifier = modifier,
        onClick = {
            openMenu()
        }
    ){
        AnimatedContent(
            targetState = drawerState.isAnimationRunning,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(200))
            }
        ) { _ ->
            if (drawerState.isOpen) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(strings.menuTitle),
                    modifier = modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
            } else {
                Icon(
                    painter = painterResource(drawables.menuHamburger),
                    contentDescription = stringResource(strings.menuTitle),
                    modifier = Modifier.size(dimens.smallIconSize),
                    tint = colors.black
                )
            }
        }
    }
}
