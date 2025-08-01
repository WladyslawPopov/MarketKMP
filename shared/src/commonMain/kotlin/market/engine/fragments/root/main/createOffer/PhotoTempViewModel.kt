package market.engine.fragments.root.main.createOffer

import androidx.lifecycle.SavedStateHandle
import io.github.vinceglb.filekit.core.PlatformFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
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
        viewModelScope,
        "deleteImages",
        emptyList(),
        ListSerializer(JsonPrimitive.serializer())
    )
    val deleteImages = _deleteImages.state

    private val _responseImages = savedStateHandle.getSavedStateFlow(
        viewModelScope,
        "responseImages",
        emptyList(),
        ListSerializer(PhotoSave.serializer())
    )
    val responseImages = _responseImages.state

    @OptIn(ExperimentalUuidApi::class)
    fun getImages(pickImagesRaw : PlatformFiles) {
        viewModelScope.launch {
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
    }

    fun setImages(images: List<PhotoSave>) {
        _responseImages.value = images.map {
            it.copy()
        }
    }

    fun setDeleteImages(item : PhotoSave) {
        if (type == CreateOfferType.EDIT || type == CreateOfferType.COPY) {
            if (item.url != null && item.id != null) {
                _deleteImages.value += JsonPrimitive(item.id!!.last().toString())
            }
        }

        _responseImages.update {
            val newList = it.toMutableList()
            newList.remove(item)
            newList
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
        viewModelScope.launch {
            val res = uploadFile(item)

            if (res.success != null) {
                delay(1000)

                if (res.success?.tempId?.isNotBlank() == true) {
                    item.uri = res.success?.uri
                    item.tempId = res.success?.tempId

                    _responseImages.update { list ->
                        list.map {
                            if (it.id == item.id) {
                                PhotoSave(
                                    id = it.id,
                                    uri = it.uri,
                                    tempId = it.tempId,
                                    url = it.url,
                                    rotate = it.rotate
                                )
                            } else {
                                it.copy()
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

            return withContext(Dispatchers.Main) {
                val cleanedSuccess = res.success?.trimStart('[')?.trimEnd(']')?.replace("\"", "")
                photoTemp.tempId = cleanedSuccess
                ServerResponse(PhotoSave(
                    id = photoTemp.id,
                    uri = photoTemp.uri,
                    tempId = photoTemp.tempId,
                    url = photoTemp.url,
                    rotate = photoTemp.rotate
                ))
            }
        } catch (e : ServerErrorException){
            onError(e)
            return withContext(Dispatchers.Main) {
                ServerResponse(error = e)
            }
        }catch (e : Exception){
            onError(ServerErrorException(e.message ?: "", ""))
            return withContext(Dispatchers.Main) {
                ServerResponse(error = ServerErrorException(errorCode = e.message ?: ""))
            }
        }
    }
}
