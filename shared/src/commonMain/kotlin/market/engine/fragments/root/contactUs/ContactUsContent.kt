package market.engine.fragments.root.contactUs

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.ktor.util.decodeBase64Bytes
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import market.engine.common.decodeToImageBitmap
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.widgets.buttons.SimpleTextButton
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.dropdown_menu.DynamicSelect
import market.engine.fragments.base.BackHandler
import market.engine.fragments.base.onError
import market.engine.widgets.textFields.DynamicInputField
import market.engine.widgets.texts.DynamicLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Composable
fun ContactUsContent(
    component: ContactUsComponent,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    val modelState = component.model.subscribeAsState()
    val model = modelState.value.contactUsViewModel

    val selectedType = modelState.value.selectedType

    val isLoading = model.isShowProgress.collectAsState()
    val err = model.errorMessage.collectAsState()

    val responseGetFields = model.responseGetFields.collectAsState()

    val error: (@Composable () -> Unit)? = if (err.value.humanMessage != "") {
        {
            onError(err) {
                model.onError(ServerErrorException())
                model.getFields()
            }
        }
    } else {
        null
    }

    BackHandler(modelState.value.backHandler){
        component.onBack()
    }

    val dataImage = remember { mutableStateOf("") }

    val launcher = rememberFilePickerLauncher(
        type = PickerType.File(
            extensions = listOf("image/png", "image/jpeg", "application/pdf")
        ),
        mode = PickerMode.Multiple(
            maxItems = 1
        ),
        initialDirectory = "market/temp/"
    ) { files ->
        files?.firstOrNull()?.let {  file->
            val photo = PhotoTemp(
                id = Uuid.random().toString(),
                file = file,
            )
            model.uploadPhotoTemp(photo){ result->
                responseGetFields.value?.fields?.find { it.widgetType == "attachment" }?.data = JsonPrimitive(result.tempId)
                dataImage.value = file.name
            }
        }
    }

    BaseContent(
        modifier = modifier,
        topBar = {
            ContactUsAppBar(
                modifier = modifier,
                onBeakClick = {
                    component.onBack()
                }
            )
        },
        toastItem = model.toastItem,
        error = error,
        isLoading = isLoading.value,
        onRefresh = { model.getFields() }
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                },
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(dimens.mediumPadding),
                verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 item {
                     responseGetFields.value?.fields?.forEach { field ->
                         when(field.widgetType) {
                             "input" -> {
                                 if (UserData.token != "") {
                                     if (field.key == "name") {
                                         field.data = JsonPrimitive(UserData.userInfo?.login)
                                     }
                                     if (field.key == "email") {
                                         field.data = JsonPrimitive(UserData.userInfo?.email)
                                     }
                                 }

                                 if(field.choices.isNullOrEmpty()) {
                                     if(field.key == "email") {
                                         DynamicInputField(
                                             field = field,
                                             enabled = UserData.token == ""
                                         )
                                     }else{
                                         DynamicInputField(
                                             field = field,
                                         )
                                     }
                                 }else{
                                     //selected type
                                     DynamicSelect(
                                         field
                                     )
                                 }
                             }

                             "text_area" -> {
                                 DynamicInputField(
                                     field = field,
                                     singleLine = false
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

                             "file" -> {
                                 Row(
                                     modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                     horizontalArrangement = Arrangement.SpaceEvenly,
                                     verticalAlignment = Alignment.Top
                                 ) {
                                     DynamicLabel(
                                         field.shortDescription ?: field.longDescription ?: "",
                                         isMandatory = false
                                     )

                                     if (dataImage.value.isBlank()) {
                                         Card(
                                             colors = colors.cardColors,
                                             shape = MaterialTheme.shapes.medium,
                                             onClick = {
                                                 launcher.launch()
                                             }
                                         ) {
                                             Column(
                                                 modifier = Modifier.padding(dimens.mediumPadding),
                                                 horizontalAlignment = Alignment.CenterHorizontally,
                                                 verticalArrangement = Arrangement.spacedBy(dimens.mediumPadding)
                                             ) {
                                                 Icon(
                                                     painterResource(drawables.documentIcon),
                                                     contentDescription = null,
                                                     tint = colors.textA0AE,
                                                     modifier = Modifier.size(dimens.mediumIconSize)
                                                 )

                                                 Text(
                                                     stringResource(strings.chooseAction),
                                                     style = MaterialTheme.typography.titleSmall,
                                                     color = colors.grayText
                                                 )
                                             }
                                         }
                                     }else{
                                         Row(
                                             modifier = Modifier.fillMaxWidth().padding(dimens.smallPadding),
                                             horizontalArrangement = Arrangement.SpaceEvenly,
                                             verticalAlignment = Alignment.CenterVertically
                                         ) {
                                             Icon(
                                                 painterResource(drawables.documentIcon),
                                                 contentDescription = null,
                                                 tint = colors.textA0AE,
                                                 modifier = Modifier.size(dimens.smallIconSize)
                                             )

                                             Text(
                                                 dataImage.value,
                                                 style = MaterialTheme.typography.titleSmall,
                                                 color = colors.black,
                                                 modifier = Modifier.fillMaxWidth(0.5f)
                                             )

                                             SmallIconButton(
                                                 drawables.deleteIcon,
                                                 color = colors.negativeRed,
                                                 modifierIconSize = Modifier.size(dimens.smallIconSize),
                                             ){
                                                 dataImage.value = ""
                                                 field.data = null
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }

                 item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
                        horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SimpleTextButton(
                            text = stringResource(strings.actionSend),
                            backgroundColor = colors.inactiveBottomNavIconColor,
                            textStyle = MaterialTheme.typography.titleMedium,
                            textColor = colors.alwaysWhite
                        ){
                            model.postContactUs{
                                component.onBack()
                            }
                        }
                    }
                 }
            }
        }
    }
}
