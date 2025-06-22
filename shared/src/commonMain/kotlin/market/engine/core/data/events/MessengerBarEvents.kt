package market.engine.core.data.events

import io.github.vinceglb.filekit.core.PlatformFiles
import market.engine.core.data.items.PhotoTemp

interface MessengerBarEvents {
    fun getImages(images : PlatformFiles)
    fun deleteImage(image : PhotoTemp)
    fun onTextChanged(text : String)
    fun sendMessage()
}
