package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.TopCategory
import org.jetbrains.compose.resources.painterResource

@Composable
fun FooterItem(
    modifier: Modifier = Modifier.sizeIn(minWidth = if (isBigScreen.value) 300.dp else 100.dp),
    category: TopCategory,
    onClick: (TopCategory) -> Unit
) {
    Card(
        colors = colors.cardColors,
        shape = MaterialTheme.shapes.small,
        onClick = {
            onClick(category)
        },
    ) {
        Column(
            modifier = modifier.padding(dimens.smallPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(category.icon),
                contentDescription = null,
                modifier = Modifier.size(70.dp)
            )

            Text(
                text = category.name,
                color = colors.black,
                letterSpacing = 0.1.sp,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                textAlign = TextAlign.Center
            )
        }
    }
}
