package market.engine.widgets.buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.stringResource

@Composable
fun FilterButton(
    text: String,
    color: ButtonColors,
    onClick: () -> Unit,
    onCancelClick: (() -> Unit)? = null,
) {
    TextButton(
        onClick = { onClick() },
        colors = color,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.8f).padding(dimens.extraSmallPadding)
            )

            if(onCancelClick != null) {
                SmallIconButton(
                    icon = drawables.cancelIcon,
                    contentDescription = stringResource(strings.actionClose),
                    color = colors.steelBlue,
                    modifier = Modifier.size(dimens.extraSmallIconSize),
                    modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                ) {
                    onCancelClick()
                }
            }
        }
    }
}
