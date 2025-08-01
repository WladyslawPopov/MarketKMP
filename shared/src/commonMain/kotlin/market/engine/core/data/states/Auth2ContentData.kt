package market.engine.core.data.states

import kotlinx.serialization.Serializable

@Serializable
data class Auth2ContentData(
    val user: Long = 1L,
    val obfuscatedIdentity: String?= null,
    val lastRequestByIdentity: Int?= null,
    val humanMessage: String? = null,
)
