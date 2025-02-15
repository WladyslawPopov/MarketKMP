package market.engine.fragments.root.main.profile.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import market.engine.fragments.base.BaseContent

@Composable
fun ProfileContent(
    component: ProfileComponent,
    modifier: Modifier
) {



   BaseContent(
       topBar = {},
       modifier = modifier.fillMaxSize(),
       isLoading = false,
       onRefresh = {
            component.updateProfile()
       },
   ) {
       ProfileNavContent(
           component.model.value.navigationItems,
           goToAllLots = {
               component.goToAllMyOfferListing()
           },
           goToAboutMe = {
               component.goToAboutMe()
           },
           goToSubscriptions = {
                component.goToSubscribe()
           }
       )
   }
}
