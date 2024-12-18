package market.engine.fragments.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.FavScreenType
import market.engine.widgets.buttons.SimpleTextButton
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesAppBar(
    currentTab : FavScreenType,
    modifier: Modifier = Modifier,
    navigationClick : (FavScreenType) -> Unit,
) {
    val fav = stringResource(strings.myFavoritesTitle)
    val sub = stringResource(strings.mySubscribedTitle)

    val isTypeSelected = remember { mutableStateOf(currentTab) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.largePadding, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ){

                SimpleTextButton(
                    fav,
                    backgroundColor = if (isTypeSelected.value == FavScreenType.FAVORITES) colors.rippleColor else colors.white,
                ){
                    isTypeSelected.value = FavScreenType.FAVORITES
                    navigationClick(FavScreenType.FAVORITES)
                }

                SimpleTextButton(
                    sub,
                    if (isTypeSelected.value == FavScreenType.SUBSCRIBED) colors.rippleColor else colors.white,
                ){
                    isTypeSelected.value = FavScreenType.SUBSCRIBED
                    navigationClick(FavScreenType.SUBSCRIBED)
                }
            }
        }
    )
}
