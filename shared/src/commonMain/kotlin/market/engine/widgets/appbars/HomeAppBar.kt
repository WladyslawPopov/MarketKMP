package market.engine.widgets.appbars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.dimens
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class AppBarItem(
    val title: String,
    val icon: DrawableResource,
    val tint: Color,
    val badgeCount: Int? = null,
    val hasNews: Boolean,
    val isVisible: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    modifier: Modifier = Modifier,
    showNavigationRail: Boolean,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val listItems = listOf(
        AppBarItem(
            title = stringResource(strings.proposalTitle),
            icon = drawables.currencyIcon,
            tint = colors.notifyTextColor,
            hasNews = false,
            badgeCount = 4,
            isVisible = true
        ),
        AppBarItem(
            title = stringResource(strings.mailTitle),
            icon = drawables.mail,
            tint = colors.brightBlue,
            hasNews = false,
            badgeCount = null,
            isVisible = true
        ),
        AppBarItem(
            title = stringResource(strings.notificationTitle),
            icon = drawables.notification,
            tint = colors.titleTextColor,
            hasNews = false,
            badgeCount = null,
            isVisible = true
        ),
    )

    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showNavigationRail) modifier.padding(start = 72.dp)
                else modifier
            ),
        title = {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!showNavigationRail) {
                    Icon(
                        painter = painterResource(drawables.menuHamburger),
                        contentDescription = stringResource(strings.menuTitle),
                        modifier = modifier
                            .size(dimens.smallIconSize)
                            .clickable {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                        tint = colors.black
                    )
                }

                Icon(
                    painter = painterResource(drawables.logo),
                    contentDescription = stringResource(strings.homeTitle),
                    modifier = modifier,
                    tint = colors.titleTextColor,
                )

                Row {
                    listItems.forEachIndexed{ index, item ->
                        if(item.isVisible){
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount != null){
                                        Badge {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    } else {
                                        if (item.hasNews) {
                                            Badge()
                                        }
                                    }
                                }
                            ){
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = item.title,
                                    modifier = modifier.size(dimens.smallIconSize)
                                        .clickable { /* Действие при нажатии на иконку уведомлений */ },
                                    tint = item.tint
                                )
                            }

                            if (index > 0) {
                                Spacer(modifier = Modifier.width(dimens.smallPadding))
                            }else{
                                Spacer(modifier = Modifier.width(dimens.mediumPadding))
                            }
                        }
                    }
                }
            }
        }
    )
}
