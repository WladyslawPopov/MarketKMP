package market.engine.fragments.dynamicSettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicSettingsContent(
    component : DynamicSettingsComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.dynamicSettingsViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()
    val builderDescription by viewModel.builderDescription.collectAsState()
    val errorSettings by viewModel.errorSettings.collectAsState()
    val settingsType = model.settingsType

    val titleText = remember { mutableStateOf("") }

    BaseContent(
        topBar = {
            DynamicAppBar(
                title = titleText.value,
                navigateBack = {
                    component.onBack()
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        isLoading = isLoading.value,
        onRefresh = {
            viewModel.onError(ServerErrorException())
        },
        toastItem = viewModel.toastItem
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            when(settingsType){
                "set_login" -> {
                    titleText.value = stringResource(strings.setLoginTitle)

                    if (builderDescription?.operationResult?.result == "ok") {
                        builderDescription?.fields?.find { it.widgetType == "input" }?.let {
                            DynamicLabel(
                                stringResource(strings.setNewLoginTitle),
                                true
                            )

                            DynamicInputField(
                                field = it
                            )
                        }
                        AcceptedPageButton(
                            strings.actionChangeLabel,
                        ){

                        }
                    }else{
                        if (errorSettings != null) {
                            Text(
                                errorSettings?.first!!,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.black
                            )

                            Text(
                                errorSettings?.second!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.black
                            )
                        }
                    }
                }
            }
        }
    }
}
