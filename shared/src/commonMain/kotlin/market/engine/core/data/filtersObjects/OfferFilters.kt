package market.engine.core.data.filtersObjects

import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.types.LotsType

object OfferFilters {

    private val base = listOf(
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
    )

    private var filtersMyLotsActive = listOf(
        Filter("state", "0", "", null),
        Filter("session_start", "time", "", "lte"),
    )

    private var filtersMyLotsUnactive = listOf(
        Filter("state", "1", "", null),
        Filter("with_sales", "", null, null),
        Filter("without_sales", "", null, null),
    )
    private var filtersMyLotsFuture = listOf(
        Filter("state", "0", "", null),
        Filter("session_start", "time", "", "gt"),
    )
    private var filtersMyBidsActive = listOf(
        Filter("state", "0", "", null),
        Filter("seller_login", "", null, null),
    )
    private var filtersMyBidsUnactive = listOf(
        Filter("state", "1", "", null),
        Filter("seller_login", "", null, null),
    )
    private  var filtersFav = listOf(
        Filter("state", "0", "", null),//0 фильтр
        Filter("seller_login", "", null, null),
    )
    private  var filtersProposeAll = listOf(
        Filter("state", "0", "", null),//0 фильтр
        Filter("seller_login", "", null, null),
    )

    private var filtersProposeNeed = listOf(
        Filter("state", "0", "", null),//0 фильтр
        Filter("seller_login", "", null, null),
        Filter("users_to_act_on_price_proposals", "", "", null),
    )

    fun getByTypeFilter(type: LotsType?) : List<Filter> {
        return when (type) {
            LotsType.MY_LOT_ACTIVE -> {
                filtersMyLotsActive + base
            }
            LotsType.MY_LOT_INACTIVE -> {
                filtersMyLotsUnactive + base
            }
            LotsType.MY_LOT_IN_FUTURE -> {
                filtersMyLotsFuture + base
            }
            LotsType.MY_BIDS_ACTIVE -> {
                filtersMyBidsActive + base
            }
            LotsType.MY_BIDS_INACTIVE -> {
                filtersMyBidsUnactive + base
            }
            LotsType.FAVORITES -> {
                filtersFav + base
            }
            LotsType.ALL_PROPOSAL -> {
                filtersProposeAll + base
            }
            LotsType.NEED_RESPONSE -> {
                filtersProposeNeed + base
            }
            else -> {
                base
            }
        }
    }
}
