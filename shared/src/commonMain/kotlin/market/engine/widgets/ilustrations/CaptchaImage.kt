package market.engine.widgets.ilustrations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import io.ktor.util.decodeBase64Bytes
import market.engine.common.decodeToImageBitmap

@Composable
fun CaptchaImage(
    captchaImage: String
) {
    val bitmap = captchaImage.substring(24).decodeBase64Bytes()
    val imageBitmap = decodeToImageBitmap(bitmap)

    Image(
        BitmapPainter(imageBitmap),
        contentDescription = null,
        modifier = Modifier
            .width(250.dp)
            .height(80.dp)
    )
}
