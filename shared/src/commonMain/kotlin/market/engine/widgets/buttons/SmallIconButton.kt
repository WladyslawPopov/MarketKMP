package market.engine.widgets.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SmallIconButton(
    icon : DrawableResource,
    color : Color,
    contentDescription : String = "",
    modifierIconSize: Modifier = Modifier.size(dimens.smallIconSize),
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = modifierIconSize,
            tint = if (enabled) color else colors.rippleColor
        )
    }
}
