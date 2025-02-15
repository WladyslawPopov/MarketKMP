package market.engine.core.data.filtersObjects


import market.engine.core.data.baseFilters.Filter
import market.engine.core.data.types.DealType

object DealFilters {
    private var filtersSalesInWork = arrayListOf(
        Filter("parcel_sent", "", null, null),//0 фильтр
        Filter("paid", "", null, null),//1 фильтр
        Filter("created_ts", "", null, "gte"),
        Filter("created_ts", "", null, "lte"),
        Filter("buyer_id", "", null, null),
        Filter("buyer_login", "", null, null),
        Filter("id", "", null, null),
        Filter("offer_id", "", null, null),
        Filter("search", "", null, null),
        Filter("archived_by_seller","false","",null)
    )
    private var filtersSalesArchive = arrayListOf(
        Filter("created_ts", "", null, "gte"),
        Filter("created_ts", "", null, "lte"),
        Filter("buyer_id", "", null, null),
        Filter("buyer_login", "", null, null),
        Filter("id", "", null, null),
        Filter("offer_id", "", null, null),
        Filter("search", "", null, null),
        Filter("archived_by_seller","true","",null)
    )
    private var filtersSalesAll = arrayListOf(
        Filter("created_ts", "", null, "gte"),
        Filter("created_ts", "", null, "lte"),
        Filter("buyer_id", "", null, null),
        Filter("buyer_login", "", null, null),
        Filter("id", "", null, null),
        Filter("offer_id", "", null, null),
        Filter("search", "", null, null),
    )
    private var filtersBuysArchive = arrayListOf(
        Filter("created_ts", "", null, "gte"),
        Filter("created_ts", "", null, "lte"),
        Filter("seller_id", "", null, null),
        Filter("seller_login", "", null, null),
        Filter("id", "", null, null),
        Filter("offer_id", "", null, null),
        Filter("search", "", null, null),
        Filter("archived_by_buyer","true","",null)
    )
    private var filtersBuysInWork = arrayListOf(
        Filter("created_ts", "", null, "gte"),
        Filter("created_ts", "", null, "lte"),
        Filter("seller_id", "", null, null),
        Filter("seller_login", "", null, null),
        Filter("id", "", null, null),
        Filter("offer_id", "", null, null),
        Filter("search", "", null, null),
        Filter("archived_by_buyer","false","",null)
    )

    fun clearTypeFilter(type: DealType) {
        when (type) {
            DealType.BUY_IN_WORK -> {
                filtersBuysInWork = arrayListOf(
                    Filter("created_ts", "", null, "gte"),
                    Filter("created_ts", "", null, "lte"),
                    Filter("seller_id", "", null, null),
                    Filter("seller_login", "", null, null),
                    Filter("id", "", null, null),
                    Filter("offer_id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("archived_by_buyer","false","",null)
                )
            }
            DealType.BUY_ARCHIVE -> {
                filtersBuysArchive = arrayListOf(
                    Filter("created_ts", "", null, "gte"),
                    Filter("created_ts", "", null, "lte"),
                    Filter("seller_id", "", null, null),
                    Filter("seller_login", "", null, null),
                    Filter("id", "", null, null),
                    Filter("offer_id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("archived_by_buyer","true","",null)
                )

            }
            DealType.SELL_ALL -> {
                filtersSalesAll = arrayListOf(
                    Filter("created_ts", "", null, "gte"),
                    Filter("created_ts", "", null, "lte"),
                    Filter("buyer_id", "", null, null),
                    Filter("buyer_login", "", null, null),
                    Filter("id", "", null, null),
                    Filter("offer_id", "", null, null),
                    Filter("search", "", null, null),
                )
            }
            DealType.SELL_ARCHIVE -> {
                filtersSalesArchive = arrayListOf(
                    Filter("created_ts", "", null, "gte"),
                    Filter("created_ts", "", null, "lte"),
                    Filter("buyer_id", "", null, null),
                    Filter("buyer_login", "", null, null),
                    Filter("id", "", null, null),
                    Filter("offer_id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("archived_by_seller","true","",null)
                )
            }
            DealType.SELL_IN_WORK -> {
                filtersSalesInWork = arrayListOf(
                    Filter("parcel_sent", "", null, null),//0 фильтр
                    Filter("paid", "", null, null),//1 фильтр
                    Filter("created_ts", "", null, "gte"),
                    Filter("created_ts", "", null, "lte"),
                    Filter("buyer_id", "", null, null),
                    Filter("buyer_login", "", null, null),
                    Filter("id", "", null, null),
                    Filter("offer_id", "", null, null),
                    Filter("search", "", null, null),
                    Filter("archived_by_seller","false","",null)
                )
            }
        }
    }

    fun getByTypeFilter(type: DealType): ArrayList<Filter> {
        when (type) {
            DealType.BUY_IN_WORK -> {
                return filtersBuysInWork
            }

            DealType.BUY_ARCHIVE -> {
                return filtersBuysArchive
            }

            DealType.SELL_ALL -> {
                return filtersSalesAll
            }

            DealType.SELL_ARCHIVE -> {
                return filtersSalesArchive
            }

            DealType.SELL_IN_WORK -> {
                return filtersSalesInWork
            }
        }
    }
}
