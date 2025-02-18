package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.value.MutableValue
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent

@Composable
fun ProfileContent(
    component: ProfileComponent,
    modifier: Modifier,
    publicProfileNavigationItems: MutableValue<List<NavigationItem>>
) {

    BackHandler(
        component.model.value.backHandler
    ){

    }

   BaseContent(
       topBar = {},
       modifier = modifier.fillMaxSize(),
       isLoading = false,
       onRefresh = {
            component.model.value.profileViewModel.updateUserInfo()
       },
   ) {
       ProfileNavContent(
           publicProfileNavigationItems.value,
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
