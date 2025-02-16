package market.engine.widgets.rows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun FilterContentHeaderRow(
    title: String,
    isShowClearBtn: Boolean,
    onClear: () ->Unit,
    onClosed: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallIconButton(
                drawables.closeBtn,
                colors.black,
            ){
                onClosed()
            }

            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(dimens.smallPadding)
            )
        }

        if (isShowClearBtn) {
            ActionButton(
                strings.clear
            ){
                onClear()
                onClosed()
            }
        }
    }
}
