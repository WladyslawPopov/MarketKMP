package market.engine.widgets.bars

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens

@Composable
fun PagingCounterBar(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(dimens.smallPadding).animateContentSize()
    ) {
        Row(
            modifier = Modifier.wrapContentWidth().background(colors.grayLayout).padding(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentPage",
                modifier = Modifier.padding(start = dimens.smallPadding),
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = "/",
                modifier = Modifier.padding(start = dimens.smallPadding),
                style = MaterialTheme.typography.labelSmall,
                color = colors.titleTextColor
            )

            Text(
                text = "$totalPages",
                modifier = Modifier.padding(start = dimens.smallPadding),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
