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
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.isBigScreen
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
import market.engine.widgets.filterContents.deliveryCardsContents.DeliveryCardsContent
import market.engine.fragments.root.dynamicSettings.contents.VacationSettingsContent
import market.engine.fragments.root.dynamicSettings.contents.WatermarkAndBlockRatingContent
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.rows.LazyColumnWithScrollBars
import market.engine.widgets.textFields.DescriptionTextField
import market.engine.widgets.texts.HeaderAlertText
import market.engine.widgets.texts.TextAppBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun DynamicSettingsContent(
    component : DynamicSettingsComponent
) {
    val model by component.model.subscribeAsState()
    val viewModel = model.dynamicSettingsViewModel
    val isLoading = viewModel.isShowProgress.collectAsState()
    val err = viewModel.errorMessage.collectAsState()
    val settingsType = model.settingsType
    val code = model.code

    val pageState = viewModel.dynamicSettingsState.collectAsState()

    val blocList = viewModel.blocList.collectAsState()

    val focusManager = LocalFocusManager.current

    val error: (@Composable () -> Unit)? = remember(err.value) {
        if (err.value.humanMessage.isNotBlank()) {
            {
                OnError(err.value) {
                    viewModel.setUpPage()
                }
            }
        } else {
            null
        }
    }

    val richTextState = rememberRichTextState()

    LaunchedEffect(richTextState){
        snapshotFlow{
            richTextState.annotatedString
        }.collectLatest { _ ->
            val text = KsoupEntities.decodeHtml(richTextState.toHtml())
            pageState.value.fields.find { it.key == "description" }?.data = JsonPrimitive(text)
        }
    }

    LaunchedEffect(pageState.value){
        richTextState.setHtml(
            pageState.value.fields.find { it.widgetType == "text_area" }?.
            data?.jsonPrimitive?.content ?: ""
        )
    }

    BackHandler(model.backHandler){
        component.onBack()
    }

    BaseContent(
        topBar = {
            SimpleAppBar(
                data = pageState.value.appBarState
            ){
                TextAppBar(pageState.value.titleText)
            }
        },
        modifier = Modifier.fillMaxSize(),
        isLoading = isLoading.value,
        onRefresh = {
            viewModel.setUpPage()
        },
        error = error,
        toastItem = viewModel.toastItem.value
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
                    modifier = Modifier
                        .fillMaxWidth(if(isBigScreen.value) 0.7f else 1f)
                        .padding(dimens.mediumPadding),
                    verticalArrangement = Arrangement.spacedBy(dimens.largePadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    when (settingsType) {
                        "app_settings" -> {
                            AppSettingsContent {
                                viewModel.changeTheme(it)
                            }
                        }

                        "set_about_me" -> {
                            pageState.value.fields.find { it.widgetType == "text_area" }?.let {
                                DescriptionTextField(it, richTextState)
                            }

                            AcceptedPageButton(
                                stringResource(strings.actionChangeLabel),
                                enabled = !isLoading.value
                            ) {
                                viewModel.postSubmit()
                            }
                        }

                        "set_vacation" -> {
                            VacationSettingsContent(pageState.value.fields) {
                                viewModel.postSubmit()
                            }
                        }

                        "set_bidding_step" -> {
                            BiddingStepSettingsContent(pageState.value.fields){
                                viewModel.postSubmit()
                            }
                        }

                        "set_auto_feedback" -> {
                            AutoFeedbackSettingsContent(pageState.value.fields){
                                viewModel.postSubmit()
                            }
                        }

                        "set_watermark" -> {
                            WatermarkAndBlockRatingContent(true){ isEnabled ->
                                if (!isEnabled) {
                                    viewModel.disabledWatermark()
                                } else {
                                    viewModel.enabledWatermark()
                                }
                            }
                        }

                        "set_address_cards" -> {
                            HeaderAlertText(
                                rememberRichTextState().
                                setHtml(
                                    stringResource(strings.headerDeliveryCardLabel)
                                ).annotatedString
                            )

                            DeliveryCardsContent(
                                viewModel.deliveryCardsViewModel,
                                refresh = {
                                    viewModel.deliveryCardsViewModel.refreshCards()
                                }
                            )
                        }

                        "add_to_seller_blacklist", "add_to_buyer_blacklist", "add_to_whitelist" -> {
                            HeaderAlertText(
                                rememberRichTextState().setHtml(
                                    pageState.value.titleText
                                ).annotatedString
                            )

                            SetUpDynamicFields(pageState.value.fields, showRating = true)

                            AcceptedPageButton(
                                stringResource(strings.actionAddEnterLabel),
                                enabled = !isLoading.value
                            ) {
                                viewModel.postSubmit()
                            }

                            BlocListContent(
                                blocList = blocList.value
                            ){ id ->
                                viewModel.deleteFromBlocList(id)
                            }
                        }

                        "set_block_rating" ->{
                            WatermarkAndBlockRatingContent(false){ isEnabled ->
                                if (isEnabled) {
                                    viewModel.disabledBlockRating()
                                } else {
                                    viewModel.enabledBlockRating()
                                }
                            }
                        }

                        "cancel_all_bids" ->{
                            CancelAllBidsContent{ field ->
                                viewModel.cancelAllBids(field)
                            }
                        }

                        "remove_bids_of_users" -> {
                            val field = pageState.value.fields.find { it.key == "bidders" }

                            if (field != null) {
                                DynamicCheckboxGroup(
                                    field,
                                    showRating = true
                                )

                                AcceptedPageButton(
                                    stringResource(strings.actionDelete)
                                )
                                {
                                    viewModel.removeBidsOfUser()
                                }
                            }
                        }

                        // set_login, set_email, set/reset_password, set_phone,
                        // set_message_to_buyer, set_outgoing_address
                        else -> {
                            if (pageState.value.errorMessage != null) {
                                if (settingsType == "set_login") {
                                    Text(
                                        pageState.value.errorMessage?.first!!,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.black
                                    )

                                    Text(
                                        pageState.value.errorMessage?.second!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.black
                                    )
                                }
                            } else {
                                HeaderAlertText(
                                    richTextState.setHtml(pageState.value.titleText).annotatedString
                                )

                                SetUpDynamicFields(pageState.value.fields, code)

                                AcceptedPageButton(
                                    stringResource(strings.actionChangeLabel)
                                ) {
                                    viewModel.postSubmit()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
