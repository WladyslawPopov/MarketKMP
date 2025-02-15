package market.engine.core.data.filtersObjects

import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.types.ReportPageType

object ReportFilters {
    private var filtersAll = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("evaluation", "", null, null),
    )

    private var filtersFromBuyers = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("role", "0", "", null),
        Filter("evaluation", "", null, null),
    )

    private var filtersFromSellers = arrayListOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("role", "1", "", null),
        Filter("evaluation", "", null, null),
    )

    private var filtersFromUsers = arrayListOf(
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

    fun getByTypeFilter(type: ReportPageType) : ArrayList<Filter> {
        return when(type){
            ReportPageType.ALL_REPORTS -> {
                filtersAll
            }
            ReportPageType.FROM_BUYERS -> {
                filtersFromBuyers
            }
            ReportPageType.FROM_SELLERS -> {
                filtersFromSellers
            }
            ReportPageType.FROM_USER -> {
                filtersFromUsers
            }
            ReportPageType.ABOUT_ME -> {
                arrayListOf()
            }
        }
    }
}
