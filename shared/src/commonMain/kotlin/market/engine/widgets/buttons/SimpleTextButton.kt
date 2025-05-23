package market.engine.widgets.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens


@Composable
fun SimpleTextButton(
    text: String,
    backgroundColor: Color = colors.white,
    textColor: Color = colors.black,
    textStyle : TextStyle = MaterialTheme.typography.titleSmall,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadIcon : @Composable (() -> Unit)? = null,
    trailingIcon : @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.textButtonColors(
            containerColor = backgroundColor,
            disabledContainerColor = colors.rippleColor,
            disabledContentColor = colors.grayText
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        enabled = enabled
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadIcon?.invoke()

            Text(
                text = text,
                color = textColor,
                style = textStyle,
                textAlign = TextAlign.Center,
            )

            trailingIcon?.invoke()
        }
    }
}
