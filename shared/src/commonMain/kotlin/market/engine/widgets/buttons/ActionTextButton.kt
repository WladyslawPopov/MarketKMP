package market.engine.widgets.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionTextButton(
    text: StringResource,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    alignment: Alignment,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        TextButton(
            onClick = {
                onClick()
            },
            modifier = modifier,
            colors = colors.actionButtonColors
        ){
            Text(
                text = stringResource(text),
                fontSize = fontSize,
                color = colors.actionTextColor,
            )
        }
    }
}
