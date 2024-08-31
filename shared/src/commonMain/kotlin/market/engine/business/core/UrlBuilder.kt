package application.market.auction_mobile.business.core

import application.market.auction_mobile.business.constants.UserData
import application.market.auction_mobile.business.globalData.LD
import application.market.data.globalData.SD

class UrlBuilder {

    private val queryParams: MutableMap<String, String> = mutableMapOf()
    private val pathSegments: MutableList<String> = mutableListOf()


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
            if (searchData.fromSearch || searchData.searchChoice != null) {
                when (searchData.searchChoice) {
                    "user_search" -> {
                        if (searchData.userID != 1L) {
                            queryParams["filter_${counter}_key"] = "seller_id"
                            queryParams["filter_${counter}_value"] = searchData.userID.toString()
                        } else {
                            val s = searchData.searchUsersLots
                            if (s != null) {
                                queryParams["filter_${counter}_key"] = "seller_login"
                                queryParams["filter_${counter}_value"] = s
                            }
                        }
                        counter++
                    }
                }
                if (searchData.searchFinished) {
                    queryParams["filter_${counter}_key"] = "state"
                    queryParams["filter_${counter}_value"] = "1"
                    counter++
                }
                if (searchData.searchString != null) {
                    val search = replaceSpecialCharacters(searchData.searchString.toString())
                    queryParams["filter_${counter}_key"] = "search"
                    queryParams["filter_${counter}_value"] = search
                    counter++
                }
                queryParams["filter_${counter}_key"] = "category"
                queryParams["filter_${counter}_value"] = searchData.searchCategoryID.toString()
                counter++
            } else {
                queryParams["filter_${counter}_key"] = "category"
                queryParams["filter_${counter}_value"] = searchData.searchCategoryID.toString()
                counter++
            }
        }

        if (listingData != null) {
            val filters = listingData.filters
            filters?.forEach {
                if (it.interpritation != null) {
                    when (it.key) {
                        "session_start" -> {
//                            it.value = (Calendar.getInstance().timeInMillis / 1000).toString()
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
                    if (operation != null) {
                        queryParams["filter_${counter}_operation"] = operation
                    }
                    counter++
                }
            }

            val sort = listingData.sort
            if (sort != null) {
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
        //val queryString = queryParams.entries.joinToString("&") { (key, value) ->
            //"${URLEncoder.encode(key, StandardCharsets.UTF_8.name())}=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
        //}
        return "$path?queryString"
    }
}
