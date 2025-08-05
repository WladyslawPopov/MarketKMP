package market.engine.core.data.states

import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.network.networkObjects.Category

data class CreateOfferContentState(
    val appBarState: SimpleAppBarData = SimpleAppBarData(),
    val categoryState: CategoryState,

    val catHistory : List<Category> = emptyList(),
    val textState : String = "",

    val firstDynamicContent : List<String> = listOf(
        "title",
        "saletype",
    ),
    val secondDynamicContent : List<String> = listOf("params"),
    val thirdDynamicContent : List<String> = listOf(
        "length_in_days",
        "quantity",
        "relisting_mode",
        "whopaysfordelivery",
        "region",
        "freelocation",
        "paymentmethods",
        "dealtype",
        "deliverymethods",
    ),
    val endDynamicContent : List<String> = listOf(
        "images",
        "session_start",
        "description",
    )
)
