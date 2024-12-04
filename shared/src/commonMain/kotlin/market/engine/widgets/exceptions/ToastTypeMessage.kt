package market.engine.widgets.exceptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.types.ToastType
import org.jetbrains.compose.resources.painterResource

@Composable
fun ToastTypeMessage(
    message: String,
    toastType: ToastType = ToastType.SUCCESS
) {
    val icon = when (toastType) {
        ToastType.SUCCESS -> painterResource(drawables.successIcon)
        ToastType.ERROR -> painterResource(drawables.closeBtn)
        ToastType.WARNING -> painterResource(drawables.warningIcon)
    }

    val color = when (toastType) {
        ToastType.SUCCESS -> colors.positiveGreen
        ToastType.ERROR -> colors.negativeRed
        ToastType.WARNING -> colors.yellowSun
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.mediumPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 3.dp,
                    shape = MaterialTheme.shapes.medium,
                )
                .clip(MaterialTheme.shapes.medium)
                .background(colors.white)
                .padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding, Alignment.Start)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.sizeIn(minWidth = 60.dp, maxWidth = 80.dp),
                tint = color
            )
            Text(
                text = message,
                color = colors.black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
