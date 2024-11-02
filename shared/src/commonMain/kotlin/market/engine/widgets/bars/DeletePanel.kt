package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.widgets.buttons.ActionTextButton
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeletePanel(
    selectedCount: Int,
    scrollState: LazyListState,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val isVisible = remember { mutableStateOf(true) }

    var previousIndex by remember { mutableStateOf(0) }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { currentIndex ->
                if (currentIndex < previousIndex) {
                    isVisible.value = true
                } else if (currentIndex > previousIndex) {
                    isVisible.value = false
                }

                if (currentIndex == 0) {
                    isVisible.value = true
                }

                previousIndex = currentIndex
            }
    }
    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn() ,
        exit = fadeOut() ,
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stringResource(strings.deleteSelectOffers)} ($selectedCount)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.black,
                modifier = Modifier.padding(dimens.mediumPadding).fillMaxWidth(0.6f)
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallIconButton(
                    drawables.deleteIcon,
                    colors.inactiveBottomNavIconColor,
                    onClick = onDelete
                )
                ActionTextButton(
                    strings.resetLabel,
                    fontSize = 8.sp,
                    alignment = Alignment.CenterEnd,
                    onClick = onCancel
                )
            }
        }
    }
}
