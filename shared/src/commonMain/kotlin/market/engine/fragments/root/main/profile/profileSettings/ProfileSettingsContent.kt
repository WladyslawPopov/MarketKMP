package market.engine.fragments.root.main.profile.profileSettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.types.ProfileSettingsTypes
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.root.main.profile.profileSettings.content.GlobalSettings
import market.engine.fragments.root.main.profile.profileSettings.content.SettingsContent
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSettingsContent(
    component : ProfileSettingsComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.profileSettingsViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()
    val settingsType = model.type
    val err = viewModel.errorMessage.collectAsState()
    val toastItem = viewModel.toastItem.collectAsState()

    val refresh = {
        viewModel.onError(ServerErrorException())
        viewModel.refresh()
    }

    BackHandler(model.backHandler){
        component.goToBack()
    }

    val error : (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()){
        {
            OnError(err.value){
                refresh()
            }
        }
    }else{
        null
    }

    BaseContent(
        modifier = Modifier.fillMaxSize(),
        isLoading = isLoading.value,
        onRefresh = {
            refresh()
        },
        error = error,
        toastItem = toastItem.value
    ) {
        LazyColumnWithScrollBars{
            item {
                when(settingsType){
                    ProfileSettingsTypes.GLOBAL_SETTINGS -> GlobalSettings(component, viewModel)
                    ProfileSettingsTypes.SELLER_SETTINGS -> SettingsContent(
                        stringResource(strings.sellerSettingsTitle),
                        viewModel.sellerSettingsItems
                    )
                    ProfileSettingsTypes.ADDITIONAL_SETTINGS -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SettingsContent(
                                stringResource(strings.addressesSettingsTitle),
                                viewModel.addressItems
                            )
                            SettingsContent(
                                stringResource(strings.blackListSettingsTitle),
                                viewModel.blackListItems
                            )
                        }
                    }
                }
            }
        }
    }
}
