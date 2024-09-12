package market.engine.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun toImageBitmap(bitmap: ByteArray?): ImageBitmap? {
    return if (bitmap != null) {
        Image.makeFromEncoded(bitmap).toComposeImageBitmap()
    } else {
        null
    }
}

