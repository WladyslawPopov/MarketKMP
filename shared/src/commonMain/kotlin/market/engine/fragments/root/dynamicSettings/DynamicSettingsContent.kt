package market.engine.fragments.root.dynamicSettings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.AnnotatedString
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.SetUpDynamicFields
import market.engine.fragments.base.screens.OnError
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
    val isLoading by viewModel.isShowProgress.collectAsState()
    val err by viewModel.errorMessage.collectAsState()
    val settingsType = model.settingsType
    val code = model.code

    val pageState by viewModel.dynamicSettingsState.collectAsState()

    val blocList by viewModel.blocList.collectAsState()

    val focusManager = LocalFocusManager.current

    val toastItem by viewModel.toastItem.collectAsState()

    val error: (@Composable () -> Unit)? = remember(err) {
        if (err.humanMessage.isNotBlank()) {
            {
                OnError(err) {
                    viewModel.setUpPage()
                }
            }
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
                data = pageState.appBarState
            ){
                TextAppBar(pageState.titleText)
            }
        },
        modifier = Modifier.background(colors.primaryColor).pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus()
            }
        }.fillMaxSize(),
        isLoading = isLoading,
        onRefresh = {
            viewModel.setUpPage()
        },
        error = error,
        toastItem = toastItem
    ) { contentPadding ->
        LazyColumnWithScrollBars(
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            when (settingsType) {
                "app_settings" -> {
                    item {
                        AppSettingsContent {
                            viewModel.changeTheme(it)
                        }
                    }
                }

                "set_about_me" -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            pageState.fields.find { it.widgetType == "text_area" }?.let {
                                DescriptionTextField(it){ description ->
                                    viewModel.setDescription(description)
                                }
                            }

                            AcceptedPageButton(
                                stringResource(strings.actionChangeLabel),
                                enabled = !isLoading
                            ) {
                                viewModel.postSubmit()
                            }
                        }
                    }
                }

                "set_vacation" -> {
                    item {
                        VacationSettingsContent(pageState.fields, onValueChange = {
                            viewModel.setNewFields(it)
                        }) {
                            viewModel.postSubmit()
                        }
                    }
                }

                "set_bidding_step" -> {
                    item {
                        BiddingStepSettingsContent(pageState.fields) {
                            viewModel.postSubmit()
                        }
                    }
                }

                "set_auto_feedback" -> {
                    item {
                        AutoFeedbackSettingsContent(
                            pageState.fields,
                            onValueChange = {
                                viewModel.setNewFields(it)
                            }
                        ) {
                            viewModel.postSubmit()
                        }
                    }
                }

                "set_watermark" -> {
                    item {
                        WatermarkAndBlockRatingContent(true) { isEnabled ->
                            if (!isEnabled) {
                                viewModel.disabledWatermark()
                            } else {
                                viewModel.enabledWatermark()
                            }
                        }
                    }
                }

                "set_address_cards" -> {
                    item {
                        HeaderAlertText(
                            rememberRichTextState().setHtml(
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
                }

                "add_to_seller_blacklist", "add_to_buyer_blacklist", "add_to_whitelist" -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            HeaderAlertText(
                                rememberRichTextState().setHtml(
                                    pageState.titleText
                                ).annotatedString
                            )

                            SetUpDynamicFields(pageState.fields, showRating = true){
                                viewModel.setNewFields(it)
                            }

                            AcceptedPageButton(
                                stringResource(strings.actionAddEnterLabel),
                                enabled = !isLoading
                            ) {
                                viewModel.postSubmit()
                            }

                            BlocListContent(
                                blocList = blocList
                            ) { id ->
                                viewModel.deleteFromBlocList(id)
                            }
                        }
                    }
                }

                "set_block_rating" ->{
                    item {
                        WatermarkAndBlockRatingContent(false) { isEnabled ->
                            if (!isEnabled) {
                                viewModel.disabledBlockRating()
                            } else {
                                viewModel.enabledBlockRating()
                            }
                        }
                    }
                }

                "cancel_all_bids" ->{
                    item {
                        CancelAllBidsContent { field ->
                            viewModel.cancelAllBids(field)
                        }
                    }
                }

                "remove_bids_of_users" -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            val field = pageState.fields.find { it.key == "bidders" }

                            if (field != null) {
                                DynamicCheckboxGroup(
                                    field,
                                    showRating = true
                                ){
                                    viewModel.setNewFields(it)
                                }

                                AcceptedPageButton(
                                    stringResource(strings.actionDelete)
                                )
                                {
                                    viewModel.removeBidsOfUser()
                                }
                            }
                        }
                    }
                }

                // set_login, set_email, set/reset_password, set_phone,
                // set_message_to_buyer, set_outgoing_address
                else -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
                        ) {
                            if (pageState.errorMessage != null) {
                                if (settingsType == "set_login") {
                                    Text(
                                        text = pageState.errorMessage?.first ?: AnnotatedString(""),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.black
                                    )

                                    Text(
                                        pageState.errorMessage?.second!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.black
                                    )
                                }
                            } else {
                                val richTextState = rememberRichTextState()

                                HeaderAlertText(
                                    richTextState.setHtml(pageState.titleText).annotatedString
                                )

                                SetUpDynamicFields(pageState.fields, code){
                                    viewModel.setNewFields(it)
                                }

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
