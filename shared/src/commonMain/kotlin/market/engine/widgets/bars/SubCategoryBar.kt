package market.engine.widgets.bars

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.text.ifEmpty

@Composable
fun SubCategoryBar(
    selectedCategory: String,
    openCategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(colors.white, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .clickable {
                openCategory()
            }
            .fillMaxWidth()
            .padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    )
    {
        Icon(
            painterResource(drawables.listIcon),
            contentDescription = null,
            tint = colors.black,
            modifier = Modifier.size(dimens.extraSmallIconSize)
        )

        Text(
            text = selectedCategory.ifEmpty { stringResource(strings.categoryMain) },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colors.black,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            painterResource(drawables.nextArrowIcon),
            contentDescription = null,
            tint = colors.black,
            modifier = Modifier.size(dimens.extraSmallIconSize)
        )
    }
}
