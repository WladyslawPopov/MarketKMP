package market.engine.widgets.bars.appBars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.states.SimpleAppBarData
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.dropdown_menu.PopUpMenu


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAppBar(
    modifier: Modifier = Modifier,
    data: SimpleAppBarData,
    content : @Composable () -> Unit = {}
) {
    val openMenu = remember { mutableStateOf(false) }

    LaunchedEffect(data.menuItems) {
        snapshotFlow {
            data.menuItems
        }.collect {
            if (it.isNotEmpty()) {
                openMenu.value = true
            }
        }
    }

    LaunchedEffect(openMenu.value) {
        snapshotFlow {
            openMenu.value
        }.collectLatest {
            if (!it) {
                delay(300)
                data.closeMenu()
            }
        }
    }

    TopAppBar(
        modifier = modifier,
        title = {
            content()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = data.color
        ),
        navigationIcon = if (data.onBackClick != null) {
            {
                NavigationArrowButton {
                    data.onBackClick.invoke()
                }
            }
        }else{
            {}
        },
        actions = {
            Column {
                Row(
                    modifier = Modifier.padding(end = dimens.smallPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimens.smallPadding,
                        alignment = Alignment.End
                    )
                ) {
                    data.listItems.forEachIndexed { _, item ->
                        if (item.isVisible) {
                            BadgedButton(item)
                        }
                    }
                }

                PopUpMenu(
                    openPopup = openMenu.value,
                    menuList = data.menuItems,
                ) {
                    openMenu.value = false
                }
            }
        }
    )
}
