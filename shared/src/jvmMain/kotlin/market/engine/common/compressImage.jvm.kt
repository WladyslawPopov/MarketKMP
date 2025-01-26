package market.engine.common

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual fun compressImage(originalBytes: ByteArray, quality: Int): ByteArray {
    val inputStream = ByteArrayInputStream(originalBytes)
    val original = ImageIO.read(inputStream) ?: return originalBytes

    val outputStream = ByteArrayOutputStream()
    val writers = ImageIO.getImageWritersByFormatName("jpeg")
    val writer = writers.next()
    val ios = ImageIO.createImageOutputStream(outputStream)
    writer.output = ios

    val param = writer.defaultWriteParam
    if (param.canWriteCompressed()) {
        param.compressionMode = ImageWriteParam.MODE_EXPLICIT
        param.compressionQuality = quality / 100f
    }
    writer.write(null, IIOImage(original, null, null), param)
    writer.dispose()
    ios.close()

    return outputStream.toByteArray()
}
