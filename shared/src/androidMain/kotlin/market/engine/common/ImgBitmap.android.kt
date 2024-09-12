package market.engine.common

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun toImageBitmap(bitmap: ByteArray?): ImageBitmap? {
    return if (bitmap != null) {
        try {
            val decodedBitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)
            decodedBitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}
