import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import market.engine.core.data.items.PhotoTemp
import market.engine.core.network.APIService
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import org.koin.mp.KoinPlatform.getKoin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class FileUpload {

    private val apiService: APIService = getKoin().get()

    suspend fun uploadFile(
        photoTemp: PhotoTemp
    ): ServerResponse<String> {
        return try {
            val part = createMultipartBodyPart(photoTemp)
            val response = apiService.uploadFile(part)
            ServerResponse(success = response.payload?.jsonArray?.get(0)?.jsonPrimitive?.content)
        } catch (e: Exception) {
            ServerResponse(error = ServerErrorException(e.message ?: "Unknown error", ""))
        }
    }


    private suspend fun createMultipartBodyPart(
       photoTemp: PhotoTemp
    ): List<PartData> {
        val file = when {
            photoTemp.file?.path != null -> File(photoTemp.file?.path!!)
            photoTemp.url != null -> downloadImageAndSaveAsFile(photoTemp.url!!)
            else -> throw IllegalArgumentException("Either filePath or URL must be provided")
        }

        photoTemp.uri = photoTemp.file?.path

        val filePart = PartData.FileItem(
            { file.inputStream().toByteReadChannel() },
            {},
            io.ktor.http.Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"${photoTemp.file?.name}\"")
                append(HttpHeaders.ContentType, "image/jpeg")
            }
        )

        val typePart = createPartFromString()

        return listOf(filePart, typePart)
    }

    private suspend fun downloadImageAndSaveAsFile(imageUrl: String): File {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(imageUrl).openConnection()
                val tempFile = createImageFile()
                connection.getInputStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } catch (e: IOException) {
                throw IOException("Failed to download image: $imageUrl", e)
            }
        }
    }


    private fun createImageFile(): File {
        val tempDir = System.getProperty("java.io.tmpdir")
        val filePath = Paths.get(tempDir, "image_${System.currentTimeMillis()}.jpg")
        return Files.createFile(filePath).toFile()
    }


    private fun createPartFromString(): PartData.FormItem {
        return PartData.FormItem(
            "attachmentFile",
            {},
            io.ktor.http.Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"type\"")
                append(HttpHeaders.ContentType, "text/plain")
            }
        )
    }
}
