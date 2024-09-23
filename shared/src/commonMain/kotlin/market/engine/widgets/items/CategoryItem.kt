package market.engine.widgets.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import application.market.agora.business.networkObjects.Category
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.widgets.common.getCategoryIcon
import org.jetbrains.compose.resources.painterResource

@Composable
fun CategoryItem(category: Category, onClick: (Category) -> Unit) {
    Box(
        modifier = Modifier
            .background(colors.white, shape = RoundedCornerShape(dimens.smallPadding))
            .clip(RoundedCornerShape(dimens.smallPadding))
            .clickable { onClick(category) }
            .padding(horizontal = dimens.extraLargePadding, vertical = dimens.largePadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            getCategoryIcon(category.name)?.let {
                Image(
                    painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(dimens.smallIconSize)
                )
                Spacer(modifier = Modifier.width(dimens.smallPadding))
            }
            Text(
                text = category.name ?: "",
                color = colors.black,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}


