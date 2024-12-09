package market.engine.presentation.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.globalData.ThemeResources.drawables
import market.engine.core.globalData.ThemeResources.strings
import market.engine.core.globalData.UserData
import market.engine.core.items.NavigationItem
import market.engine.presentation.base.BaseContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.LoadImage
import market.engine.widgets.texts.TitleText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileContent(
    component: UserComponent,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()
    val userInfo = UserData.userInfo

   BaseContent(
       modifier = modifier.fillMaxSize(),
       isLoading = false,
       onRefresh = {},
   ) {
       Column {
           Row(
               modifier = modifier.fillMaxWidth().padding(dimens.smallPadding),
               verticalAlignment = Alignment.Top,
               horizontalArrangement = Arrangement.spacedBy(
                   dimens.extraSmallPadding,
                   Alignment.CenterHorizontally
               ),
           ) {
               val image = userInfo?.avatar?.thumb?.content

               Card(
                   modifier = Modifier.padding(dimens.smallPadding),
                   shape = MaterialTheme.shapes.extraLarge
               ) {
                   if (image != null) {
                       LoadImage(
                           url = image,
                           isShowLoading = false,
                           isShowEmpty = false,
                           size = 60.dp
                       )
                   } else {
                       Icon(
                           painter = painterResource(drawables.profileIcon),
                           contentDescription = "",
                           tint = colors.black,
                           modifier = Modifier.size(dimens.mediumIconSize)
                       )
                   }
               }

               Column(
                   verticalArrangement = Arrangement.Center,
                   horizontalAlignment = Alignment.Start
               ) {
                   Row {
                       TitleText(
                           text = userInfo?.login.toString()
                       )
                   }

                   Row(
                       verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.Start,
                       modifier = Modifier.padding(dimens.smallPadding)
                   ) {
                       Text(
                           stringResource(strings.ratingParameterName)
                       )

                       TitleText(
                           text = userInfo?.rating.toString(),
                           color = colors.ratingBlue
                       )

                       if (userInfo?.isVerified == true) {
                           Image(
                               painterResource(drawables.verifySellersIcon),
                               contentDescription = "",
                               modifier = Modifier.size(dimens.smallIconSize)
                           )
                       }

                       Spacer(modifier = Modifier.width(dimens.smallPadding))

                       val imageRating = UserData.userInfo?.ratingBadge?.imageUrl

                       if (imageRating != null) {
                           LoadImage(
                               url = imageRating,
                               isShowLoading = false,
                               isShowEmpty = false,
                               size = 30.dp
                           )
                       }

                       Spacer(modifier = Modifier.width(dimens.smallPadding))

                       SimpleTextButton(
                           text = "id",
                           shape = MaterialTheme.shapes.large
                       ) {

                       }
                   }
               }
           }
       }
   }
}
