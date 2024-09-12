package market.engine.common

import androidx.compose.ui.graphics.ImageBitmap

expect fun toImageBitmap(bitmap: ByteArray?): ImageBitmap?

