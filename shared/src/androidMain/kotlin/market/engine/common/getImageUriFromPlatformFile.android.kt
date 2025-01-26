package market.engine.common

import io.github.vinceglb.filekit.core.PlatformFile

actual fun getImageUriFromPlatformFile(file: PlatformFile?): String? {
    return file?.uri.toString()
}
