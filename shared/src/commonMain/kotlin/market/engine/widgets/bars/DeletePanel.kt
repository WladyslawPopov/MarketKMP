package market.engine.widgets.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.buttons.SmallIconButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeletePanel(
    selectedCount: Int,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .background(colors.primaryColor)
                .fillMaxWidth().padding(horizontal = dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stringResource(strings.actionDelete)} ($selectedCount)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.black,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallIconButton(
                    drawables.deleteIcon,
                    colors.negativeRed,
                    onClick = onDelete
                )
                ActionButton(
                    stringResource(strings.resetLabel),
                    fontSize = dimens.mediumText,
                    alignment = Alignment.CenterEnd,
                    onClick = onCancel
                )
            }
        }
    }
}
