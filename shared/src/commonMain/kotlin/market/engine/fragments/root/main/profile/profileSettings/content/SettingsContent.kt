package market.engine.fragments.root.main.profile.profileSettings.content

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
import market.engine.core.data.items.NavigationItemUI
import market.engine.widgets.items.getNavigationItem
import market.engine.widgets.texts.SeparatorLabel

@Composable
fun SettingsContent(
    separatorString: String,
    list: List<NavigationItemUI>,
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
            if (item.data.isVisible) {
                getNavigationItem(
                    item,
                    label = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                item.data.title,
                                color = colors.black,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                lineHeight = dimens.largeText,
                            )

                            if (item.data.subtitle != null) {
                                Text(
                                    item.data.subtitle,
                                    color = colors.grayText,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    lineHeight = dimens.largeText
                                )
                            }
                        }
                    },
                ){
                    item.onClick()
                }
            }
        }
    }
}
