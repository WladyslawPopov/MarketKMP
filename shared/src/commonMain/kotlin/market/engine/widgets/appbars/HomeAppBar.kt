package market.engine.widgets.appbars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import market.engine.theme.ThemeResources
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    modifier: Modifier = Modifier,
    showNavigationRail: Boolean,
    scope: CoroutineScope,
    drawerState: DrawerState,
    themeResources: ThemeResources
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showNavigationRail) Modifier.padding(start = 72.dp)
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
                        painter = painterResource(themeResources.drawables.menuHamburger),
                        contentDescription = "Menu",
                        modifier = Modifier
                            .padding( horizontal = 16.dp)
                            .size(24.dp)
                            .clickable {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                        tint = Color.White
                    )
                }

                // Название приложения
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Auction.ru",
                    color = Color.Red,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    // Иконка почты
                    Icon(
                        painter = painterResource(themeResources.drawables.search),
                        contentDescription = "Mail",
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .size(24.dp)
                            .clickable { /* Действие при нажатии на иконку почты */ },
                        tint = Color.White
                    )

                    // Иконка уведомлений
                    Icon(
                        painter = painterResource(themeResources.drawables.search),
                        contentDescription = "Notifications",
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .size(24.dp)
                            .clickable { /* Действие при нажатии на иконку уведомлений */ },
                        tint = Color.White
                    )
                }
            }
        }
    )
}
