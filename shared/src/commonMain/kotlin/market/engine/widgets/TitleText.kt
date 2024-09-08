package market.engine.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleText(text : StringResource) {
    Text(
        text = stringResource(text),
        fontSize = MaterialTheme.typography.titleMedium.fontSize,
        color = colors.black,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = dimens.smallPadding)
    )
}
