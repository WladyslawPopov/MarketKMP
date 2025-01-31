package market.engine.widgets.exceptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.PickerResultLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.decodeToImageBitmap
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.networkObjects.Fields
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.checkboxs.DynamicCheckbox
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun SetUpDynamicFields(
    fields: List<Fields>
){
    fields.forEach { field ->
        when(field.widgetType) {
            "input" -> {
                if(field.choices.isNullOrEmpty()) {
                    DynamicInputField(
                        field = field,
                        Modifier.fillMaxWidth(0.9f)
                    )
                }else{
                    DynamicSelect(field, Modifier.fillMaxWidth(0.9f))
                }
            }

            "password" -> {
                DynamicInputField(
                    field = field,
                    Modifier.fillMaxWidth(0.9f)
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
                    Modifier.fillMaxWidth(0.9f)
                )
            }

            "text_area" -> {
                DynamicInputField(
                    field = field,
                    Modifier.fillMaxWidth(0.9f),
                    singleLine = false
                )
            }
        }
    }
}
