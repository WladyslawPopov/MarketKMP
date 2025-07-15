package market.engine.widgets.items

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.DialogsData
import market.engine.core.utils.convertDateYear
import market.engine.core.utils.getCurrentDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun SeparatorDialogItem(
    item: DialogsData.SeparatorItem
){
    val today = stringResource(strings.todayLabel)
    Row(
        modifier = Modifier
            .padding(dimens.mediumPadding)
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        Text(
            text = if(getCurrentDate().convertDateYear() == item.dateTime){
                today
            } else {
                item.dateTime
            },
            style = MaterialTheme.typography.titleSmall,
            color = colors.grayText,
            modifier = Modifier.padding(horizontal = dimens.mediumPadding)
        )
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}
