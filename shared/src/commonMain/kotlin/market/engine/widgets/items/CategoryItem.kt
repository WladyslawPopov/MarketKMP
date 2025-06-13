package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import market.engine.core.network.networkObjects.Category
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.TopCategory
import market.engine.widgets.ilustrations.getCategoryIcon
import org.jetbrains.compose.resources.painterResource

@Composable
fun CategoryItem(category: TopCategory, onClick: (TopCategory) -> Unit) {
    Box(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick(category) }
            .padding(horizontal = dimens.extraLargePadding, vertical = dimens.largePadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            getCategoryIcon(category.name)?.let {
                Image(
                    painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(dimens.smallIconSize)
                )
            }

            Text(
                text = category.name,
                color = colors.black,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


