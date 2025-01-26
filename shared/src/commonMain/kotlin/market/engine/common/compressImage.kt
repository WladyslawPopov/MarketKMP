package market.engine.common

expect fun compressImage(originalBytes: ByteArray, quality: Int = 50): ByteArray
