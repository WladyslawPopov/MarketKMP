package market.engine.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActionTextButton(text : StringResource, onClick : () -> Unit) {
    Box(
        modifier = Modifier.padding(horizontal = dimens.smallPadding).fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd
    ) {
        TextButton(
            onClick = {
                onClick()
            },
            modifier = Modifier.padding(horizontal = dimens.smallPadding),
            colors = colors.actionButtonColors
        ){
            Text(
                text = stringResource(text),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                color = colors.actionTextColor,
            )
        }
    }
}
