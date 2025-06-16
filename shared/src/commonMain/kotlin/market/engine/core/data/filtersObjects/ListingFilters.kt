package market.engine.core.data.filtersObjects

import market.engine.core.data.baseFilters.Filter


object ListingFilters {
    fun getEmpty(): List<Filter> {
        return listOf(
            Filter("sale_type", "", null, null),
            Filter("session_end", "", null, null),
            Filter("starting_price", "", null, null),
            Filter("discount_price", "", null, "gt"),
            Filter("current_price", "", null, "gte"),
            Filter("current_price", "", null, "lte"),
            Filter("price_proposal", "", null, "intersect"),
            Filter("region", "", null, null),
            Filter("session_start", "", null, null),
            Filter("new", "", null, null),
            Filter("ending", "", null, null),
            Filter("new_without_relisted", "", null, null),
            Filter("with_video", "", null, null),
            Filter("with_safe_deal", "", null, null),
            Filter("promo_main_page", "", null, null)
        )
    }
}
