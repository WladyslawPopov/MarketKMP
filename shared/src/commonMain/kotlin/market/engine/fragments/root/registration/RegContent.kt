package market.engine.fragments.root.registration

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.coroutines.launch
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.BackHandler
import market.engine.widgets.exceptions.SetUpDynamicFields
import market.engine.widgets.exceptions.onError
import market.engine.widgets.exceptions.showNoItemLayout
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegistrationContent(
    component: RegistrationComponent,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
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
        { onError(err.value) {  } }
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
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(dimens.mediumPadding),
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
                            backgroundColor = colors.positiveGreen,
                            textStyle = MaterialTheme.typography.titleMedium,
                        ){
                            model.viewModelScope.launch {
                                val res = model.postRegistration()

                                if (res) {
                                    showSuccessReg.value = true
                                }
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
                        showNoItemLayout(
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
