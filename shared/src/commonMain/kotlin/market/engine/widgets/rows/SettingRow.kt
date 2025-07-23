package market.engine.widgets.rows

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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.buttons.ActionButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingRow(
    label: String,
    body: @Composable () -> Unit,
    action: @Composable ((() -> Unit)?) -> Unit = { click->
        if (click != null) {
            ActionButton(
                stringResource(strings.actionChangeLabel),
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
            ) {
                click()
            }
        }
    },
    onClick: (() -> Unit)? = null
){
    Row(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        horizontalArrangement = Arrangement.spacedBy(dimens.extraSmallPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.grayText
            )

            body()
        }

        action(onClick)
    }
}
