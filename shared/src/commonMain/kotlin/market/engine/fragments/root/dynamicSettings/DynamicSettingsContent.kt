package market.engine.fragments.root.dynamicSettings

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.OnError
import market.engine.fragments.root.dynamicSettings.contents.AppSettingsContent
import market.engine.fragments.root.dynamicSettings.contents.AutoFeedbackSettingsContent
import market.engine.fragments.root.dynamicSettings.contents.BiddingStepSettingsContent
import market.engine.fragments.root.dynamicSettings.contents.BlocListContent
import market.engine.fragments.root.dynamicSettings.contents.CancelAllBidsContent
import market.engine.fragments.root.dynamicSettings.contents.DeliveryCardsContent
import market.engine.fragments.root.dynamicSettings.contents.VacationSettingsContent
import market.engine.fragments.root.dynamicSettings.contents.WatermarkAndBlockRatingContent
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.DescriptionTextField
import market.engine.widgets.texts.HeaderAlertText
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicSettingsContent(
    component : DynamicSettingsComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.dynamicSettingsViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()
    val builderDescription by viewModel.builderDescription.collectAsState()
    val errorSettings by viewModel.errorSettings.collectAsState()
    val settingsType = model.settingsType
    val owner = model.owner
    val code = model.code

    val titleText = remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage.isNotBlank()) {
        {
            OnError(err.value) {
                viewModel.onError(ServerErrorException())
                component.updateModel()
            }
        }
    } else {
        null
    }

    val richTextState = rememberRichTextState()

    LaunchedEffect(richTextState){
        snapshotFlow{
            richTextState.annotatedString
        }.collectLatest { _ ->
            val text = KsoupEntities.decodeHtml(richTextState.toHtml())
            builderDescription?.fields?.find { it.key == "description" }?.data = JsonPrimitive(text)
        }
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    BaseContent(
        topBar = {
            DynamicAppBar(
                title = titleText.value,
                navigateBack = {
                    component.onBack()
                },
                onRefresh = {
                    component.updateModel()
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        isLoading = isLoading.value,
        onRefresh = {
            component.updateModel()
        },
        error = error,
        toastItem = viewModel.toastItem
    ) {
        LazyColumnWithScrollBars(
            modifierList = Modifier.pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(if(isBigScreen.value) 0.7f else 1f).padding(dimens.mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.largePadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (settingsType) {
                        "app_settings" -> {
                            titleText.value = stringResource(strings.settingsTitleApp)
                            AppSettingsContent()
                        }

                        "set_about_me" -> {
                            titleText.value =
                                builderDescription?.title ?: builderDescription?.description ?: ""

                            builderDescription?.fields?.find { it.widgetType == "text_area" }?.let {
                                if (richTextState.annotatedString.text == "") {
                                    richTextState.setHtml(it.data?.jsonPrimitive?.content ?: "")
                                }

                                DescriptionTextField(it, richTextState)
                            }

                            AcceptedPageButton(
                                stringResource(strings.actionChangeLabel),
                                enabled = !isLoading.value
                            ) {
                                builderDescription?.fields?.find { it.widgetType == "text_area" }?.let {
                                    it.data = JsonPrimitive(KsoupEntities.decodeHtml(richTextState.toHtml()))
                                }
                                viewModel.postSubmit(settingsType, owner) {
                                    component.onBack()
                                }
                            }
                        }

                        "set_vacation" -> {
                            titleText.value = stringResource(strings.vacationTitle)

                            builderDescription?.fields?.let {
                                VacationSettingsContent(it) {
                                    viewModel.postSubmit(
                                        settingsType,
                                        owner,
                                        component::onBack
                                    )
                                }
                            }
                        }

                        "set_bidding_step" -> {
                            titleText.value = stringResource(strings.settingsBiddingStepsLabel)
                            builderDescription?.fields?.let {
                                BiddingStepSettingsContent(it){
                                    viewModel.postSubmit(settingsType, owner, component::onBack)
                                }
                            }
                        }

                        "set_auto_feedback" -> {
                            titleText.value = stringResource(strings.settingsAutoFeedbacksLabel)
                            builderDescription?.fields?.let {
                                AutoFeedbackSettingsContent(it){
                                    viewModel.postSubmit(settingsType, owner, component::onBack)
                                }
                            }
                        }

                        "set_watermark" -> {
                            titleText.value = stringResource(strings.settingsWatermarkLabel)

                            WatermarkAndBlockRatingContent(true, viewModel)
                        }

                        "set_address_cards" -> {
                            titleText.value = stringResource(strings.addressCardsTitle)

                            HeaderAlertText(
                                rememberRichTextState().setHtml(stringResource(strings.headerDeliveryCardLabel)).annotatedString
                            )

                            DeliveryCardsContent(
                                viewModel.deliveryCardsViewModel,
                                refresh = {
                                    viewModel.deliveryCardsViewModel.refreshCards()
                                }
                            )
                        }

                        "add_to_seller_blacklist", "add_to_buyer_blacklist", "add_to_whitelist" -> {
                            if (builderDescription?.fields?.isNotEmpty() == true) {
                                HeaderAlertText(
                                    rememberRichTextState().setHtml(
                                        builderDescription?.title ?: builderDescription?.description ?: ""
                                    ).annotatedString
                                )

                                builderDescription?.fields?.let {
                                    SetUpDynamicFields(it, showRating = true)
                                }

                                AcceptedPageButton(
                                    stringResource(strings.actionAddEnterLabel),
                                    enabled = !isLoading.value
                                ) {
                                    viewModel.postSubmit(settingsType, owner) {
                                        viewModel.init(settingsType, owner)
                                        focusManager.clearFocus()
                                        viewModel.updateItemTrigger.value++
                                    }
                                }

                                BlocListContent(settingsType, viewModel, viewModel.updateItemTrigger.value)
                            }
                        }

                        "set_block_rating" ->{
                            titleText.value = stringResource(strings.settingsBlockRatingLabel)

                            WatermarkAndBlockRatingContent(false, viewModel)
                        }

                        "cancel_all_bids" ->{
                            titleText.value = stringResource(strings.cancelAllBidsTitle)

                            CancelAllBidsContent(
                                owner ?: 1L,
                                viewModel,
                            ){
                                component.onBack()
                            }
                        }

                        "remove_bids_of_users" -> {
                            titleText.value = stringResource(strings.cancelAllBidsTitle)

                            val field = builderDescription?.fields?.find { it.key == "bidders" }

                            field?.let {
                                DynamicCheckboxGroup(
                                    field,
                                    showRating = true
                                )

                                AcceptedPageButton(
                                    stringResource(strings.actionDelete)
                                ) {
                                    val data = field.data?.jsonArray
                                    field.data = buildJsonArray {
                                        field.choices?.forEachIndexed { index, choices ->
                                            if(data?.get(index) == choices.code){
                                                val exData = choices.extendedFields?.find { it.data != null }?.data
                                                if (exData != null) {
                                                    add(
                                                        buildJsonObject {
                                                            choices.code?.jsonPrimitive?.let { code ->
                                                                put(
                                                                    "code",
                                                                    code
                                                                )
                                                            }
                                                            put("comment", exData)
                                                        }
                                                    )
                                                }else{
                                                    choices.code?.let { code -> add(code) }
                                                }
                                            }
                                        }
                                    }

                                    viewModel.postSubmit(settingsType, owner) {
                                        component.onBack()
                                    }
                                }
                            }
                        }

                        // set_login, set_email, set/reset_password, set_phone,
                        // set_message_to_buyer, set_outgoing_address
                        else -> {
                            if (errorSettings != null) {
                                if (settingsType == "set_login") {
                                    titleText.value = stringResource(strings.setLoginTitle)
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
                            } else {
                                titleText.value =
                                    builderDescription?.title ?: builderDescription?.description
                                            ?: ""

                                if (builderDescription?.fields?.isNotEmpty() == true) {
                                    val headerText = when (settingsType) {
                                        "set_phone" -> {
                                            rememberRichTextState().setHtml(stringResource(strings.htmlVerifyLabel))
                                        }

                                        "set_message_to_buyer" -> {
                                            rememberRichTextState().setHtml(stringResource(strings.headerMessageToBuyersLabel))
                                        }

                                        "set_outgoing_address" -> {
                                            rememberRichTextState().setHtml(stringResource(strings.outgoingAddressHeaderLabel))
                                        }

                                        else -> {
                                            rememberRichTextState().setHtml(
                                                builderDescription?.description ?: ""
                                            )
                                        }
                                    }

                                    if (headerText.annotatedString.text != "") {
                                        HeaderAlertText(
                                            headerText.annotatedString
                                        )
                                    }

                                    builderDescription?.fields?.let {
                                        SetUpDynamicFields(it, code)
                                    }

                                    AcceptedPageButton(
                                        stringResource(strings.actionChangeLabel)
                                    ) {
                                        viewModel.postSubmit(settingsType, owner) {
                                            when (settingsType) {
                                                "set_phone" -> {
                                                    component.goToVerificationPage("set_phone",owner, code)
                                                }

                                                "set_password", "forgot_password", "reset_password" -> {
                                                    if (builderDescription?.body != null) {
                                                        component.goToVerificationPage("set_password",owner, code)
                                                    } else {
                                                        component.onBack()
                                                    }
                                                }

                                                else -> {
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
        }
    }
}
