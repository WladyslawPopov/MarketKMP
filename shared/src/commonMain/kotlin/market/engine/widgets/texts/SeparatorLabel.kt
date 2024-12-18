package market.engine.widgets.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SeparatorLabel(
    title: StringResource
){
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleLarge,
        color = colors.black,
        modifier = Modifier.padding(horizontal = dimens.mediumPadding, vertical = dimens.smallPadding)
    )
}
