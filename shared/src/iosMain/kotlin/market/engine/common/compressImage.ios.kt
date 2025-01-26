package market.engine.common

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.UIKit.*
import platform.Foundation.*

actual fun compressImage(
    originalBytes: ByteArray,
    quality: Int
): ByteArray {
    val nsData = originalBytes.toNSData()
    val uiImage = UIImage(data = nsData)
    val jpegQuality = quality.toDouble() / 100.0
    val compressedNSData = UIImageJPEGRepresentation(uiImage, jpegQuality)
        ?: return originalBytes

    return compressedNSData.toByteArray()
}


@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData =
    this.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.toULong())
    }

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    bytes.usePinned {
        memScoped {
            val src = this@toByteArray.bytes
            platform.posix.memcpy(it.addressOf(0), src, size.toULong())
        }
    }
    return bytes
}
