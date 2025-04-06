package market.engine.fragments.root.main.createOffer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.common.Platform
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.PlatformWindowType
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferAppBar(
    type: CreateOfferType,
    onBackClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val title = when(type){
        CreateOfferType.EDIT -> stringResource(strings.editOfferLabel)
        else -> {
            stringResource(strings.createNewOfferTitle)
        }
    }

    val listItems = listOf(
        NavigationItem(
            title = "",
            icon = drawables.recycleIcon,
            tint = colors.inactiveBottomNavIconColor,
            hasNews = false,
            isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
            badgeCount = null,
            onClick = onRefresh
        ),
    )


    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            NavigationArrowButton {
                onBackClick()
            }
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
                    listItems.forEachIndexed { _, item ->
                        if(item.isVisible){
                            BadgedButton(item)
                        }
                    }
                }
            }
        }
    )
}
