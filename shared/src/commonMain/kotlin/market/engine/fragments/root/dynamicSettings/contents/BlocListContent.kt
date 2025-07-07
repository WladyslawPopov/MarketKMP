package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.ListItem
import market.engine.fragments.root.dynamicSettings.DynamicSettingsViewModel
import market.engine.widgets.items.BlocListItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun BlocListContent(
    type : String,
    viewModel: DynamicSettingsViewModel,
    updateItemTrigger : Int
) {
    val blocList = remember { mutableStateOf(emptyList<ListItem>()) }

    LaunchedEffect(updateItemTrigger) {
        viewModel.getBlocList(type){
            blocList.value = it
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        Text(
            stringResource(if(blocList.value.isEmpty()) strings.settingsBlocListEmpty else strings.settingsBlocListHeader),
            style = MaterialTheme.typography.titleSmall,
            color = colors.black
        )

        blocList.value.forEach { item ->
            BlocListItem(item){
                viewModel.deleteFromBlocList(
                    type, item.id
                ){
                    blocList.value = blocList.value.toMutableList().also { it.remove(item) }
                }
            }
        }
    }
}
