package market.engine.widgets.dropdown_menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun getDropdownMenu(
    selectedText : String,
    selectedTextDef : String = stringResource(strings.chooseAction),
    paddingContainer: Dp = dimens.mediumPadding,
    selects: List<String>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onClearItem: (() -> Unit)?,
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Column(
        modifier = modifier
            .shadow(elevation = 1.dp, shape = MaterialTheme.shapes.small, true)
            .background(color = colors.white)
            .clip(MaterialTheme.shapes.small)
            .clickable {
                expanded = !expanded
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(paddingContainer),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            Text(
                selectedText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.black,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding)
            ) {
                AnimatedVisibility(
                    !expanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (selectedText != selectedTextDef && onClearItem != null && selectedText != "") {
                        SmallIconButton(
                            drawables.cancelIcon,
                            colors.black,
                            onClick = {
                                onClearItem()
                            },
                            modifierIconSize = Modifier.size(dimens.extraSmallIconSize),
                            modifier = Modifier.size(dimens.smallIconSize)
                        )
                    }
                }

                SmallIconButton(
                    drawables.iconArrowDown,
                    colors.black,
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .size(dimens.smallIconSize)
                        .graphicsLayer {
                            rotationZ = rotationAngle
                        },
                )
            }
        }

        DropdownMenu(
            offset = DpOffset(20.dp, 0.dp),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            containerColor = colors.white,
            modifier = Modifier
                .widthIn(max = 350.dp)
                .heightIn(max = 400.dp)
                .wrapContentSize()
        ) {
            selects.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onItemClick(option)
                        expanded = false
                    },
                    text = {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.black
                        )
                    },
                )
            }
        }
    }
}
