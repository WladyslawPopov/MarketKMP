package market.engine.fragments.dynamicSettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.decodeToImageBitmap
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.Fields
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.rows.RichTextStyleRow
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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

    val focusManager = LocalFocusManager.current

    val richTextState = rememberRichTextState()

    LaunchedEffect(builderDescription){
        if (builderDescription != null) {
            richTextState.setHtml(
                builderDescription?.fields?.find {
                    it.widgetType == "text_area"
                }?.data?.jsonPrimitive?.content ?: ""
            )
        }
    }

    LaunchedEffect(richTextState){
        snapshotFlow{
            richTextState.annotatedString
        }.collectLatest { _ ->
            val text = KsoupEntities.decodeHtml(richTextState.toHtml())
            builderDescription?.fields?.find { it.widgetType == "text_area" }?.data = JsonPrimitive(text)
        }
    }

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
        LazyColumn(
            modifier = Modifier.pointerInput(Unit){
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }.fillMaxSize().padding(dimens.mediumPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            item {
                when(settingsType) {
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
                                strings.actionChangeLabel
                            ){
                                viewModel.viewModelScope.launch {
                                    val res = viewModel.postSubmit(settingsType)

                                    if (res){
                                        delay(2000)
                                        component.onBack()
                                    }
                                }
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
                    "set_email" -> {
                        titleText.value = builderDescription?.title ?: builderDescription?.description ?: stringResource(strings.editEmailTitle)
                        if (builderDescription?.fields?.isNotEmpty() == true) {
                            DynamicLabel(
                                builderDescription?.description ?: "",
                                false
                            )

                            builderDescription?.fields?.let {
                                SetUpDynamicFields(it)
                            }
                            AcceptedPageButton(
                                strings.actionChangeLabel
                            ){
                                viewModel.viewModelScope.launch {
                                    val res = viewModel.postSubmit(settingsType)

                                    if (res){
                                        delay(2000)
                                        component.onBack()
                                    }
                                }
                            }
                        }
                    }
                    "set_password","forgot_password","reset_password" -> {
                        titleText.value = builderDescription?.title ?: builderDescription?.description ?: ""
                        if (builderDescription?.fields?.isNotEmpty() == true) {
                            DynamicLabel(
                                builderDescription?.description ?: "",
                                false
                            )

                            builderDescription?.fields?.let {
                                SetUpDynamicFields(it)
                            }
                            AcceptedPageButton(
                                strings.actionChangeLabel
                            ){
                                viewModel.viewModelScope.launch {
                                    val res = viewModel.postSubmit(settingsType)
                                    delay(2000)

                                    if (res){
                                        if (builderDescription?.body != null) {
                                            component.goToVerificationPage("set_password")
                                        }else {
                                            component.onBack()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "set_phone" -> {
                        titleText.value = builderDescription?.title ?: builderDescription?.description ?: ""
                        if (builderDescription?.fields?.isNotEmpty() == true) {
                            val htmTextState = rememberRichTextState()

                            htmTextState.setHtml(stringResource(strings.htmlVerifyLabel))

                            Text(
                                htmTextState.annotatedString,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.grayText,
                                modifier = Modifier.background(
                                    color = colors.solidGreen,
                                    shape = MaterialTheme.shapes.medium
                                ).padding(dimens.mediumPadding)
                            )

                            Spacer(modifier = Modifier.height(dimens.mediumPadding))

                            builderDescription?.fields?.let {
                                SetUpDynamicFields(it)
                            }

                            AcceptedPageButton(
                                strings.submitSmsLabel
                            ){
                                viewModel.viewModelScope.launch {
                                    val res = viewModel.postSubmit("set_phone")

                                    if (res){
                                        delay(2000)
                                        component.goToVerificationPage("set_phone")
                                    }
                                }
                            }
                        }
                    }
                    "set_about_me" -> {
                        titleText.value = builderDescription?.title ?: builderDescription?.description ?: ""

                        builderDescription?.fields?.find { it.widgetType == "text_area" }?.let {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(dimens.mediumPadding)
                                    .background(
                                        colors.white,
                                        MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                RichTextStyleRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimens.smallPadding),
                                    state = richTextState,
                                )

                                Spacer(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .fillMaxWidth(0.98f)
                                        .background(colors.grayLayout)
                                )

                                RichTextEditor(
                                    state = richTextState,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 300.dp, max = 500.dp),
                                    label = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            DynamicLabel(
                                                text = it.shortDescription ?: it.longDescription
                                                ?: "",
                                                isMandatory = false
                                            )
                                        }
                                    },
                                    maxLength = it.validators?.firstOrNull()?.parameters?.max ?: 2000,
                                    placeholder = {
                                        Text(
                                            stringResource(strings.descriptionPlaceholderLabel),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.grayText
                                        )
                                    },
                                    colors = RichTextEditorDefaults.richTextEditorColors(
                                        focusedIndicatorColor = colors.transparent,
                                        unfocusedIndicatorColor = colors.transparent,
                                        disabledIndicatorColor = colors.transparent,
                                        errorIndicatorColor = colors.transparent,
                                    )
                                )
                            }
                        }

                        AcceptedPageButton(
                            strings.actionChangeLabel
                        ){
                            viewModel.viewModelScope.launch {
                                val res = viewModel.postSubmit(settingsType)

                                if (res){
                                    delay(2000)
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

@Composable
fun SetUpDynamicFields(
    fields: List<Fields>
){
    fields.forEach { field ->
        when(field.widgetType) {
            "input" -> {
                DynamicInputField(
                    field = field
                )
            }

            "password" -> {
                DynamicInputField(
                    field = field
                )
            }

            "hidden" -> {
                if (field.key == "captcha_image") {
                    val captchaImage = field.data?.jsonPrimitive?.content ?: ""
                    val bitmap = captchaImage.substring(24).decodeBase64Bytes()
                    val imageBitmap = decodeToImageBitmap(bitmap)

                    Image(
                        BitmapPainter(imageBitmap),
                        contentDescription = null,
                        modifier = Modifier
                            .width(250.dp)
                            .height(100.dp)
                    )
                }
            }
        }
    }
}
