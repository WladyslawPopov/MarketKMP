package market.engine.core.globalObjects


object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"

//    fun encrypt(input: String, key: String): String {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//        val encryptedBytes = cipher.doFinal(input.toByteArray())
//        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
//    }
//
//    fun decrypt(input: String, key: String): String {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//        val decryptedBytes = cipher.doFinal(Base64.decode(input, Base64.DEFAULT))
//        return String(decryptedBytes)
//    }
}
