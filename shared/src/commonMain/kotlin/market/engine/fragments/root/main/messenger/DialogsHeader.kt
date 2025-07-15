package market.engine.fragments.root.main.messenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.items.MesHeaderItem
import market.engine.widgets.ilustrations.LoadImage
import org.jetbrains.compose.resources.painterResource

@Composable
fun DialogsHeader(
    headerItem : MesHeaderItem
) {
    Row(
        modifier = Modifier
            .clickable {
                headerItem.onClick()
            }
            .background(color = colors.white)
            .fillMaxWidth().padding(dimens.smallPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        LoadImage(
            url = headerItem.image.toString(),
            modifier = Modifier.size(40.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(dimens.smallSpacer)
        ) {
            Text(
                text = headerItem.title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.black,
                maxLines = 2
            )
            Text(
                text = headerItem.subtitle,
                style = MaterialTheme.typography.titleSmall,
                color = colors.black
            )
        }

        Icon(
            painterResource(drawables.nextArrowIcon),
            contentDescription = null,
            tint = colors.inactiveBottomNavIconColor,
            modifier = Modifier.size(dimens.mediumIconSize)
        )
    }
}
