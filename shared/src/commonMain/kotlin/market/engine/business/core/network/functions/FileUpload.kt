package application.market.agora.business.core.network.functions

import market.engine.business.core.network.APIService
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData

class FileUpload(private val apiService: APIService) {



//    suspend fun uploadFile(uri: Uri?, fileName: String, mimeType: String, context: Context, url: String? = ""): ServerResponse<String> {
//        return coroutineScope {
//            async {
//                try {
//                    val part = createMultipartBodyPart(context, uri, fileName, mimeType, url.toString())
//
//                    val response: AppResponse = apiService.uploadFile(part)
//                    try {
//                        val payload = deserializePayload<ArrayList<String>>(response.payload)
//                        return@async ServerResponse(success = payload[0])
//                    }catch (e : Exception){
//                        throw  ServerErrorException(response.errorCode.toString(),response.humanMessage.toString())
//                    }
//                } catch (e: ServerErrorException) {
//                    ServerResponse(error = e)
//                } catch (e: Exception) {
//                    return@async ServerResponse(
//                        error = ServerErrorException(e.message.toString(), "")
//                    )
//                }
//            }.await()
//        }
//    }

//    private fun createMultipartBodyPart(context: Context, uri: Uri?, fileName: String, mimeType: String, url: String): List<PartData> {
//        val contentResolver = context.contentResolver
//        if (uri != null) {
//            val inputStream = contentResolver.openInputStream(uri)
//            val file = File(context.cacheDir, fileName)
//            file.outputStream().use { outputStream ->
//                inputStream?.copyTo(outputStream)
//            }
//
//            val filePart = PartData.FileItem(
//                { file.inputStream().asInput() },
//                {},
//                Headers.build {
//                    append(
//                        HttpHeaders.ContentDisposition,
//                        "form-data; name=\"file\"; filename=\"$fileName\""
//                    )
//                    append(HttpHeaders.ContentType, mimeType)
//                }
//            )
//
//
//            val typePart = createPartFromString()
//
//            return listOf(filePart, typePart)
//        }else{
//
//            //val file = downloadImageAndSaveAsFile(context, url)
//
//            val filePart = PartData.FileItem(
//                { file.inputStream().asInput() },
//                {},
//                Headers.build {
//                    append(
//                        HttpHeaders.ContentDisposition,
//                        "form-data; name=\"file\"; filename=\"$fileName\""
//                    )
//                    append(HttpHeaders.ContentType, mimeType)
//                }
//            )
//
//
//            val typePart = createPartFromString()
//
//            return listOf(filePart, typePart)
//        }
//    }

//    private fun downloadImageAndSaveAsFile(context: Context, imageUrl: String): File {
////        try {
////            val bitmap: Bitmap = Picasso.get().load(imageUrl).get()
////            val file = createImageFile(context)
////            val outputStream = FileOutputStream(file)
////            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
////            outputStream.flush()
////            outputStream.close()
////            return file
////        } catch (e: IOException) {
////            throw e
////        }
//    }

//    private fun createImageFile(context: Context): File {
////        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
////        return File.createTempFile(
////            "image_", /* prefix */
////            ".jpg", /* suffix */
////            storageDir /* directory */
////        )
//    }


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
