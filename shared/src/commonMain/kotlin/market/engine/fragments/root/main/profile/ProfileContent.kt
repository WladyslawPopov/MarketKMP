package market.engine.fragments.root.main.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import market.engine.core.data.items.NavigationItem
import market.engine.fragments.base.EdgeToEdgeScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    component: ProfileComponent,
    modifier: Modifier,
    publicProfileNavigationItems: List<NavigationItem>
) {
    val viewModel = component.model.value.profileViewModel
    val isLoading by viewModel.isShowProgress.collectAsState()


   EdgeToEdgeScaffold(
       modifier = modifier.padding(top = TopAppBarDefaults.TopAppBarExpandedHeight).fillMaxSize(),
       isLoading = isLoading,
       onRefresh = {
           viewModel.viewModelScope.launch {
               viewModel.setLoading(true)
               viewModel.refresh()
               delay(2000)
               viewModel.setLoading(false)
           }
       },
   ) { contentPadding ->
       ProfileNavContent(
           publicProfileNavigationItems,
           contentPadding = contentPadding,
           goToSettings = {
               component.goToSettings(it)
           }
       )
   }
}
