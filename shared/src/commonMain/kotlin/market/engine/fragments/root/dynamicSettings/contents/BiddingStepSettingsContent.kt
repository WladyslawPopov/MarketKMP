package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.checkboxs.RadioOptionRow
import market.engine.widgets.texts.HeaderAlertText
import org.jetbrains.compose.resources.stringResource

@Composable
fun BiddingStepSettingsContent(
    fields : ArrayList<Fields>,
    onConfirm : () -> Unit,
) {
    val biddingStepField = remember { fields.find { it.key == "bidding_step" } }

    val selectMode = remember {
        mutableStateOf(
            biddingStepField?.data?.jsonPrimitive?.intOrNull ?: -1
        )
    }

    HeaderAlertText(
        rememberRichTextState().setHtml(biddingStepField?.longDescription ?: "").annotatedString
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        if (biddingStepField != null) {
            biddingStepField.choices?.forEach { choice ->
                val string = choice.extendedFields?.firstOrNull()?.let { formatBiddingStepData(it) } ?: ""
                RadioOptionRow(
                    Pair(choice.code?.intOrNull ?: 0, string),
                    selectMode.value,
                ) { isChecked, c ->
                    if (!isChecked) {
                        selectMode.value = c
                        biddingStepField.data = JsonPrimitive(c)
                    }
                }
            }
        }
    }

    AcceptedPageButton(
        strings.actionConfirm
    ) {
        onConfirm()
    }
}

@Composable
fun formatBiddingStepData(field: Fields): String {
    val jsonData = field.data?.jsonPrimitive?.content ?: ""
    val steps = mutableListOf<String>()

    val parsedData = Json.parseToJsonElement(jsonData).jsonArray
    parsedData.forEach { element ->
        val from = element.jsonObject["FROM"]?.jsonPrimitive?.content ?: ""
        val to = element.jsonObject["TO"]?.jsonPrimitive?.content ?: ""
        val step = element.jsonObject["STEP"]?.jsonPrimitive?.content ?: ""
        if (to != "null")
            steps.add("${stringResource(strings.fromAboutParameterName)} $from ${stringResource(strings.currencyCode)} ${stringResource(strings.toAboutParameterName)} $to ${stringResource(strings.currencyCode)}: $step ${stringResource(strings.currencyCode)}")
        else
            steps.add("${stringResource(strings.fromAboutParameterName)} $from ${stringResource(strings.currencyCode)}: $step ${stringResource(strings.currencyCode)}")

    }

    return steps.joinToString("\n")
}

