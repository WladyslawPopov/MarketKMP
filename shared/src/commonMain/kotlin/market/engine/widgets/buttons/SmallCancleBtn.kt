package market.engine.widgets.buttons

import androidx.compose.foundation.layout.padding
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
fun SmallCancelBtn(
    modifier: Modifier = Modifier,
    onCancelClick: (String) -> Unit
) {
    IconButton(
        modifier = modifier.padding(dimens.smallPadding),
        onClick = {
            onCancelClick("")
        }
    ) {
        Icon(
            painterResource(drawables.cancelIcon),
            contentDescription = stringResource(strings.actionClose),
            modifier = modifier.size(dimens.extraSmallIconSize),
            tint = colors.steelBlue
        )
    }
}
