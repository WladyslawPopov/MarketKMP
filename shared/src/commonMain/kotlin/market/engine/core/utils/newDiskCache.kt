package market.engine.core.utils

import coil3.disk.DiskCache
import okio.FileSystem

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY)
        .maxSizeBytes(1024L*1024*1024).build()
}
