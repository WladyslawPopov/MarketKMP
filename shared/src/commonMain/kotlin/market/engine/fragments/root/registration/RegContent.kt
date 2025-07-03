package market.engine.fragments.root.registration

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.OnError
import market.engine.fragments.base.NoItemsFoundLayout
import market.engine.widgets.rows.LazyColumnWithScrollBars
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegistrationContent(
    component: RegistrationComponent,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val modelState = component.model.subscribeAsState()
    val model = modelState.value.regViewModel

    val getRegFields = model.responseGetRegFields.collectAsState()

    val isLoading = model.isShowProgress.collectAsState()
    val err = model.errorMessage.collectAsState()

    BackHandler(modelState.value.backHandler){
        component.onBack()
    }

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { OnError(err.value) {
            model.getRegFields()
            model.onError(ServerErrorException())
        } }
    } else {
        null
    }

    val showSuccessReg = remember { mutableStateOf(false) }

    BaseContent(
        modifier = modifier,
        topBar = {
            RegistrationAppBar(
                title = stringResource(strings.registration),
                modifier = modifier,
                onBeakClick = {
                    component.onBack()
                }
            )
        },
        toastItem = model.toastItem,
        error = error,
        isLoading = isLoading.value,
        onRefresh = { model.getRegFields() }
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                },
        ) {
            LazyColumnWithScrollBars(
                modifierList = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(if(isBigScreen.value) 0.7f else 1f)
                    .padding(dimens.smallPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showSuccessReg.value){
                    item(getRegFields.value?.fields?.size) {
                        getRegFields.value?.fields?.let { SetUpDynamicFields(it) }
                    }

                    item {
                        SimpleTextButton(
                            stringResource(strings.registration),
                            backgroundColor = colors.brightGreen,
                            textStyle = MaterialTheme.typography.titleMedium,
                        ){
                            model.postRegistration{
                                showSuccessReg.value = true
                            }
                        }
                    }

                    item {
                        Text(
                            stringResource(strings.registrationDisclaimer),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.grayText
                        )
                    }
                } else {
                    item {
                        NoItemsFoundLayout(
                            icon = drawables.mail,
                            title = stringResource(strings.registrationSuccessLabel),
                            textButton = stringResource(strings.goBackLabel)
                        ) {
                            component.onBack()
                        }
                    }
                }
            }
        }
    }
}
