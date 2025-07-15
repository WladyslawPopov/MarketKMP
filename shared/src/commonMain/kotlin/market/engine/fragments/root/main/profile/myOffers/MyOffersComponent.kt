package market.engine.fragments.root.main.profile.myOffers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType


interface MyOffersComponent {
    val model : Value<Model>
    data class Model(
        val viewModel: MyOffersViewModel,
        var type : LotsType,
        val backHandler: BackHandler
    )

    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun selectMyOfferPage(select : LotsType)
    fun goToCreateOffer(type : CreateOfferType, offerId : Long? = null,  catPath : List<Long>?)
    fun goToProposals(offerId : Long, proposalType: ProposalType)
    fun goToDynamicSettings(type : String, id : Long? = null)
    fun goToBack()
    fun onRefresh()
}

class DefaultMyOffersComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.MY_LOT_ACTIVE,
    val offerSelected: (Long) -> Unit,
    val selectedMyOfferPage: (LotsType) -> Unit,
    val navigateToCreateOffer: (CreateOfferType, Long?, List<Long>?) -> Unit,
    val navigateToProposal: (Long, ProposalType) -> Unit,
    val navigateToBack: () -> Unit,
    val navigateToDynamicSettings: (String, Long?) -> Unit,
) : MyOffersComponent, ComponentContext by componentContext {

    private val viewModel : MyOffersViewModel = MyOffersViewModel(type, this)

    private val _model = MutableValue(
        MyOffersComponent.Model(
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyOffersComponent.Model> = _model
    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    private val updateBackHandlerItem = MutableValue(1L)

    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                goToBack()
            }

            if (updateBackHandlerItem.value != 1L) {
                viewModel.setUpdateItem(updateBackHandlerItem.value)
                updateBackHandlerItem.value = 1L
            }
        }

        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "offers"
        )

        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        offerSelected(offer.id)

        updateBackHandlerItem.value = offer.id
    }

    override fun selectMyOfferPage(select: LotsType) {
        selectedMyOfferPage(select)
    }

    override fun goToCreateOffer(type: CreateOfferType, offerId: Long?, catPath : List<Long>?) {
        navigateToCreateOffer(type, offerId, catPath)
        updateBackHandlerItem.value = offerId ?: 1L
    }

    override fun goToProposals(offerId: Long, proposalType: ProposalType) {
        navigateToProposal(offerId, proposalType)
        updateBackHandlerItem.value = offerId
    }

    override fun goToDynamicSettings(type: String, id : Long?) {
        navigateToDynamicSettings(type, id)
        updateBackHandlerItem.value = id ?: 1L
    }

    override fun goToBack() {
        navigateToBack()
    }

    override fun onRefresh() {
        viewModel.updatePage()
    }
}
