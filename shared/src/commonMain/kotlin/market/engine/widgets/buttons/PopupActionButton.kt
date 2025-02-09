package market.engine.widgets.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables

@Composable
fun PopupActionButton(
    text : String = "",
    color : Color,
    tint : Color,
    isShowOptions: MutableState<Boolean>
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isShowOptions.value) 180f else 0f
    )
    TextButton(
        shape = MaterialTheme.shapes.small,
        onClick = {
            isShowOptions.value = !isShowOptions.value
        },
        colors = ButtonDefaults.textButtonColors(
            containerColor = color,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = tint,
                modifier = Modifier.padding(horizontal = dimens.smallPadding)
            )

            SmallIconButton(
                drawables.iconArrowDown,
                tint,
                onClick = {
                    isShowOptions.value = !isShowOptions.value
                },
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }.size(dimens.smallIconSize)
            )
        }
    }
}
