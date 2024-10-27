package market.engine.core.util


object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"

//    fun encrypt(input: String, key: String): String {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//        val encryptedBytes = cipher.doFinal(input.toByteArray())
//        return market.engine.core.util.Base64.encodeToString(encryptedBytes, market.engine.core.util.Base64.DEFAULT)
//    }
//
//    fun decrypt(input: String, key: String): String {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//        val decryptedBytes = cipher.doFinal(market.engine.core.util.Base64.decode(input, market.engine.core.util.Base64.DEFAULT))
//        return String(decryptedBytes)
//    }
}
