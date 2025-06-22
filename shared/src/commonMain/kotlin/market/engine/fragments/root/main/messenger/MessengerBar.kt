package market.engine.fragments.root.main.messenger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import market.engine.common.getPermissionHandler
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.events.MessengerBarEvents
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.states.MessengerBarState
import market.engine.widgets.buttons.SmallIconButton
import market.engine.widgets.items.DialogsImgUploadItem
import market.engine.widgets.textFields.TextFieldWithState
import org.jetbrains.compose.resources.stringResource


@Composable
fun MessengerBar(
    data: MessengerBarState,
    events: MessengerBarEvents
) {
    val messageTextState = data.messageTextState
    val imagesUpload = data.imagesUpload
    val isDisabledSendMes = data.isDisabledSendMes
    val isDisabledAddPhotos = data.isDisabledAddPhotos

    val launcher = rememberFilePickerLauncher(
        type = PickerType.Image,
        mode = PickerMode.Multiple(
            maxItems = MAX_IMAGE_COUNT
        ),
        initialDirectory = "market/temp/"
    ) { files ->
        if (files != null) {
            events.getImages(files)
        }
    }

    AnimatedVisibility(
        imagesUpload.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(dimens.mediumPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.smallPadding)
        ) {
            items(
                imagesUpload.size,
                key = {
                    imagesUpload[it].id ?: it
                }
            ) {
                val item = imagesUpload[it]
                DialogsImgUploadItem(
                    item = item,
                    delete = {
                        events.deleteImage(item)
                    }
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AnimatedVisibility(
            !isDisabledAddPhotos,
            enter = expandIn(),
            exit = fadeOut()
        ) {
            SmallIconButton(
                drawables.attachIcon,
                colors.black,
                modifierIconSize = Modifier.size(dimens.mediumIconSize)
            ) {
                if (!getPermissionHandler().checkImagePermissions()) {
                    getPermissionHandler().requestImagePermissions {
                        if (it) {
                            launcher.launch()
                        }
                    }
                } else {
                    launcher.launch()
                }
            }
        }

        TextFieldWithState(
            label = stringResource(strings.messageLabel),
            textState = mutableStateOf(messageTextState),
            modifier = Modifier.fillMaxWidth(0.85f)
                .heightIn(max= 150.dp),
            onTextChange = {
                events.onTextChanged(it)
            },
            readOnly = isDisabledSendMes,
            maxLines = Int.MAX_VALUE,
        )

        SmallIconButton(
            drawables.sendMesIcon,
            colors.black,
            enabled = messageTextState.trim().isNotEmpty(),
            modifierIconSize = Modifier.size(dimens.mediumIconSize),
        ){
            events.sendMessage()
        }
    }
}
