package market.engine.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import application.market.auction_mobile.ui.home.HomeComponent
import market.engine.theme.ThemeResources
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    component: HomeComponent,
    m: Modifier = Modifier,
    themeResources: ThemeResources
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = m,
                title = {
                    Row(
                        modifier = m.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Левый иконка меню
                        Icon(
                            painter = painterResource(themeResources.drawables.search),
                            contentDescription = "Menu",
                            modifier = Modifier
                                .padding(16.dp)
                                .size(24.dp)
                                .clickable { /* Действие при нажатии на меню */ },
                            tint = Color.White
                        )

                        // Название приложения
                        Text(
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
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(24.dp)
                                    .clickable { /* Действие при нажатии на иконку почты */ },
                                tint = Color.White
                            )

                            // Иконка уведомлений
                            Icon(
                                painter = painterResource(themeResources.drawables.search),
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(24.dp)
                                    .clickable { /* Действие при нажатии на иконку уведомлений */ },
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Text(text = "Home Content")
            }
        },
        modifier = m
    )
}
