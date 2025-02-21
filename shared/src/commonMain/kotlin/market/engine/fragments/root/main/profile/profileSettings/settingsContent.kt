package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.texts.SeparatorLabel

@Composable
fun settingsContent(
    separatorString: String,
    list: List<NavigationItem>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
        horizontalAlignment = Alignment.Start
    ) {
        SeparatorLabel(
            title = separatorString
        )
        list.forEach { item ->
            if (item.isVisible) {
                getNavigationItem(
                    item,
                    label = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                item.title,
                                color = colors.black,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                lineHeight = dimens.largeText,
                            )

                            if (item.subtitle != null) {
                                Text(
                                    item.subtitle,
                                    color = colors.grayText,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    lineHeight = dimens.largeText
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}
