package market.engine.widgets.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import market.engine.core.network.networkObjects.RemoveBid

@Composable
fun RemovedBidsListItem(
    i: Int,
    bid: RemoveBid,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimens.smallPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${i + 1}. ${bid.bidderLogin ?: "User"} (${bid.bidderRating})",
            style = MaterialTheme.typography.bodySmall,
            color = colors.black,
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = bid.ownerComments ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = colors.black,
            )
        }
    }
}
