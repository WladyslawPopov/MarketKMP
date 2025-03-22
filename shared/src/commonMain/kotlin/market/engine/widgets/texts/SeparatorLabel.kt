package market.engine.widgets.texts

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens

@Composable
fun SeparatorLabel(
    title: String,
    annotatedString: AnnotatedString? = null
){
    Text(
        text = annotatedString ?: buildAnnotatedString { append(title) },
        style = MaterialTheme.typography.titleLarge,
        color = colors.black,
        modifier = Modifier.padding(horizontal = dimens.mediumPadding, vertical = dimens.smallPadding)
    )
}
