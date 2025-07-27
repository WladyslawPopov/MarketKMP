package market.engine.widgets.dropdown_menu

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.ActionButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpandableContainer(
    modifier: Modifier = Modifier,
    collapsedMaxHeight: Dp = 300.dp,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isOverflowing: Boolean,
    onContentSizeChanged: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clipToBounds()
                .then(
                    if (isOverflowing && !isExpanded) {
                        Modifier.height(collapsedMaxHeight)
                    } else {
                        Modifier
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        onContentSizeChanged(size.height)
                    }
            ) {
                content()
            }
        }

        if (isOverflowing) {
            ActionButton(
                text = stringResource(
                    if (isExpanded) strings.showLessLabel else strings.showFullLabel
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                onToggle()
            }
        }
    }
}
