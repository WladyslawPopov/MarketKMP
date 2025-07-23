package market.engine.fragments.root.verifyPage

import androidx.compose.foundation.background
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
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.states.SimpleAppBarData
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.screens.OnError
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.DynamicLabel
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun VerificationContent(
    component : VerificationComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.verificationViewModel
    val isLoading by viewModel.isShowProgress.collectAsState()

    val err by viewModel.errorMessage.collectAsState()

    val toastItem by viewModel.toastItem.collectAsState()

    val focusManager = LocalFocusManager.current

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage.isNotBlank()) {
            { OnError(err) { viewModel.refresh() } }
        } else {
            null
        }
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    EdgeToEdgeScaffold(
        topBar = {
            SimpleAppBar(
                data = SimpleAppBarData(
                    onBackClick = {
                        component.onBack()
                    }
                )
            ){
                TextAppBar(stringResource(strings.acceptChangesLabel))
            }
        },
        modifier = Modifier.background(colors.primaryColor).pointerInput(Unit){
            detectTapGestures {
                focusManager.clearFocus()
            }
        }.fillMaxSize(),
        isLoading = isLoading,
        onRefresh = {
            viewModel.setPage()
        },
        toastItem = toastItem,
        error = error
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            item {
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
                    stringResource(strings.acceptAction)
                ) {
                    viewModel.postCode(textState.value)
                }
            }
        }
    }
}
