package market.engine.fragments.root.main.createOffer

import androidx.lifecycle.SavedStateHandle
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonPrimitive
import market.engine.common.getFileUpload
import market.engine.core.data.constants.MAX_IMAGE_COUNT
import market.engine.core.data.constants.errorToastItem
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.PhotoSave
import market.engine.core.data.items.PhotoTemp
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.ServerErrorException
import market.engine.core.network.ServerResponse
import market.engine.core.utils.getSavedStateFlow
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel
import org.jetbrains.compose.resources.getString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PhotoTempViewModel(
    val type: CreateOfferType,
    savedStateHandle: SavedStateHandle
) : CoreViewModel(savedStateHandle){

    private val _deleteImages = savedStateHandle.getSavedStateFlow(
        scope,
        "deleteImages",
        emptyList(),
        ListSerializer(JsonPrimitive.serializer())
    )
    val deleteImages = _deleteImages.state

    private val _responseImages = savedStateHandle.getSavedStateFlow(
        scope,
        "responseImages",
        emptyList(),
        ListSerializer(PhotoSave.serializer())
    )
    val responseImages = _responseImages.state

    @OptIn(ExperimentalUuidApi::class)
    fun getImages(pickImagesRaw : PlatformFiles) {
        val photos = pickImagesRaw.map { file ->
            PhotoTemp(
                file = file,
                id = Uuid.random().toString(),
                uri = file.path
            )
        }

        _responseImages.value = buildList {
            addAll(_responseImages.value)
            photos.forEach {
                uploadPhotoTemp(it)
                if (size < MAX_IMAGE_COUNT) {
                    add(
                        PhotoSave(
                            id = it.id,
                            uri = it.uri,
                            tempId = it.tempId,
                            url = it.url,
                            rotate = it.rotate
                        )
                    )
                }
            }
        }
    }

    fun setImages(images: List<PhotoSave>) {
        _responseImages.value = images.map {
            it.copy()
        }
    }

    fun setDeleteImages(item : PhotoSave) {
        if (type == CreateOfferType.EDIT || type == CreateOfferType.COPY && item.url != null) {
            val id = item.id?.last()
             if(id != null) {
                _deleteImages.value += JsonPrimitive(id.toString())
            }
        }

        _responseImages.update { oldList ->
            val newList = oldList.toMutableList()
            newList.remove(item)
            newList.map {
                it.copy()
            }
        }
    }

    fun rotatePhoto(item : PhotoSave) {
        _responseImages.update { image ->
            image.map {
                if (it.id == item.id) {
                    it.copy(rotate = it.rotate + 90)
                } else {
                    it.copy()
                }
            }

        }
    }

    fun openPhoto(item : PhotoSave) {
        val i = item
        printLogD("Open photo", i.toString())
    }

    fun uploadPhotoTemp(item : PhotoTemp) {
        scope.launch {
            val res =  withContext(Dispatchers.IO) { uploadFile(item) }

            if (res.success != null) {
                if (res.success?.tempId?.isNotBlank() == true) {
                    item.uri = res.success?.uri
                    item.tempId = res.success?.tempId

                    _responseImages.update { list ->
                        list.map { oldItem ->
                            if (oldItem.id == item.id) {
                                PhotoSave(
                                    id = item.id,
                                    uri = item.uri,
                                    tempId = item.tempId,
                                    url = item.url,
                                    rotate = item.rotate
                                )
                            } else {
                                oldItem.copy()
                            }
                        }
                    }
                }else{
                    showToast(
                        errorToastItem.copy(
                            message = res.error?.humanMessage ?: getString(strings.failureUploadPhoto)
                        )
                    )
                    setDeleteImages(
                        PhotoSave(
                            id = item.id,
                            uri = item.uri,
                            tempId = item.tempId,
                            url = item.url,
                            rotate = item.rotate
                        )
                    )
                }
            } else {
                showToast(
                    errorToastItem.copy(
                        message = res.error?.humanMessage ?: getString(strings.failureUploadPhoto)
                    )
                )
            }
        }
    }

    private suspend fun uploadFile(photoTemp: PhotoTemp) : ServerResponse<PhotoSave> {
        try {
            val res = withContext(Dispatchers.IO) {
                getFileUpload(photoTemp)
            }

            val cleanedSuccess = res.success?.trimStart('[')?.trimEnd(']')?.replace("\"", "")
            photoTemp.tempId = cleanedSuccess

            return ServerResponse(
                PhotoSave(
                    id = photoTemp.id,
                    uri = photoTemp.uri,
                    tempId = photoTemp.tempId,
                    url = photoTemp.url,
                    rotate = photoTemp.rotate
                )
            )
        } catch (e : ServerErrorException){
            onError(e)
            return ServerResponse(error = e)
        }catch (e : Exception){
            onError(ServerErrorException(e.message ?: "", ""))
            return ServerResponse(error = ServerErrorException(errorCode = e.message ?: ""))
        }
    }
}
