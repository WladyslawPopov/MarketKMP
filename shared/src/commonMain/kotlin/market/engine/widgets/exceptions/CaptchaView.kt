package market.engine.widgets.exceptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.ktor.util.decodeBase64Bytes
import market.engine.common.decodeToImageBitmap
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.textFields.TextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun CaptchaView(
    isVisible: Boolean,
    captchaImage: String,
    captchaTextValue: TextFieldValue,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onCaptchaTextChange: (TextFieldValue) -> Unit
) {
    AnimatedVisibility(
        isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.smallPadding),
            modifier = Modifier.padding(dimens.mediumPadding)
        ) {
            val bitmap = captchaImage.substring(24).decodeBase64Bytes()
            val imageBitmap = decodeToImageBitmap(bitmap)

            Image(
                BitmapPainter(imageBitmap),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .height(100.dp)
            )

            TextInputField(
                value = captchaTextValue,
                onValueChange = onCaptchaTextChange,
                label = stringResource(strings.enterCaptcha),
                focusRequester = focusRequester
            )
        }
    }
}
