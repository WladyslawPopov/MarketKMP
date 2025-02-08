package market.engine.core.network

import market.engine.common.AnalyticsFactory
import market.engine.core.analytics.AnalyticsHelper
import market.engine.core.data.globalData.UserData
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.utils.getCurrentDate

class UrlBuilder {

    private val queryParams: MutableMap<String, String> = mutableMapOf()
    private val pathSegments: MutableList<String> = mutableListOf()

    private val analyticsHelper : AnalyticsHelper = AnalyticsFactory.getAnalyticsHelper()


    fun addQueryParameter(name: String, value: String?): UrlBuilder {
        value?.let {
            queryParams[name] = it
        }
        return this
    }

    fun addPathSegment(pathSegment: String): UrlBuilder {
        pathSegments.add(pathSegment)
        return this
    }

    fun addFilters(listingData: LD?, searchData: SD?): UrlBuilder {
        var counter = 1

        if (searchData != null) {
            if (searchData.userSearch) {
                if (searchData.userID != 1L) {
                    queryParams["filter_${counter}_key"] = "seller_id"
                    queryParams["filter_${counter}_value"] = searchData.userID.toString()

                    if (searchData.searchString.isNotEmpty()) {
                        val search = replaceSpecialCharacters(searchData.searchString)
                        if (search != "") {
                            counter++
                            queryParams["filter_${counter}_key"] = "search"
                            queryParams["filter_${counter}_value"] = search
                        }
                    }
                } else {
                    if (searchData.userLogin != null) {
                        val s = replaceSpecialCharacters(searchData.userLogin.toString())
                        if (s != "") {
                            queryParams["filter_${counter}_key"] = "seller_login"
                            queryParams["filter_${counter}_value"] = s
                        }

                        if (searchData.searchString.isNotEmpty()) {
                            val search = replaceSpecialCharacters(searchData.searchString)
                            if (search != "") {
                                counter++
                                queryParams["filter_${counter}_key"] = "search"
                                queryParams["filter_${counter}_value"] = search
                            }
                        }
                    }
                }

                val eventParameters = mapOf(
                    "search_query" to searchData.searchString,
                    "search_login" to searchData.userLogin,
                    "search_user_id" to searchData.userID.toString(),
                    "search_category" to searchData.searchCategoryName,
                )
                analyticsHelper.reportEvent("show_search_user_offers_results", eventParameters)
                counter++
            }else {
                if (searchData.searchString.isNotEmpty()) {
                    val search = replaceSpecialCharacters(searchData.searchString)
                    if (search != "") {
                        queryParams["filter_${counter}_key"] = "search"
                        queryParams["filter_${counter}_value"] = search
                        counter++
                    }
                }

                val eventParameters = mapOf(
                    "search_query" to searchData.searchString,
                    "search_category" to searchData.searchCategoryName,
                )
                analyticsHelper.reportEvent("show_search_results", eventParameters)
            }
            if (searchData.searchFinished) {
                queryParams["filter_${counter}_key"] = "state"
                queryParams["filter_${counter}_value"] = "1"
                counter++
            }
            if (searchData.searchCategoryID != 1L) {
                queryParams["filter_${counter}_key"] = "category"
                queryParams["filter_${counter}_value"] = searchData.searchCategoryID.toString()
                counter++
            }
        }

        if (listingData != null) {

            val eventParameters = mapOf("catalog_filter_string" to searchData?.searchCategoryName)
            analyticsHelper.reportEvent("activate_filter_catalog", eventParameters)

            listingData.filters.forEach {
                if (it.interpritation != null) {
                    when (it.key) {
                        "session_start" -> {
                            it.value = getCurrentDate()
                        }

                        "users_to_act_on_price_proposals" -> {
                            it.value = UserData.login.toString()
                        }
                    }
                    queryParams["filter_${counter}_key"] = it.key
                    if (it.value != "") {
                        queryParams["filter_${counter}_value"] = it.value
                    }
                    val operation = it.operation
                    if (operation != null && it.key != "category") {
                        queryParams["filter_${counter}_operation"] = operation
                    }
                    counter++
                }
            }

            val sort = listingData.sort
            if (sort != null) {
                analyticsHelper.reportEvent("activate_sort_catalog", mapOf(
                    "catalog_filter_string" to searchData?.searchCategoryName,
                    "catalog_sort_type" to sort.key,
                    "sort_type" to sort.value
                ))

                queryParams["sorter_1_key"] = sort.key
                queryParams["sorter_1_value"] = sort.value
            }
        }

        return this
    }

    private fun replaceSpecialCharacters(input: String): String {
        return input.replace(Regex("[^a-zA-Zа-яА-Я0-9]"), "_")
    }


    fun build(): String {
        val path = pathSegments.joinToString("/")
        val queryString = queryParams.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        return if (queryString.isNotEmpty()) "$path?$queryString" else path
    }

    private fun encode(value: String): String {
        return value.replace(" ", "%20")
            .replace("<", "%3C")
            .replace(">", "%3E")
            .replace("#", "%23")
            .replace("%", "%25")
            .replace("|", "%7C")
            .replace("&", "%26")
            .replace("=", "%3D")
    }
}
