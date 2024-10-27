package market.engine.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun decodeToImageBitmap(bitmap: ByteArray): ImageBitmap {
    val skiaImage = Image.makeFromEncoded(bitmap)
    return skiaImage.toComposeImageBitmap()
}
