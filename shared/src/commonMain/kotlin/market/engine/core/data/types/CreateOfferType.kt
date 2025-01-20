package market.engine.core.data.types

import kotlinx.serialization.Serializable

@Serializable
enum class CreateOfferType {
    CREATE, EDIT, COPY, COPY_WITHOUT_IMAGE, COPY_PROTOTYPE
}
