package market.engine.widgets.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f
    )
    Box(
        modifier = Modifier.padding(dimens.smallPadding)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(color = colors.white)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        onExpandChange()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

                ) {
                Text(text = title, modifier = Modifier.padding(dimens.mediumPadding).weight(1f))
                SmallIconButton(
                    drawables.iconArrowDown,
                    colors.black,
                    onClick = { onExpandChange() },
                    modifier = Modifier
                        .padding(horizontal = dimens.smallPadding)
                        .graphicsLayer {
                            rotationZ = rotationAngle
                        }
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ){
                content()
            }
        }
    }
}
