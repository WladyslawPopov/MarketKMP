package market.engine.common

import android.content.Context
import market.engine.core.network.ServerResponse
import android.graphics.Bitmap
import android.os.Environment
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.utils.deserializePayload
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileUpload {

    private val apiService: APIService = getKoin().get()

    private val context = appContext!!

    suspend fun uploadFile(photoTemp: PhotoTemp): ServerResponse<String> {
        return coroutineScope {
            async {
                try {
                    val part = createMultipartBodyPart(photoTemp)

                    val response = apiService.uploadFile(part)
                    try {
                        val serializer = ListSerializer(String.serializer())
                        val payload = deserializePayload(response.payload, serializer)
                        return@async ServerResponse(success = payload[0])
                    }catch (_ : Exception){
                        throw  ServerErrorException(response.errorCode.toString(),response.humanMessage.toString())
                    }
                } catch (e: ServerErrorException) {
                    ServerResponse(error = e)
                } catch (e: Exception) {
                    return@async ServerResponse(
                        error = ServerErrorException(e.message.toString(), "")
                    )
                }
            }.await()
        }
    }

    private suspend fun createMultipartBodyPart(photoTemp: PhotoTemp): List<PartData> {
        val file = getSanitizedImageFile(photoTemp)

        val filePart = PartData.FileItem(
            { file.inputStream().toByteReadChannel() },
            { file.delete() },
            Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"${file.name}\"")
                append(HttpHeaders.ContentType, "image/jpeg")
            }
        )

        val typePart = createPartFromString()

        return listOf(filePart, typePart)
    }


    private suspend fun getSanitizedImageFile(photoTemp: PhotoTemp): File {
        val dataSource: Any = if (photoTemp.file?.uri != null) {
            photoTemp.uri = photoTemp.file?.uri.toString()
            photoTemp.file!!.uri
        } else if (!photoTemp.url.isNullOrBlank()) {
            photoTemp.url!!
        } else {
            throw IOException("No valid image source (URI or URL) provided.")
        }

        val request = ImageRequest.Builder(context)
            .data(dataSource)
            .allowHardware(false)
            .build()

        val result = context.imageLoader.execute(request)
        if (result !is SuccessResult) {
            throw IOException("Failed to load image from: $dataSource")
        }

        val bitmap = result.image.toBitmap()
        val file = createImageFile(context)

        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
        }

        return file
    }

    private fun createImageFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "image_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }


    private fun createPartFromString(): PartData.FormItem {
        return PartData.FormItem(
            "attachmentFile",
            {},
            Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"type\"")
                append(HttpHeaders.ContentType, "text/plain")
            }
        )
    }
}
