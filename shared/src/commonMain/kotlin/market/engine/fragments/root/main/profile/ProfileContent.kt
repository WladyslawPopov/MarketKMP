package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.BaseContent

@Composable
fun ProfileContent(
    component: ProfileComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val viewModel = component.model.value.profileViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()

    BackHandler(
        component.model.value.backHandler
    ){

    }

   BaseContent(
       topBar = {},
       modifier = modifier.fillMaxSize(),
       isLoading = isLoading.value,
       onRefresh = {
           viewModel.viewModelScope.launch {
               viewModel.setLoading(true)
               viewModel.updateUserInfo()
               delay(2000)
               viewModel.setLoading(false)
           }
       },
   ) {
       ProfileNavContent(
           publicProfileNavigationItems,
           goToSettings = {
               component.goToSettings(it)
           }
       )
   }
}
