package market.engine.common

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun compressImage(originalBytes: ByteArray, quality: Int): ByteArray {
    val image = Image.makeFromEncoded(originalBytes)
    val data = image.encodeToData(
        EncodedImageFormat.JPEG,
        quality
    ) ?: return originalBytes

    return data.bytes
}
