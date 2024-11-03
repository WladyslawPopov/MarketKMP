package market.engine.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.types.FavScreenType
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesAppBar(
    modifier: Modifier = Modifier,
    navigationClick : (FavScreenType) -> Unit,
) {
    val fav = stringResource(strings.myFavoritesTitle)
    val sub = stringResource(strings.mySubscribedTitle)

    val isTypeSelected = remember { mutableStateOf(FavScreenType.FAVORITES) }

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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = fav,
                    modifier = Modifier
                        .background(if (isTypeSelected.value == FavScreenType.FAVORITES) colors.rippleColor else colors.white)
                        .clickable {
                            isTypeSelected.value = FavScreenType.FAVORITES
                            navigationClick(FavScreenType.FAVORITES)
                        }
                        .clip(MaterialTheme.shapes.small)
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(dimens.mediumSpacer))
                Text(
                    text = sub,
                    modifier = Modifier
                        .background(if (isTypeSelected.value == FavScreenType.SUBSCRIBED) colors.rippleColor else colors.white)
                        .clickable {
                            isTypeSelected.value = FavScreenType.SUBSCRIBED
                            navigationClick(FavScreenType.SUBSCRIBED)
                        }
                        .clip(MaterialTheme.shapes.small)
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall
                )
            }

        }
    )
}
