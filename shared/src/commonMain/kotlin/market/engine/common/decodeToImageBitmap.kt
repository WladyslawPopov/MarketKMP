package market.engine.common

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeToImageBitmap(bitmap: ByteArray) : ImageBitmap
