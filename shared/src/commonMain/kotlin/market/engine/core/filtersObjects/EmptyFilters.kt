package market.engine.core.filtersObjects

import market.engine.core.baseFilters.Filter


object EmptyFilters {

    fun getEmpty(): ArrayList<Filter> {
        return arrayListOf(
            Filter("sale_type", "", null, null),//0 фильтр
            Filter("session_end", "", null, null),//1 filter
            Filter("starting_price", "", null, null),//2 filter
            Filter("discount_price", "", null, "gt"),//3 filter
            Filter("current_price", "", null, "gte"),//4 цена от
            Filter("current_price", "", null, "lte"),//5 цена до
            Filter("price_proposal", "", null, "intersect"),
            Filter("region", "", null, null),//6 регион
            Filter("session_start", "", null, null),//7 время начала
            Filter("new", "", null, null),//8 новые лоты за
            Filter("ending", "", null, null),//9 заканчивающиеся в течение
            Filter("new_without_relisted",
                "",
                null,
                null),//10 новые без перевысавленных
            Filter("with_video", "", null, null),
            Filter("with_safe_deal", "", null, null),
            Filter("promo_main_page", "", null, null)
        )
    }

}
