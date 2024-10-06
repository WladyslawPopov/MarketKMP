package market.engine.widgets.buttons

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import market.engine.core.constants.ThemeResources.dimens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SmallIconButton(
    icon : DrawableResource,
    color : Color,
    contentDescription : String = "",
    modifierIconSize: Modifier = Modifier,
    modifier: Modifier = Modifier.padding(horizontal = dimens.smallPadding),
    onClick: () -> Unit,
) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier.size(dimens.smallIconSize),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = modifierIconSize,
            tint = color
        )
    }
}
