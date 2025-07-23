package market.engine.core.data.filtersObjects

import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.types.ReportPageType

object ReportFilters {
    private var filtersAll = listOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("evaluation", "", null, null),
    )

    private var filtersFromBuyers = listOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("role", "0", "", null),
        Filter("evaluation", "", null, null),
    )

    private var filtersFromSellers = listOf(
        Filter("user_id", "", "", null),
        Filter("direction", "0", "", null),
        Filter("role", "1", "", null),
        Filter("evaluation", "", null, null),
    )

    private var filtersFromUsers = listOf(
        Filter("user_id", "", "", null),
        Filter("direction", "1", "", null),
        Filter("evaluation", "", null, null),
    )

    fun getByTypeFilter(type: ReportPageType) : List<Filter> {
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
