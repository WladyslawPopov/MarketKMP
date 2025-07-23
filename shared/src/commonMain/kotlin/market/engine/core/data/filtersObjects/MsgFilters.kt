package market.engine.core.data.filtersObjects

import market.engine.core.data.baseFilters.Filter

object MsgFilters {
    var filters = listOf(
        Filter("about_offer", "", null, null),//0 фильтр
        Filter("about_order", "", null, null),//1 фильтр
        Filter("object_id", "", null, null),
        Filter("interlocutor_id", "", null, null),
        Filter("interlocutor_login", "", null, null),
    )

//    private var messageSearchFilter = Filter("search", "", null, null)
}
