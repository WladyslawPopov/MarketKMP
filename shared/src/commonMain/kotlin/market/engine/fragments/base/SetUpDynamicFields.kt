package market.engine.fragments.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.decodeToImageBitmap
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.checkboxs.DynamicCheckboxGroup
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.textFields.DynamicInputField

@Composable
fun SetUpDynamicFields(
    fields: List<Fields>,
    modifier: Modifier = Modifier.fillMaxWidth(0.9f),
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
                        showRating = true
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
