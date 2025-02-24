package market.engine.fragments.root.main.user

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.widgets.badges.BadgedButton
import market.engine.widgets.buttons.NavigationArrowButton
import org.jetbrains.compose.resources.stringResource

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
            title = stringResource(strings.searchUserStringChoice),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, alignment = Alignment.End)
            ) {
                listItems.forEachIndexed{ _, item ->
                    if(item.isVisible){
                        BadgedButton(item)
                    }
                }
            }
        }
    )
}
