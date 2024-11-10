package market.engine.common

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun toImageBitmap(bitmap: ByteArray?): ImageBitmap? {
    return if (bitmap != null) {
        try {
            Image.makeFromEncoded(bitmap).toComposeImageBitmap()
        }catch (e : Exception){
            println(e.message)
            null
        }

    } else {
        null
    }
}

