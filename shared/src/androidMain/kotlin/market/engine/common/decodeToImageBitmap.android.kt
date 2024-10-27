package market.engine.common

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeToImageBitmap(bitmap: ByteArray): ImageBitmap {
    val bitmap1 = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)
    return bitmap1.asImageBitmap()
}
