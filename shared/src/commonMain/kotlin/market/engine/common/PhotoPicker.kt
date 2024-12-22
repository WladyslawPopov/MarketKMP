package market.engine.common

import market.engine.core.data.items.PhotoTemp

interface PhotoPicker {
    suspend fun pickImagesRaw( maxCount: Int = 8,
                               maxSizeBytes: Long = 10L * 1024L * 1024L): List<PhotoTemp>
}
