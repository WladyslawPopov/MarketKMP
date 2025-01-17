package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.UserData
import market.engine.fragments.base.BaseContent
import market.engine.widgets.rows.UserPanel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileContent(
    component: ProfileComponent,
    modifier: Modifier
) {
    val userInfo = UserData.userInfo
    val list = component.model.value.navigationItems

   BaseContent(
       topBar = {},
       modifier = modifier.fillMaxSize(),
       isLoading = false,
       onRefresh = {
            component.updateProfile()
       },
   ) {
       LazyColumn(
           modifier = Modifier.fillMaxWidth(),
           verticalArrangement = Arrangement.Center,
           horizontalAlignment = Alignment.CenterHorizontally
       ) {
           item {
               UserPanel(
                   modifier = Modifier.fillMaxWidth(),
                   userInfo,
                   goToUser = null,
                   goToAllLots = {
                        component.goToAllMyOfferListing()
                   },
                   goToAboutMe = {
                        component.goToAboutMe()
                   },
                   addToSubscriptions = {

                   },
                   goToSubscriptions = {

                   },
                   isBlackList = arrayListOf()
               )
           }
           itemsIndexed(list) { _, item ->
               NavigationDrawerItem(
                   label = {
                       Box(
                           modifier = Modifier.fillMaxWidth(),
                           contentAlignment = Alignment.CenterStart
                       ) {
                           Column(
                               verticalArrangement = Arrangement.Center,
                               horizontalAlignment = Alignment.Start
                           ) {
                               Text(
                                   stringResource(item.title),
                                   color = colors.black,
                                   fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                   lineHeight = dimens.largeText,
                               )
                               if (item.subtitle != null) {
                                   Text(
                                       stringResource(item.subtitle),
                                       color = colors.steelBlue,
                                       fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                       lineHeight = dimens.largeText
                                   )
                               }
                           }

                       }
                   },
                   onClick = item.onClick,
                   icon = {
                       Icon(
                           painter = painterResource(item.icon),
                           contentDescription = stringResource(item.title),
                           modifier = Modifier.size(dimens.smallIconSize),
                           tint = item.tint
                       )
                   },
                   badge = {
                       if (item.badgeCount != null) {
                           Badge {
                               Text(text = item.badgeCount.toString())
                           }
                       }

                       if (item.hasNews) {
                           Badge { }
                       }
                   },
                   modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                   colors = NavigationDrawerItemDefaults.colors(
                       selectedContainerColor = colors.white,
                       unselectedContainerColor = colors.white,
                       selectedIconColor = colors.textA0AE,
                       unselectedIconColor = colors.textA0AE,
                       selectedTextColor = colors.black,
                       selectedBadgeColor = colors.black,
                       unselectedTextColor = colors.black,
                       unselectedBadgeColor = colors.black
                   ),
                   shape = MaterialTheme.shapes.small,
                   selected = true
               )

               Spacer(modifier = Modifier.height(dimens.smallPadding))
           }
       }
   }
}
