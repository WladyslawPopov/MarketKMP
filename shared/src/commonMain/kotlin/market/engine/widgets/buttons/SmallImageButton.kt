package market.engine.widgets.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SmallImageButton(
    icon : DrawableResource,
    contentDescription : String = "",
    modifierIconSize: Modifier = Modifier.size(dimens.smallIconSize),
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = modifierIconSize,
        )
    }
}
