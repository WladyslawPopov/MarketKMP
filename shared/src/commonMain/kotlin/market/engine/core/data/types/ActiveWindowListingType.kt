package market.engine.core.data.types

import kotlinx.serialization.Serializable

@Serializable
enum class ActiveWindowListingType {
    SEARCH, FILTERS, SORTING, CATEGORY, LISTING, CATEGORY_FILTERS
}
