package market.engine.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual fun compressImage(originalBytes: ByteArray, quality: Int): ByteArray {
    val bitmap = BitmapFactory.decodeByteArray(
        originalBytes,
        0,
        originalBytes.size
    ) ?: return originalBytes

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    return outputStream.toByteArray()
}
