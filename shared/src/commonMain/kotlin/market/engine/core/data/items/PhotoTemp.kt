package market.engine.core.data.items

import coil3.Uri


data class PhotoTemp(
    var id: String? = null,
    var uri: Uri? = null,
    var tempId: String? = null,
    var url : String? = null,
    var rotate: Int = 0
)
