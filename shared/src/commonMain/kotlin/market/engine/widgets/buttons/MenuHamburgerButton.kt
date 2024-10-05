package market.engine.widgets.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MenuHamburgerButton(
    modifier: Modifier = Modifier,
    openMenu : () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = {
            openMenu()
        }
    ){
        Icon(
            painter = painterResource(drawables.menuHamburger),
            contentDescription = stringResource(strings.menuTitle),
            modifier = modifier.size(dimens.smallIconSize),
            tint = colors.black
        )
    }
}
