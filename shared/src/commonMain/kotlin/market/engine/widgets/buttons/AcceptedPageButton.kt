package market.engine.widgets.buttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AcceptedPageButton(
    text: StringResource,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {

    TextButton(
        onClick = {
            onClick()
        },
        colors = colors.themeButtonColors,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        enabled = enabled
    ){
        Text(
            text = stringResource(text),
            color = colors.alwaysWhite,
            fontSize = MaterialTheme.typography.titleSmall.fontSize,
            lineHeight = dimens.largeText
        )
    }
}
