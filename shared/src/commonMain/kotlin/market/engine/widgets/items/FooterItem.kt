package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import market.engine.core.data.items.TopCategory
import market.engine.core.data.types.WindowType
import market.engine.core.util.getWindowType
import org.jetbrains.compose.resources.painterResource

@Composable
fun FooterItem(modifier: Modifier, category: TopCategory, onClick: (TopCategory) -> Unit) {
    val windowClass = getWindowType()
    val showNavigationRail = windowClass == WindowType.Big
    Box(
        modifier = Modifier.sizeIn(minWidth = if (showNavigationRail) 300.dp else 100.dp).clickable {
            onClick(category)
        }
    ) {
        Column(
            modifier = modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
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

            Spacer(modifier = Modifier.height(dimens.smallSpacer))
        }
    }
}
