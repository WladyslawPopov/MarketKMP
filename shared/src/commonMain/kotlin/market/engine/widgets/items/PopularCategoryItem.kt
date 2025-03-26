package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.TopCategory
import org.jetbrains.compose.resources.painterResource

@Composable
fun PopularCategoryItem(category: TopCategory, onClick: (TopCategory) -> Unit) {
    Column(
        modifier = Modifier
            .background(colors.transparent)
            .clip(MaterialTheme.shapes.small)
            .clickable {
                onClick(category)
            }
            .padding(dimens.smallPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(category.icon),
            contentDescription = null,
            modifier = Modifier.size(if(isBigScreen)160.dp else 100.dp)
        )

        Text(
            text = category.name,
            color = colors.black,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
