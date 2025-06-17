package market.engine.fragments.root.main.favPages.favorites

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.ProposalType

interface FavoritesComponent {
    val model : Value<Model>
    data class Model(
        val listId : Long?,
        val favType: FavScreenType,
        val favViewModel: FavViewModel,
        val backHandler: BackHandler
    )

    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun refreshTabs()
    fun goToProposal(type: ProposalType, offerId : Long)
    fun goToCreateOffer(createOfferType : CreateOfferType, id : Long)
    fun onRefresh()
}

class DefaultFavoritesComponent(
    componentContext: ComponentContext,
    favType : FavScreenType,
    idList : Long?,
    val goToOffer : (Long) -> Unit,
    val updateTabs : () -> Unit,
    val navigateToProposalPage : (ProposalType, Long) -> Unit,
    val navigateToCreateOffer : (CreateOfferType, Long) -> Unit,
) : FavoritesComponent, ComponentContext by componentContext {

    private val favViewModel : FavViewModel = FavViewModel(favType, idList, this)

    private val _model = MutableValue(
        FavoritesComponent.Model(
            listId = idList,
            favType = favType,
            favViewModel = favViewModel,
            backHandler = backHandler
        )
    )

    override val model: Value<FavoritesComponent.Model> = _model

    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            favViewModel.updateUserInfo()
        }

        analyticsHelper.reportEvent("open_favorites", mapOf("type" to favType.name))
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        goToOffer(offer.id)
        lifecycle.doOnResume {
            favViewModel.updateItem.value = offer.id
        }
    }

    override fun refreshTabs() {
        updateTabs()
    }

    override fun goToProposal(type: ProposalType, offerId: Long) {
        navigateToProposalPage(type, offerId)
    }

    override fun goToCreateOffer(createOfferType: CreateOfferType, id: Long) {
        navigateToCreateOffer(createOfferType, id)
    }

    override fun onRefresh() {
        favViewModel.updatePage()
    }
}
