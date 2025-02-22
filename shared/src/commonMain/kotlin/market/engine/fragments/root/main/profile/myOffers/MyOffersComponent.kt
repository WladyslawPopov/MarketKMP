package market.engine.fragments.root.main.profile.myOffers

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.types.CreateOfferType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType


interface MyOffersComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<Offer>>,
        val viewModel: MyOffersViewModel,
        var type : LotsType,
        val backHandler: BackHandler
    )

    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
    fun selectMyOfferPage(select : LotsType)
    fun goToCreateOffer(type : CreateOfferType, offerId : Long? = null,  catPath : List<Long>?)
    fun goToProposals(offerId : Long, proposalType: ProposalType)
    fun goToDynamicSettings(type : String, id : Long? = null)
    fun goToBack()
}

class DefaultMyOffersComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.MYLOT_ACTIVE,
    val offerSelected: (Long) -> Unit,
    val selectedMyOfferPage: (LotsType) -> Unit,
    val navigateToCreateOffer: (CreateOfferType, Long?, List<Long>?) -> Unit,
    val navigateToProposal: (Long, ProposalType) -> Unit,
    val navigateToBack: () -> Unit,
    val navigateToDynamicSettings: (String, Long?) -> Unit,
) : MyOffersComponent, ComponentContext by componentContext {

    private val viewModel : MyOffersViewModel = MyOffersViewModel(type)

    private val _model = MutableValue(
        MyOffersComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyOffersComponent.Model> = _model
    private val analyticsHelper = AnalyticsFactory.getAnalyticsHelper()

    init {
        lifecycle.doOnResume {
            viewModel.updateUserInfo()
            if (UserData.token == ""){
                goToBack()
            }
        }
        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "offers"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }

    override fun goToOffer(offer: Offer, isTopPromo : Boolean) {
        offerSelected(offer.id)

        lifecycle.doOnResume {
            viewModel.updateItem.value = offer.id
        }
    }

    override fun selectMyOfferPage(select: LotsType) {
        selectedMyOfferPage(select)
    }

    override fun goToCreateOffer(type: CreateOfferType, offerId: Long?, catPath : List<Long>?) {
        navigateToCreateOffer(type, offerId, catPath)

        lifecycle.doOnResume {
            viewModel.updateItem.value = offerId
        }
    }

    override fun goToProposals(offerId: Long, proposalType: ProposalType) {
        navigateToProposal(offerId, proposalType)
    }

    override fun goToDynamicSettings(type: String, id : Long?) {
        navigateToDynamicSettings(type, id)
    }

    override fun goToBack() {
        navigateToBack()
    }
}
