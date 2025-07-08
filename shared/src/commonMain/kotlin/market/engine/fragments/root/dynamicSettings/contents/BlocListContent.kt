package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.ListItem
import market.engine.widgets.items.BlocListItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun BlocListContent(
    blocList: List<ListItem>,
    deleteFromBlocList: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Text(
            stringResource(if(blocList.isEmpty()) strings.settingsBlocListEmpty else strings.settingsBlocListHeader),
            style = MaterialTheme.typography.titleSmall,
            color = colors.black
        )

        blocList.forEach { item ->
            BlocListItem(item){
                deleteFromBlocList(item.id)
            }
        }
    }
}
