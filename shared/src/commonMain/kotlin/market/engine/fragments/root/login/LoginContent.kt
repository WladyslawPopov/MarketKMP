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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.delay
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
import market.engine.fragments.base.OnError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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
    val uiState = viewModel.loginContentState.collectAsState()

    val emailText = uiState.value.email
    val passwordText = uiState.value.password
    val captchaText = uiState.value.captcha
    val captchaImage = uiState.value.captchaImage
    val appBarData = uiState.value.appBarData

    val auth2ContentState = uiState.value.auth2ContentState

    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()

    val openBottomSheet = viewModel.openContent.collectAsState()

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage != "") {
            {
                OnError(err.value) {
                    viewModel.onError(ServerErrorException())
                }
            }
        } else {
            null
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(openBottomSheet.value){
        try {
            snapshotFlow {
                openBottomSheet.value
            }.collect {
                if(it){
                    delay(30)
                    scaffoldState.bottomSheetState.expand()
                }else{
                    delay(30)
                    scaffoldState.bottomSheetState.partialExpand()
                    scaffoldState.bottomSheetState.hide()
                }
            }
        }catch (_ : Exception){}
    }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue){
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded){
            viewModel.closeAuth2Content()
        }
    }

    BackHandler(modelState.value.backHandler) {
        component.onBack()
    }

    DisposableEffect(Unit) {
        viewModel.updateUserInfo()
        onDispose { }
    }

    BaseContent(
        modifier = modifier,
        topBar = {
            SimpleAppBar(
                data = appBarData
            )
        },
        toastItem = viewModel.toastItem.value,
        error = error,
        isLoading = isLoading.value,
        onRefresh = {
            viewModel.refreshPage()
        }
    )
    {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContainerColor = colors.primaryColor,
            containerColor = colors.primaryColor,
            contentColor = colors.white,
            sheetPeekHeight = 0.dp,
            sheetSwipeEnabled = true,
            sheetContent = {
                O2AuthContent(
                    auth2ContentState
                )
            },
            modifier = Modifier.fillMaxSize()
        )
        {
            LazyColumnWithScrollBars(
                modifierList = Modifier
                    .background(color = colors.white)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                focusManager.clearFocus()
                            }
                        )
                    }
                    .padding(dimens.mediumPadding),
                state = scrollState,
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
                    CaptchaView(
                        isVisible = captchaImage != null,
                        captchaImage = captchaImage,
                        captchaTextValue = captchaText,
                        onCaptchaTextChange = {
                            viewModel.setCaptchaTextValue(it)
                        }
                    )
                }

                item {
                    OutlinedTextInputField(
                        value = emailText,
                        onValueChange = {
                            viewModel.setEmailTextValue(it)
                        },
                        label = stringResource(strings.promptEmail) +
                                " / " + stringResource(strings.loginParameterName),
                        keyboardType = KeyboardType.Email,
                        isEmail = true,
                    )

                    OutlinedTextInputField(
                        value = passwordText,
                        onValueChange = {
                            viewModel.setPasswordTextValue(it)
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
