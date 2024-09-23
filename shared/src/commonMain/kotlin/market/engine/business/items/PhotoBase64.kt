package application.market.agora.business.items

import coil3.Uri


data class PhotoBase64(
    var id: String?= null,
    var uri: Uri? = null,
    var base64: String? = null
)
