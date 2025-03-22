package market.engine.fragments.root.main.favPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.FavScreenType
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavPagesAppBar(
    currentTab : FavScreenType,
    modifier: Modifier = Modifier,
    navigationClick : (FavScreenType) -> Unit,
) {
    val fav = stringResource(strings.myFavoritesTitle)
    val sub = stringResource(strings.mySubscribedTitle)

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.primaryColor,
            titleContentColor = colors.black,
            navigationIconContentColor = colors.black,
            actionIconContentColor = colors.black
        ) ,
        modifier = modifier
            .fillMaxWidth(),
        title = {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    SimpleTextButton(
                        fav,
                        backgroundColor = if (currentTab == FavScreenType.FAVORITES) colors.rippleColor else colors.white,
                    ) {
                        navigationClick(FavScreenType.FAVORITES)
                    }
                }
                item {
                    SimpleTextButton(
                        sub,
                        if (currentTab == FavScreenType.SUBSCRIBED) colors.rippleColor else colors.white,
                    ) {
                        navigationClick(FavScreenType.SUBSCRIBED)
                    }
                }
            }
        }
    )
}
