package market.engine.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun toImageBitmap(bitmap: ByteArray?): ImageBitmap? {
        return if (bitmap != null) {
            try {
                val image = Image.makeFromEncoded(bitmap)
                image.toComposeImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }
