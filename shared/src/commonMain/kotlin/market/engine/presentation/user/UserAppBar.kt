package market.engine.presentation.user

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.items.NavigationItem
import market.engine.widgets.badges.getBadgedBox
import market.engine.widgets.buttons.NavigationArrowButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAppBar(
    titleContent : (@Composable () -> Unit)? = null,
    isVisibleUserPanel: Boolean,
    onUserSliderClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val listItems = listOf(
        NavigationItem(
            title = "",
            icon = if (isVisibleUserPanel) drawables.iconArrowUp else drawables.iconArrowDown,
            tint = colors.black,
            hasNews = false,
            badgeCount = null,
            onClick = onUserSliderClick
        ),
    )

    TopAppBar(
        modifier = Modifier
            .fillMaxWidth().animateContentSize().clickable {
                onUserSliderClick()
            },
        title = {
            if (!isVisibleUserPanel)
                titleContent?.invoke()
        },
        navigationIcon = {
            NavigationArrowButton {
                onBackClick()
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = dimens.smallPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        IconButton(
                            modifier = Modifier.size(dimens.smallIconSize),
                            onClick = { item.onClick() }
                        ) {
                            getBadgedBox(modifier = Modifier, item)
                        }
                    }
                }
            }
        }
    )
}
