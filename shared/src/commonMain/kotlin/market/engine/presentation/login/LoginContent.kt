package market.engine.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.delay
import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.constants.ThemeResources.colors
import market.engine.core.constants.ThemeResources.dimens
import market.engine.core.constants.ThemeResources.drawables
import market.engine.core.constants.ThemeResources.strings
import market.engine.core.items.ToastItem
import market.engine.core.types.ToastType
import market.engine.presentation.base.BaseContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.exceptions.CaptchaView
import market.engine.widgets.exceptions.onError
import market.engine.widgets.textFields.TextInputField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginContent(
    component: LoginComponent,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val modelState = component.model.subscribeAsState()
    val model = modelState.value.loginViewModel

    val isLoading = model.isShowProgress.collectAsState()
    val err = model.errorMessage.collectAsState()

    val postAuth = model.responseAuth.collectAsState()

    val analyticsHelper : AnalyticsHelper = AnalyticsFactory.createAnalyticsHelper()

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        { onError(err.value) {  } }
    } else {
        null
    }

    val emailTextValue = remember { mutableStateOf(TextFieldValue()) }
    val passwordTextValue = remember { mutableStateOf(TextFieldValue()) }
    val captchaTextValue = remember { mutableStateOf(TextFieldValue()) }
    val isCaptchaVisible = remember { mutableStateOf(false) }

    val successLogin = stringResource(strings.operationSuccess)
    val errorLogin = stringResource(strings.errorLogin)


    LaunchedEffect(postAuth.value) {
        val res = postAuth.value?.result
        if (res != null) {
            if ( res == "SUCCESS") {
                model.userRepository.setToken(postAuth.value?.user ?: 1L, postAuth.value?.token ?: "")

                model.showToast(
                    ToastItem(
                        isVisible = true,
                        message = successLogin,
                        type = ToastType.SUCCESS
                    )
                )
                val events = mapOf(
                    "login_type" to "email",
                    "login_result" to "success",
                    "login_email" to emailTextValue.value.text
                )
                analyticsHelper.reportEvent("login_success",events)
                delay(3000)
                component.onBack()
            } else {
                if ( res == "needs_captcha") {
                    isCaptchaVisible.value = true
                }else{
                    val events = mapOf(
                        "login_type" to "email",
                        "login_result" to "fail",
                        "login_email" to emailTextValue.value.text
                    )
                    analyticsHelper.reportEvent("login_fail",events)

                    model.showToast(
                        ToastItem(
                            message = errorLogin,
                            type = ToastType.ERROR,
                            isVisible = true
                        )
                    )
                }
            }
        }
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
        toastItem = model.toastItem.value,
        error = error,
        isLoading = isLoading.value,
        onRefresh = {  }
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .background(color = colors.white)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(dimens.mediumPadding)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                focusManager.clearFocus()
                            }
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(drawables.logo),
                    contentDescription = "",
                    modifier = Modifier
                        .size(height = 100.dp, width = 300.dp)
                        .padding(dimens.mediumPadding)
                )

                Spacer(modifier.height(dimens.mediumSpacer))

                CaptchaView(
                    isVisible = isCaptchaVisible.value,
                    captchaImage = postAuth.value?.captchaImage ?: "",
                    captchaTextValue = captchaTextValue.value,
                    focusRequester = focusRequester,
                    onCaptchaTextChange = {
                        captchaTextValue.value = it
                    }
                )

                TextInputField(
                    value = emailTextValue.value,
                    onValueChange = {
                        emailTextValue.value = it
                    },
                    label = stringResource(strings.promptEmail) + " / " + stringResource(strings.loginParameterName),
                    keyboardType = KeyboardType.Email,
                    focusRequester = focusRequester,
                    isEmail = true
                )

                TextInputField(
                    value = passwordTextValue.value,
                    onValueChange = {
                        passwordTextValue.value = it
                    },
                    label = stringResource(strings.promptPassword),
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    focusRequester = focusRequester
                )

                Row(
                    modifier = Modifier.padding(dimens.smallPadding),
                    horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SimpleTextButton(
                        text = stringResource(strings.enterLogin),
                        textColor = colors.alwaysWhite,
                        backgroundColor = colors.inactiveBottomNavIconColor,
                    ) {
                        if (emailTextValue.value.text != "" && passwordTextValue.value.text != ""){
                            isCaptchaVisible.value = false
                            component.onLogin(
                                emailTextValue.value.text,
                                passwordTextValue.value.text,
                                captchaTextValue.value.text
                            )
                        }else{
                            model.showToast(
                                ToastItem(
                                    message = errorLogin,
                                    type = ToastType.WARNING,
                                    isVisible = true
                                )
                            )
                        }
                    }

                    SimpleTextButton(
                        text = stringResource(strings.registration),
                        backgroundColor = colors.grayLayout,
                    ){

                    }
                }
            }
        }
    }
}


