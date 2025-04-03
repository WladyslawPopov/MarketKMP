package market.engine.widgets.ilustrations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.widgets.textFields.OutlinedTextInputField
import org.jetbrains.compose.resources.stringResource

@Composable
fun CaptchaView(
    isVisible: Boolean,
    captchaImage: String?,
    captchaTextValue: TextFieldValue,
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
            if (captchaImage != null) {
                CaptchaImage(captchaImage)

                OutlinedTextInputField(
                    value = captchaTextValue,
                    onValueChange = onCaptchaTextChange,
                    label = stringResource(strings.enterCaptcha),
                )
            }
        }
    }
}
