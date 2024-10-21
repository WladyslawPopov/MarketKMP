package market.engine.core.network

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import market.engine.core.constants.UserData
import market.engine.core.globalData.LD
import market.engine.core.globalData.SD
import market.engine.core.util.convertDateWithMinutes

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
            if (searchData.userSearch) {
                if (searchData.userID != 1L) {
                    queryParams["filter_${counter}_key"] = "seller_id"
                    queryParams["filter_${counter}_value"] = searchData.userID.toString()

                    if (searchData.searchString != null) {
                        val search = replaceSpecialCharacters(searchData.searchString.toString())
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

                        if (searchData.searchString != null) {
                            val search = replaceSpecialCharacters(searchData.searchString.toString())
                            if (search != "") {
                                counter++
                                queryParams["filter_${counter}_key"] = "search"
                                queryParams["filter_${counter}_value"] = search
                            }
                        }
                    }
                }
                counter++
            }else {
                if (searchData.searchString != null) {
                    val search = replaceSpecialCharacters(searchData.searchString.toString())
                    if (search != "") {
                        queryParams["filter_${counter}_key"] = "search"
                        queryParams["filter_${counter}_value"] = search
                        counter++
                    }
                }
            }
            if (searchData.searchFinished) {
                queryParams["filter_${counter}_key"] = "state"
                queryParams["filter_${counter}_value"] = "1"
                counter++
            }

            queryParams["filter_${counter}_key"] = "category"
            queryParams["filter_${counter}_value"] = searchData.searchCategoryID.toString()
            counter++
        }

        if (listingData != null) {
            val filters = listingData.filters
            filters?.forEach {
                if (it.interpritation != null) {
                    when (it.key) {
                        "session_start" -> {
                            val currentInstant: Instant = Clock.System.now()
                            val currentTimeInSeconds = currentInstant.epochSeconds
                            it.value = currentTimeInSeconds.toString()
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
