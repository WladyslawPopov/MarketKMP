package market.engine.widgets.dropdown_menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun getDropdownMenu(
    selectedText : String?=null,
    selectedTextDef : String? = null,
    selects: List<String>,
    onItemClick: (String) -> Unit,
    onClearItem: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectDef = selectedTextDef ?: stringResource(strings.chooseAction)

    var selectedOption by remember { mutableStateOf(selectedText ?: selectDef) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )

    Column(
        modifier = Modifier
            .widthIn(min = 300.dp, max = 500.dp)
            .shadow(elevation = 3.dp, shape = MaterialTheme.shapes.medium, true)
            .clip(MaterialTheme.shapes.medium)
            .background(color = colors.white)
            .animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable {
                    expanded = !expanded
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                selectedOption,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(dimens.mediumPadding)
            )
            Row {
                AnimatedVisibility(!expanded) {
                    if (selectedOption != selectDef) {
                        SmallIconButton(
                            drawables.cancelIcon,
                            colors.black,
                            onClick = {
                                onClearItem()
                                selectedOption = selectDef
                            },
                            modifier = Modifier.padding(end = dimens.smallPadding)
                        )
                    }
                }

                SmallIconButton(
                    drawables.iconArrowDown,
                    colors.black,
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .padding(end = dimens.smallPadding)
                        .graphicsLayer {
                            rotationZ = rotationAngle
                        }
                )
            }
        }

        DropdownMenu(
            offset = DpOffset(20.dp, 0.dp),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded },
            containerColor = colors.white,
            modifier = Modifier.widthIn(max = 350.dp).heightIn(max = 400.dp).fillMaxWidth()
        ) {
            selects.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onItemClick(option)
                        selectedOption = option
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
