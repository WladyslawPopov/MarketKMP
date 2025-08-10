package market.engine.common

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.writePacket
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.utils.nowAsEpochSeconds
import org.koin.mp.KoinPlatform.getKoin
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.coroutines.CoroutineContext

class FileUpload {
    private val apiService: APIService = getKoin().get()

    suspend fun uploadFile(photoTemp: PhotoTemp): ServerResponse<String> {
        println("Starting uploadFile for URI: ${photoTemp.uri} / URL: ${photoTemp.url}")
        return try {
            val parts = createMultipartBodyPart(photoTemp)
                ?: return ServerResponse(error = ServerErrorException("Failed to create multipart body part"))

            println("Prepared multipart body for: URI=${photoTemp.uri}, URL=${photoTemp.url}")

            val response = apiService.uploadFile(parts)
            if (response.payload == null) {
                throw ServerErrorException("No response payload", "Server returned empty response")
            }

            println("Upload successful, server payload: ${response.payload}")
            ServerResponse(success = response.payload.toString())
        } catch (e: ServerErrorException) {
            println("Server error during upload: ${e.humanMessage}")
            e.printStackTrace()
            ServerResponse(error = e)
        } catch (e: Exception) {
            println("Unexpected error during upload: ${e.message}")
            e.printStackTrace()
            ServerResponse(error = ServerErrorException(e.message ?: "Unknown error"))
        }
    }

    @OptIn(ExperimentalForeignApi::class, ExperimentalForeignApi::class)
    private suspend fun createMultipartBodyPart(photoTemp: PhotoTemp): List<PartData>? {
        return withContext(Dispatchers.IO) {
            try {
                val (rawData, fileName) = if (photoTemp.file?.nsUrl != null) {
                    val uri = photoTemp.file?.nsUrl!!
                    photoTemp.uri = uri.path
                    println("Creating multipart part from local/cloud URI: $uri")

                    val fileName = "image_${photoTemp.tempId ?: nowAsEpochSeconds()}.jpg"

                    // If file founded in own place security scope not needed.
                    val normalizedPath = uri.path?.removePrefix("/private")
                    val data = if (normalizedPath?.startsWith(NSHomeDirectory()) == true) {
                        NSData.dataWithContentsOfURL(uri)
                            ?: throw Exception("Failed to load data from URI: $uri")
                    } else {
                        // If file from cloud or sth else palace - request security scoped access.
                        if (!uri.startAccessingSecurityScopedResource()) {
                            throw Exception("Unable to access security scoped resource: $uri")
                        }
                        try {
                            NSData.dataWithContentsOfURL(uri)
                                ?: throw Exception("Failed to load data from URI: $uri")
                        } finally {
                            uri.stopAccessingSecurityScopedResource()
                        }
                    }
                    data to fileName
                } else if (!photoTemp.url.isNullOrBlank()) {
                    // If needed - download file from URL.
                    val url = photoTemp.url!!
                    println("Creating multipart part by downloading from URL: $url")
                    val fileName = "image_${photoTemp.tempId ?: nowAsEpochSeconds()}.jpg"
                    val nsUrl = NSURL.URLWithString(url)
                        ?: throw Exception("Invalid URL string: $url")
                    val data = NSData.dataWithContentsOfURL(nsUrl)
                        ?: throw Exception("Failed to download data from URL: $url")
                    data to fileName
                } else {
                    throw IllegalArgumentException("No valid URI or URL specified in PhotoTemp")
                }

                val originalImage = UIImage(data = rawData)
                val fixedImage = fixImageOrientation(originalImage)
                val nsData = UIImageJPEGRepresentation(fixedImage, 1.0)
                    ?: throw Exception("Failed to get JPEG representation")

                // Convert NSData to (Flow<ByteArray>)
                val byteArrayFlow: Flow<ByteArray> = flow {
                    memScoped {
                        val byteArray = ByteArray(nsData.length.toInt())
                        nsData.getBytes(byteArray.refTo(0).getPointer(this), nsData.length)
                        println("Prepared byte array: size=${byteArray.size}")
                        emit(byteArray)
                    }
                }

                val byteReadChannel = byteArrayFlow.toByteReadChannel(Dispatchers.IO)

                val filePart = PartData.FileItem(
                    { byteReadChannel },
                    {},
                    Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"file\"; filename=\"$fileName\""
                        )
                        append(HttpHeaders.ContentType, "image/jpeg")
                    }
                )

                val typePart = PartData.FormItem(
                    value = "attachmentFile",
                    dispose = {},
                    Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"type\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    }
                )

                listOf(filePart, typePart)
            } catch (e: Exception) {
                println("Error creating multipart body part: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun fixImageOrientation(image: UIImage): UIImage {
        if (image.imageOrientation == platform.UIKit.UIImageOrientation.UIImageOrientationUp) {
            return image
        }

        val rect = image.size.useContents {
            CGRectMake(x = 0.0, y = 0.0, width = this.width, height = this.height)
        }

        UIGraphicsBeginImageContextWithOptions(size = image.size, opaque = false, scale = image.scale)

        image.drawInRect(rect)

        val normalizedImage = UIGraphicsGetImageFromCurrentImageContext()

        UIGraphicsEndImageContext()

        return normalizedImage ?: image
    }

    private fun Flow<ByteArray>.toByteReadChannel(coroutineContext: CoroutineContext): ByteReadChannel {
        val channel = ByteChannel(autoFlush = true)
        CoroutineScope(coroutineContext).launch {
            collect { bytes ->
                channel.writePacket(ByteReadPacket(bytes))
            }
            channel.close()
        }
        return channel
    }
}
