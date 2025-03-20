package market.engine.fragments.root.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.common.additionalAuthorizationContent
import market.engine.common.openUrl
import market.engine.common.requestIntegrityTokenAuth
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.fragments.base.BackHandler
import market.engine.widgets.ilustrations.CaptchaView
import market.engine.fragments.base.onError
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginContent(
    component: LoginComponent,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val modelState = component.model.subscribeAsState()
    val model = modelState.value.loginViewModel

    val isLoading = model.isShowProgress.collectAsState()
    val err = model.errorMessage.collectAsState()

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        {
            onError(err) {
                model.onError(ServerErrorException())
            }
        }
    } else {
        null
    }

    val emailTextValue = remember { mutableStateOf(TextFieldValue()) }
    val passwordTextValue = remember { mutableStateOf(TextFieldValue()) }
    val captchaTextValue = remember { mutableStateOf(TextFieldValue()) }
    val captchaImage = remember { mutableStateOf<String?>(null) }
    val captchaKey = remember { mutableStateOf<String?>(null) }
    val isCaptchaVisible = remember { mutableStateOf(false) }

    BackHandler(modelState.value.backHandler){
        component.onBack()
    }

    DisposableEffect(Unit){
        model.updateUserInfo()
        onDispose {  }
    }

    BaseContent(
        modifier = modifier,
        topBar = {
            LoginAppBar(
                title = "",
                modifier = modifier,
                onBeakClick = {
                    component.onBack()
                }
            )
        },
        toastItem = model.toastItem,
        error = error,
        isLoading = isLoading.value,
        onRefresh = {

        }
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .background(color = colors.white)
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
                item {
                    Image(
                        painter = painterResource(drawables.logoMain),
                        contentDescription = "",
                        modifier = Modifier
                            .size(height = 100.dp, width = 300.dp)
                            .padding(dimens.mediumPadding)
                    )
                    Spacer(modifier.height(dimens.mediumSpacer))
                }
                item {
                    CaptchaView(
                        isVisible = isCaptchaVisible.value,
                        captchaImage = captchaImage.value,
                        captchaTextValue = captchaTextValue.value,
                        onCaptchaTextChange = {
                            captchaTextValue.value = it
                        }
                    )
                }
                item {
                    OutlinedTextInputField(
                        value = emailTextValue.value,
                        onValueChange = {
                            emailTextValue.value = it
                        },
                        label = stringResource(strings.promptEmail) + " / " + stringResource(strings.loginParameterName),
                        keyboardType = KeyboardType.Email,
                        isEmail = true
                    )

                    OutlinedTextInputField(
                        value = passwordTextValue.value,
                        onValueChange = {
                            passwordTextValue.value = it
                        },
                        label = stringResource(strings.promptPassword),
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.smallPadding,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButton(
                            strings.forgotPassword
                        ) {
                            component.goToForgotPassword()
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.mediumSpacer,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SimpleTextButton(
                            text = stringResource(strings.enterLogin),
                            textColor = colors.alwaysWhite,
                            backgroundColor = colors.inactiveBottomNavIconColor,
                            textStyle = MaterialTheme.typography.titleMedium
                        ) {
                            requestIntegrityTokenAuth()

                            model.postAuth(
                                emailTextValue.value.text,
                                passwordTextValue.value.text,
                                captchaTextValue.value.text,
                                captchaKey.value,
                                onSuccess = {
                                    isCaptchaVisible.value = false
                                    component.onBack()
                                },
                                onError = { image, key ->
                                    captchaImage.value = image
                                    captchaKey.value = key
                                    isCaptchaVisible.value = true
                                }
                            )
                        }

                        SimpleTextButton(
                            text = stringResource(strings.registration),
                            backgroundColor = colors.steelBlue,
                            textColor = colors.alwaysWhite,
                            textStyle = MaterialTheme.typography.titleMedium
                        ) {
                            component.goToRegistration()
                        }
                    }
                }
                item {
                    additionalAuthorizationContent{ bodyString ->
                        model.postAuthExternal(bodyString){
                            component.onBack()
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding).clickable {
                            openUrl(SAPI.dataPolicyURL)
                        },
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.mediumSpacer,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            stringResource(strings.dataUsagePolicyLabel),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.actionTextColor,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}


