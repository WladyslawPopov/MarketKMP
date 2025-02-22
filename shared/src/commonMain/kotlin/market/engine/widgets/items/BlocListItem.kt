package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.network.networkObjects.ListItem
import market.engine.core.utils.convertDateWithMinutes
import market.engine.widgets.buttons.SmallIconButton

@Composable
fun BlocListItem(
    item : ListItem,
    deleteClick : () -> Unit,
) {
    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(dimens.smallPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(dimens.mediumPadding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
            ) {
                Text(
                    buildString {
                        append(item.name)
                        append(" (${item.rating})")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.black
                )

                Text(
                    item.comment ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.black
                )

                Text(
                    item.listedAt.toString().convertDateWithMinutes(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.black,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            SmallIconButton(
                drawables.deleteIcon,
                color = colors.inactiveBottomNavIconColor,
            ){
                deleteClick()
            }
        }
    }
}
