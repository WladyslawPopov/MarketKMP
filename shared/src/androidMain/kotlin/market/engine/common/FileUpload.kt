package market.engine.common

import android.content.Context
import market.engine.core.network.ServerResponse
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.jvm.nio.toByteReadChannel
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.io.asByteChannel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.networkObjects.deserializePayload
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileUpload {

    private val apiService: APIService = getKoin().get()

    private val context = appContext!!

    suspend fun uploadFile(uri: Uri?, fileName: String, mimeType: String, url: String? = ""): ServerResponse<String> {
        return coroutineScope {
            async {
                try {
                    val part = createMultipartBodyPart(uri, fileName, mimeType, url.toString())

                    val response = apiService.uploadFile(part)
                    try {
                        val serializer = ListSerializer(String.serializer())
                        val payload = deserializePayload(response.payload, serializer)
                        return@async ServerResponse(success = payload[0])
                    }catch (e : Exception){
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

    private suspend fun createMultipartBodyPart(uri: Uri?, fileName: String, mimeType: String, url: String): List<PartData> {
        val contentResolver = context.contentResolver
        if (uri != null) {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }

            val filePart = PartData.FileItem(
                { file.inputStream().asInput().asByteChannel().toByteReadChannel() },
                {},
                Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"file\"; filename=\"$fileName\""
                    )
                    append(HttpHeaders.ContentType, mimeType)
                }
            )


            val typePart = createPartFromString()

            return listOf(filePart, typePart)
        }else{

            val file = downloadImageAndSaveAsFile(url)

            val filePart = PartData.FileItem(
                { file.inputStream().asInput().asByteChannel().toByteReadChannel() },
                {},
                Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"file\"; filename=\"$fileName\""
                    )
                    append(HttpHeaders.ContentType, mimeType)
                }
            )


            val typePart = createPartFromString()

            return listOf(filePart, typePart)
        }
    }

    private suspend fun downloadImageAndSaveAsFile(imageUrl: String): File {
        try {

            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()


            val result = context.imageLoader.execute(request)
            if (result !is SuccessResult) {
                throw IOException("Error load URL: $imageUrl")
            }

            val bitmap = result.image.toBitmap()

            val file = createImageFile(appContext!!)

            withContext(Dispatchers.IO) {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                }
            }

            return file
        } catch (e: IOException) {
            throw e
        }
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
