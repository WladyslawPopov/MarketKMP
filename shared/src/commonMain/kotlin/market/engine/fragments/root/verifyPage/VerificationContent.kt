package market.engine.fragments.root.verifyPage

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun VerificationContent(
    component : VerificationComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.verificationViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()
    val settingsType = model.settingsType

    val err = viewModel.errorMessage.collectAsState()

    val toastItem = viewModel.toastItem.collectAsState()

    val focusManager = LocalFocusManager.current

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage.isNotBlank()) {
            { OnError(err.value) { viewModel.onError(ServerErrorException()) } }
        } else {
            null
        }
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    EdgeToEdgeScaffold(
        topBar = {
            VerificationAppBar(
                navigateBack = {
                    component.onBack()
                }
            )
        },
        modifier = Modifier.pointerInput(Unit){
            detectTapGestures {
                focusManager.clearFocus()
            }
        }.fillMaxSize(),
        isLoading = isLoading.value,
        onRefresh = {
            viewModel.init(model.settingsType, model.owner, model.code)
        },
        toastItem = toastItem.value,
        error = error
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            item {
                when(settingsType) {
                    "set_password" -> {
                        DynamicLabel(
                            stringResource(strings.verificationSmsLabel),
                            true
                        )

                        val textState = remember {
                            mutableStateOf("")
                        }


                        Column(
                            modifier = Modifier.fillMaxWidth(if(isBigScreen.value) 0.5f else 1f).padding(dimens.mediumPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            TextField(
                                value = textState.value,
                                onValueChange = {
                                    textState.value = it
                                },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = colors.black,
                                    unfocusedTextColor = colors.black,

                                    focusedContainerColor = colors.white,
                                    unfocusedContainerColor = colors.white,

                                    focusedIndicatorColor = colors.transparent,
                                    unfocusedIndicatorColor = colors.transparent,
                                    disabledIndicatorColor = colors.transparent,
                                    errorIndicatorColor = colors.transparent,

                                    errorContainerColor = colors.white,

                                    focusedPlaceholderColor = colors.steelBlue,
                                    unfocusedPlaceholderColor = colors.steelBlue,
                                    disabledPlaceholderColor = colors.transparent
                                ),
                                textStyle = MaterialTheme.typography.titleSmall,
                            )
                        }


                        AcceptedPageButton(
                            stringResource(strings.actionChangeLabel)
                        ){
                            viewModel.postSetPassword(model.owner, textState.value) {
                                component.onBack()
                            }
                        }
                    }
                    "set_phone" -> {
                        DynamicLabel(
                            stringResource(strings.htmlVerifyLabel),
                            false
                        )

                        val textState = remember {
                            mutableStateOf("")
                        }


                        Column(
                            modifier = Modifier.fillMaxWidth(if(isBigScreen.value) 0.5f else 1f).padding(dimens.mediumPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            TextField(
                                value = textState.value,
                                onValueChange = {
                                    textState.value = it
                                },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = colors.black,
                                    unfocusedTextColor = colors.black,

                                    focusedContainerColor = colors.white,
                                    unfocusedContainerColor = colors.white,

                                    focusedIndicatorColor = colors.transparent,
                                    unfocusedIndicatorColor = colors.transparent,
                                    disabledIndicatorColor = colors.transparent,
                                    errorIndicatorColor = colors.transparent,

                                    errorContainerColor = colors.white,

                                    focusedPlaceholderColor = colors.steelBlue,
                                    unfocusedPlaceholderColor = colors.steelBlue,
                                    disabledPlaceholderColor = colors.transparent
                                ),
                                textStyle = MaterialTheme.typography.titleSmall,
                            )
                        }


                        AcceptedPageButton(
                            stringResource(strings.acceptAction)
                        ){
                            viewModel.postSetPhone(textState.value) {
                                component.onBack()
                            }
                        }
                    }
                    else -> {
                        if (model.owner != null && model.code != null && viewModel.action.value == "change_email") {
                            DynamicLabel(
                                stringResource(strings.verificationSmsLabel),
                                true
                            )

                            val textState = remember {
                                mutableStateOf("")
                            }


                            Column(
                                modifier = Modifier.fillMaxWidth(if(isBigScreen.value) 0.5f else 1f).padding(dimens.mediumPadding),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                            ) {
                                TextField(
                                    value = textState.value,
                                    onValueChange = {
                                        textState.value = it
                                    },
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = colors.black,
                                        unfocusedTextColor = colors.black,

                                        focusedContainerColor = colors.white,
                                        unfocusedContainerColor = colors.white,

                                        focusedIndicatorColor = colors.transparent,
                                        unfocusedIndicatorColor = colors.transparent,
                                        disabledIndicatorColor = colors.transparent,
                                        errorIndicatorColor = colors.transparent,

                                        errorContainerColor = colors.white,

                                        focusedPlaceholderColor = colors.steelBlue,
                                        unfocusedPlaceholderColor = colors.steelBlue,
                                        disabledPlaceholderColor = colors.transparent
                                    ),
                                    textStyle = MaterialTheme.typography.titleSmall,
                                )
                            }


                            AcceptedPageButton(
                                stringResource(strings.actionChangeLabel)
                            ) {
                                viewModel.postSetEmail(textState.value) {
                                    component.onBack()
                                }
                            }
                        } else {
                            viewModel.viewModelScope.launch {
                                delay(2000)
                                withContext(Dispatchers.Main) {
                                    if (viewModel.action.value == "login" && UserData.token == "") {
                                        goToLogin(true)
                                    } else {
                                        component.onBack()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
