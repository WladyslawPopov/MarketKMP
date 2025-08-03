package market.engine.fragments.root.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.ActionButton
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.ilustrations.CaptchaView
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.filterContents.CustomBottomSheet
import market.engine.widgets.rows.LazyColumnWithScrollBars
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
    val model = modelState.value
    val viewModel = model.loginViewModel
    val uiState by viewModel.loginContentState.collectAsState()

    val emailText = uiState.email
    val passwordText = uiState.password
    val captchaText = uiState.captcha
    val captchaImage = uiState.captchaImage
    val appBarData = uiState.appBarData

    val auth2ContentState = viewModel.auth2ContentRepository

    val isLoading by viewModel.isShowProgress.collectAsState()
    val err by viewModel.errorMessage.collectAsState()

    val openBottomSheet by viewModel.openContent.collectAsState()

    val toastItem by viewModel.toastItem.collectAsState()

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            {
                OnError(err) {
                    viewModel.refresh()
                }
            }
        } else {
            null
        }
    }

    DisposableEffect(Unit) {
        viewModel.updateUserInfo()
        onDispose { }
    }

    EdgeToEdgeScaffold(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    focusManager.clearFocus()
                }
            )
        }.fillMaxSize(),
        topBar = {
            SimpleAppBar(
                data = appBarData
            )
        },
        toastItem = toastItem,
        error = error,
        isLoading = isLoading,
        onRefresh = {
            viewModel.refreshPage()
        }
    )
    { contentPadding ->
        CustomBottomSheet(
            initValue = openBottomSheet,
            contentPadding = contentPadding,
            onClosed = {
                viewModel.closeAuth2Content()
            },
            sheetContent = {
                O2AuthContent(
                    auth2ContentState,
                )
            }
        ){
            LazyColumnWithScrollBars(
                heightMod = Modifier.background(colors.white)
                    .fillMaxSize(),
                state = scrollState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
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
                    val textValue = remember { mutableStateOf(TextFieldValue(captchaText)) }

                    CaptchaView(
                        isVisible = captchaImage != null,
                        captchaImage = captchaImage,
                        captchaTextValue = textValue.value,
                        onCaptchaTextChange = {
                            textValue.value = it
                            viewModel.setCaptchaTextValue(it.text)
                        }
                    )
                }

                item {
                    val textValueEmail = remember { mutableStateOf(TextFieldValue(emailText)) }
                    val textValuePassword = remember { mutableStateOf(TextFieldValue(passwordText)) }

                    OutlinedTextInputField(
                        value = textValueEmail.value,
                        onValueChange = {
                            textValueEmail.value = it
                            viewModel.setEmailTextValue(it.text)
                        },
                        label = stringResource(strings.promptEmail) +
                                " / " + stringResource(strings.loginParameterName),
                        keyboardType = KeyboardType.Email,
                        isEmail = true,
                    )

                    OutlinedTextInputField(
                        value = textValuePassword.value,
                        onValueChange = {
                            textValuePassword.value = it
                            viewModel.setPasswordTextValue(it.text)
                        },
                        label = stringResource(strings.promptPassword),
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                    )
                }

                item {
                    Row(
                        modifier = Modifier.widthIn(min = 500.dp).padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.smallPadding,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButton(
                            stringResource(strings.forgotPassword)
                        ) {
                            component.goToForgotPassword()
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.widthIn(min = 500.dp).padding(dimens.smallPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.mediumSpacer,
                            Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        SimpleTextButton(
                            text = stringResource(strings.enterLogin),
                            textColor = colors.alwaysWhite,
                            backgroundColor = colors.inactiveBottomNavIconColor,
                            textStyle = MaterialTheme.typography.titleMedium
                        ) {
                            requestIntegrityTokenAuth()

                            viewModel.postAuth()
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
                    additionalAuthorizationContent { bodyString ->
                        viewModel.postAuthExternal(bodyString)
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                openUrl(SAPI.dataPolicyURL)
                            }
                            .widthIn(min = 500.dp)
                            .padding(dimens.mediumPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            dimens.mediumSpacer,
                            Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
