package market.engine.widgets.tabs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.TabWithIcon
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun PageTab(
    tab: TabWithIcon,
    selectedTab: Int,
    currentIndex: Int,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    isDragMode: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // Animation state for shaking
    val offsetX = remember { Animatable(0f) }

    // Start or stop the shaking animation based on drag mode
    LaunchedEffect(isDragMode) {
        if (isDragMode) {
            // Rapid, small oscillation for shake effect
            offsetX.animateTo(
                targetValue = 1.5f, // Reduced for subtler shake
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(
        modifier = modifier
            .offset(
                x = if (isDragMode) offsetX.value.dp else 0.dp
            )
            .height(dimens.mediumIconSize + dimens.smallPadding)
            .clip(MaterialTheme.shapes.small)
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterHorizontally)
    ) {
        when {
            tab.icon != null -> {
                Icon(
                    painter = painterResource(tab.icon),
                    contentDescription = tab.title,
                    modifier = Modifier.size(dimens.smallIconSize)
                )
            }
            tab.image != null -> {
                LoadImage(
                    url = tab.image,
                    modifier = Modifier.size(dimens.smallIconSize)
                )
            }
        }

        Text(
            text = tab.title,
            style = textStyle,
            maxLines = 1,
            color = if (selectedTab != currentIndex) colors.grayText else colors.black
        )
        if (tab.isPined) {
            Icon(
                painter = painterResource(drawables.pinIcon),
                contentDescription = tab.title,
                modifier = Modifier.size(dimens.smallIconSize)
            )
        }
    }
}
