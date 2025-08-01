package market.engine.core.data.events

import io.github.vinceglb.filekit.core.PlatformFiles
import market.engine.core.data.items.PhotoSave

interface MessengerBarEvents {
    fun getImages(images : PlatformFiles)
    fun deleteImage(image : PhotoSave)
    fun onTextChanged(text : String)
    fun sendMessage()
}
