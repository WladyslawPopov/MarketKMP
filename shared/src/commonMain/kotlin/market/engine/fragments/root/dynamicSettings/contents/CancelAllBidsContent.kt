package market.engine.fragments.root.dynamicSettings.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.network.networkObjects.Fields
import market.engine.core.network.networkObjects.Parameters
import market.engine.core.network.networkObjects.Validator
import market.engine.widgets.buttons.AcceptedPageButton
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.HeaderAlertText
import org.jetbrains.compose.resources.stringResource

@Composable
fun CancelAllBidsContent(
    success: (Fields) -> Unit
) {
    val commentLabel = stringResource(strings.cancelAllBidsCommentLabel)

    val field = remember {
        Fields(
            longDescription = commentLabel,
            validators = listOf(
                Validator(
                    parameters = Parameters(
                        max = 250
                    )
                )
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        HeaderAlertText(
            rememberRichTextState().setHtml(stringResource(strings.cancelAllBidsHeader)).annotatedString
        )

        DynamicInputField(
            field = field,
            singleLine = false
        ){
            field.data = it.data
        }

        AcceptedPageButton(
            stringResource(strings.actionConfirm),
        ) {
            success(field)
        }
    }
}
