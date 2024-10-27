package market.engine.widgets.badges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.types.ToastType
import org.jetbrains.compose.resources.painterResource

@Composable
fun ToastTypeMessage(
    isVisible: Boolean = false,
    modifier: Modifier = Modifier,
    message: String,
    toastType: ToastType = ToastType.SUCCESS
) {
    val icon = when (toastType) {
        ToastType.SUCCESS -> painterResource(drawables.successIcon)
        ToastType.ERROR -> painterResource(drawables.closeBtn)
        ToastType.WARNING -> painterResource(drawables.warningIcon)
    }

    val color = when (toastType) {
        ToastType.SUCCESS -> Color.Green
        ToastType.ERROR -> Color.Red
        ToastType.WARNING -> Color.Yellow
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .padding(dimens.mediumPadding)
            .clip(MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .background(colors.transparentGrayColor)
                .fillMaxWidth()
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.mediumPadding, Alignment.Start)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.sizeIn(minWidth = 50.dp, maxWidth = 80.dp),
                tint = color
            )
            Text(
                text = message,
                color = colors.black,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
