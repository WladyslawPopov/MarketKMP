package market.engine.fragments.root.main.profile.myProposals

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.setNewParams

interface MyProposalsComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<OfferItem>>,
        val viewModel: MyProposalsViewModel,
        var type : LotsType,
        val backHandler: BackHandler
    )

    fun goToProposal(offerId: Long, proposalType: ProposalType)
    fun goToUser(userId : Long)
    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun selectMyProposalsPage(select : LotsType)
    fun goToDialog(dialogId : Long?)
    fun updateItem(offer: OfferItem?)
    fun goToBack()
    fun onRefresh()
}

class DefaultMyProposalsComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.ALL_PROPOSAL,
    val offerSelected: (Long) -> Unit,
    val selectedMyProposalsPage: (LotsType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToDialog: (Long?) -> Unit,
    val navigateBack: () -> Unit,
    val navigateToProposal: (Long, ProposalType) -> Unit,
) : MyProposalsComponent, ComponentContext by componentContext {

    private val viewModel : MyProposalsViewModel = MyProposalsViewModel(type)

    private val _model = MutableValue(
        MyProposalsComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyProposalsComponent.Model> = _model


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
            "profile_source" to "proposals"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        offerSelected(offer.id)

        lifecycle.doOnResume {
            viewModel.updateItem.value = offer.id
        }
    }

    override fun selectMyProposalsPage(select: LotsType) {
        selectedMyProposalsPage(select)
    }

    override fun goToDialog(dialogId: Long?) {
        navigateToDialog(dialogId)
    }

    override fun updateItem(oldItem: OfferItem?) {
        viewModel.viewModelScope.launch {
            val offer = withContext(Dispatchers.Default) {
                viewModel.getOfferById(viewModel.updateItem.value!!)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    oldItem?.setNewParams(offer)
                }else{
                    oldItem?.session = null
                }

                viewModel.updateItemTrigger.value++
                viewModel.updateItem.value = null
            }
        }
    }

    override fun goToBack() {
        navigateBack()
    }

    override fun onRefresh() {
        viewModel.onRefresh()
    }

    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }

    override fun goToProposal(offerId: Long, proposalType: ProposalType) {
        navigateToProposal(offerId, proposalType)
    }
}
