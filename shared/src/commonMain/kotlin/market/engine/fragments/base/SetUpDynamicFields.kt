package market.engine.fragments.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.ilustrations.CaptchaImage
import market.engine.widgets.textFields.DynamicInputField

@Composable
fun SetUpDynamicFields(
    fields: List<Fields>,
    code: String? = null,
    showRating: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(),
){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.smallPadding)
    ) {
        fields.forEach { field ->
            when(field.widgetType) {
                "input" -> {
                    if(field.choices.isNullOrEmpty()) {
                        DynamicInputField(
                            field = field
                        )
                    }else{
                        DynamicSelect(field)
                    }
                }

                "password" -> {
                    DynamicInputField(
                        field = field
                    )
                }

                "hidden" -> {
                    if (field.key == "captcha_image") {
                        val captchaImage = field.data?.jsonPrimitive?.content ?: ""
                        CaptchaImage(captchaImage)
                    }

                    if (field.key == "resetcode" && code != null){
                        field.data = JsonPrimitive(code)
                    }
                }

                "checkbox" -> {
                    DynamicCheckbox(
                        field = field,
                    )
                }

                "text_area" -> {
                    DynamicInputField(
                        field = field,
                        singleLine = false
                    )
                }

                "checkbox_group" -> {
                    DynamicCheckboxGroup(
                        field,
                        showRating = showRating
                    )
                }
                "select" -> {
                    DynamicSelect(
                        field
                    )
                }
            }
        }
    }
}
