package market.engine.fragments.root.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import market.engine.core.data.states.SimpleAppBarData
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegistrationContent(
    component: RegistrationComponent,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val modelState = component.model.subscribeAsState()
    val model = modelState.value.regViewModel

    val fields by model.responseGetRegFields.collectAsState()

    val isLoading by model.isShowProgress.collectAsState()
    val err by model.errorMessage.collectAsState()
    val toastItem by model.toastItem.collectAsState()
    val showSuccessReg by model.showSuccessReg.collectAsState()

    BackHandler(modelState.value.backHandler){
        component.onBack()
    }

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage != "") {
            { OnError(err) {
                model.getRegFields()
                model.refresh()
            } }
        } else {
            null
        }
    }

    EdgeToEdgeScaffold(
        modifier = modifier.background(colors.primaryColor).fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            },
        topBar = {
            SimpleAppBar(
                data = SimpleAppBarData(
                    onBackClick = {
                        component.onBack()
                    }
                )
            ){
                TextAppBar(stringResource(strings.registration))
            }
        },
        toastItem = toastItem,
        error = error,
        isLoading = isLoading,
        onRefresh = { model.getRegFields() }
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            modifierList = Modifier
                .fillMaxWidth(if(isBigScreen.value) 0.7f else 1f),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            if (!showSuccessReg){
                item {
                    SetUpDynamicFields(fields){
                        model.setNewField(it)
                    }
                }

                item {
                    SimpleTextButton(
                        stringResource(strings.registration),
                        backgroundColor = colors.brightGreen,
                        textStyle = MaterialTheme.typography.titleMedium,
                    ){
                        model.postRegistration()
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
