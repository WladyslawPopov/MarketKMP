package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.TopCategory
import market.engine.core.data.types.WindowType
import market.engine.core.utils.getWindowType
import org.jetbrains.compose.resources.painterResource

@Composable
fun PopularCategoryItem(modifier: Modifier, category: TopCategory, onClick: (TopCategory) -> Unit) {
    val windowClass = getWindowType()
    val bs = windowClass == WindowType.Big

    Card(
        modifier = Modifier.sizeIn(
            minHeight = 100.dp,
            minWidth = 100.dp,
            maxHeight = if (!bs) 200.dp else 200.dp,
            maxWidth = if (!bs) 200.dp else 300.dp
        ).fillMaxWidth().wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = colors.transparent,
        ),
        shape = RoundedCornerShape(dimens.smallCornerRadius),
        onClick = { onClick(category) },
    ) {
        Column(
            modifier = modifier.align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(category.icon),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
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
}
