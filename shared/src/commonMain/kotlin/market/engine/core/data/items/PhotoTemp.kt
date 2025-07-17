package market.engine.core.data.items

import io.github.vinceglb.filekit.core.PlatformFile

data class PhotoTemp(
    var id: String? = null,
    var uri: String? = null,
    var tempId: String? = null,
    var url : String? = null,
    var rotate: Int = 0,
    var file : PlatformFile? = null
)
