package market.engine.core.data.filtersObjects

import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.types.ReportPageType

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

    fun clearTypeFilter(type: ReportPageType) {
        when(type){
            ReportPageType.ALL_REPORTS -> {
                filtersAll = arrayListOf(
                    Filter("user_id", "", "", null),
                    Filter("direction", "0", "", null),
                    Filter("evaluation", "", null, null),
                )
            }
            ReportPageType.FROM_BUYERS -> {
                filtersFromBuyers = arrayListOf(
                    Filter("user_id", "", "", null),
                    Filter("direction", "0", "", null),
                    Filter("role", "0", "", null),
                    Filter("evaluation", "", null, null),
                )
            }
            ReportPageType.FROM_SELLERS -> {
                filtersFromSellers = arrayListOf(
                    Filter("user_id", "", "", null),
                    Filter("direction", "0", "", null),
                    Filter("role", "1", "", null),
                    Filter("evaluation", "", null, null),
                )
            }
            ReportPageType.FROM_USER -> {
                filtersFromUsers = arrayListOf(
                    Filter("user_id", "", "", null),
                    Filter("direction", "1", "", null),
                    Filter("evaluation", "", null, null),
                )
            }
            ReportPageType.ABOUT_ME -> {

            }
        }
    }
}
