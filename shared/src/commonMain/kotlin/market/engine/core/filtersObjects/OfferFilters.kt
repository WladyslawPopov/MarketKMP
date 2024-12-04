package market.engine.core.filtersObjects

import market.engine.core.baseFilters.Filter
import market.engine.core.types.LotsType

object OfferFilters {
    var filtersMyLotsActive = arrayListOf(
        Filter("state", "0", "", null),
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("session_start", "time", "", "lte"),
    )
    var filtersMyLotsUnactive = arrayListOf(
        Filter("state", "1", "", null),
        Filter("with_sales", "", null, null),//0 фильтр
        Filter("without_sales", "", null, null),//0 фильтр
        Filter("category", "1", "", null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
    )
    var filtersMyLotsFuture = arrayListOf(
        Filter("state", "0", "", null),
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("session_start", "time", "", "gt"),
    )
    var filtersMyBidsActive = arrayListOf(
        Filter("state", "0", "", null),
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("seller_login", "", null, null),
    )
    var filtersMyBidsUnactive = arrayListOf(
        Filter("state", "1", "", null),
        Filter("category", "1", "", null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("seller_login", "", null, null),
    )
    var filtersFav = arrayListOf(
        Filter("state", "0", "", null),//0 фильтр
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("seller_login", "", null, null),
    )
    var filtersProposeAll = arrayListOf(
        Filter("state", "0", "", null),//0 фильтр
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("seller_login", "", null, null),
    )

    var filtersProposeNeed = arrayListOf(
        Filter("state", "0", "", null),//0 фильтр
        Filter("category", "1", null, null),
        Filter("sale_type", "", null, null),
        Filter("id", "", null, null),
        Filter("search", "", null, null),
        Filter("seller_login", "", null, null),
        Filter("users_to_act_on_price_proposals", "", "", null),
    )

    fun clearTypeFilter(type: LotsType) {
        when(type){
            LotsType.MYLOT_ACTIVE ->{
               filtersMyLotsActive = arrayListOf(
                   Filter("state", "0", "", null),
                   Filter("category", "1", null, null),
                   Filter("sale_type", "", null, null),
                   Filter("id", "", null, null),
                   Filter("search", "", null, null),
                   Filter("session_start", "time", "", "lte"),
               )
            }
            LotsType.MYLOT_UNACTIVE ->{
                filtersMyLotsUnactive = arrayListOf(
                    Filter("state", "1", "", null),
                    Filter("with_sales", "", null, null),//0 фильтр
                    Filter("without_sales", "", null, null),//0 фильтр
                    Filter("category", "1", "", null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                )
            }
            LotsType.MYLOT_FUTURE ->{

                filtersMyLotsFuture = arrayListOf(
                    Filter("state", "0", "", null),
                    Filter("category", "1", null, null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("session_start", "time", "", "gt"),
                )
            }
            LotsType.MYBIDLOTS_ACTIVE ->{
                filtersMyBidsActive = arrayListOf(
                    Filter("state", "0", "", null),
                    Filter("category", "1", null, null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("seller_login", "", null, null),
                )
            }
            LotsType.MYBIDLOTS_UNACTIVE ->{
                filtersMyBidsUnactive = arrayListOf(
                    Filter("state", "1", "", null),
                    Filter("category", "1", null, null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("seller_login", "", null, null),
                )
            }
            LotsType.FAVORITES ->{

                filtersFav = arrayListOf(
                    Filter("state", "0", "", null),//0 фильтр
                    Filter("category", "1", null, null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("seller_login", "", null, null),
                )
            }
            LotsType.ALL_PROPOSAL ->{
                filtersProposeAll = arrayListOf(
                    Filter("state", "0", "", null),//0 фильтр
                    Filter("category", "1", null, null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("seller_login", "", null, null),
                )
            }
            LotsType.NEED_RESPOSE ->{
                filtersProposeNeed = arrayListOf(
                    Filter("state", "0", "", null),//0 фильтр
                    Filter("category", "1", null, null),
                    Filter("sale_type", "", null, null),
                    Filter("id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("seller_login", "", null, null),
                    Filter("users_to_act_on_price_proposals", "", "", null),
                )
            }
        }
    }

    fun addByTypeFilter(type: LotsType) : ArrayList<Filter> {
        return when(type){
            LotsType.MYLOT_ACTIVE ->{
                filtersMyLotsActive
            }
            LotsType.MYLOT_UNACTIVE ->{
                filtersMyLotsUnactive
            }
            LotsType.MYLOT_FUTURE ->{
                filtersMyLotsFuture
            }
            LotsType.MYBIDLOTS_ACTIVE ->{
                filtersMyBidsActive
            }
            LotsType.MYBIDLOTS_UNACTIVE ->{
                filtersMyBidsUnactive
            }
            LotsType.FAVORITES ->{
                filtersFav
            }
            LotsType.ALL_PROPOSAL ->{
                filtersProposeAll
            }
            LotsType.NEED_RESPOSE ->{
                filtersProposeNeed
            }
        }
    }
}
