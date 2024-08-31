package application.market.auction_mobile.ui.search

import com.arkivanov.decompose.ComponentContext


interface SearchComponent {

    fun onCloseClicked()
}

class DefaultSearchComponent(
    componentContext: ComponentContext,
    itemId: Long,
    private val onBackPressed: () -> Unit
) : SearchComponent, ComponentContext by componentContext {

    // Omitted code

    override fun onCloseClicked() {
        onBackPressed()
    }
}
