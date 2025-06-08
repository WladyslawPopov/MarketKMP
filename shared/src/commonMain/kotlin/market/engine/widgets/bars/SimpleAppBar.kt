package market.engine.widgets.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton

interface SimpleAppBarData{
    val modifier: Modifier
    val content : @Composable () -> Unit
    val onBackClick: () -> Unit
    val listItems: List<NavigationItem>
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAppBar(
    data: SimpleAppBarData
) {
    TopAppBar(
        modifier = data.modifier,
        title = {
            data.content()
        },
        navigationIcon = {
            NavigationArrowButton {
                data.onBackClick()
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, alignment = Alignment.End)
            ) {
                data.listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        BadgedButton(item)
                    }
                }
            }
        }
    )
}
