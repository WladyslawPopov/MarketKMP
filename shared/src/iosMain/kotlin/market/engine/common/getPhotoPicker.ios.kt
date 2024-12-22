package market.engine.common

var photoPicker: PhotoPicker? = null

actual fun getPhotoPicker(): PhotoPicker {
    return photoPicker!!
}
