package application.market.auction_mobile.business.filtersObjects

import application.market.auction_mobile.business.baseFilters.Filter

object ReportFilters {
    var filtersAll = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("evaluation", "", null, null),
    )

    var filtersFromBuyers = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("role", "0", "", null),
        Filter("evaluation", "", null, null),
    )

    var filtersFromSellers = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("role", "1", "", null),
        Filter("evaluation", "", null, null),
    )

    var filtersFromUsers = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "1", "", null),
        Filter("evaluation", "", null, null),
    )
}
