package market.engine.fragments.root.main.profile.myProposals

import androidx.lifecycle.createSavedStateHandle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.jetpackcomponentcontext.JetpackComponentContext
import com.arkivanov.decompose.jetpackcomponentcontext.viewModel
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.launch
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel

interface MyProposalsComponent {

    val additionalModels : Value<AdditionalModels>
    data class AdditionalModels(
        val listingBaseViewModel: ListingBaseViewModel,
        val categoryViewModel: CategoryViewModel,
    )

    val model : Value<Model>
    data class Model(
        val viewModel: MyProposalsViewModel,
        var type : LotsType,
        val backHandler: BackHandler
    )

    fun goToProposal(offerId: Long, proposalType: ProposalType)
    fun goToUser(userId : Long)
    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun selectMyProposalsPage(select : LotsType)
    fun goToDialog(dialogId : Long?)
    fun goToBack()
    fun onRefresh()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultMyProposalsComponent(
    componentContext: JetpackComponentContext,
    val type: LotsType = LotsType.ALL_PROPOSAL,
    val offerSelected: (Long) -> Unit,
    val selectedMyProposalsPage: (LotsType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToDialog: (Long?) -> Unit,
    val navigateBack: () -> Unit,
    val navigateToProposal: (Long, ProposalType) -> Unit,
) : MyProposalsComponent, JetpackComponentContext by componentContext {

    val listingBaseVM = viewModel("MyProposalsBaseViewModel"){
        ListingBaseViewModel(
            savedStateHandle = createSavedStateHandle()
        )
    }

    val listingCategoryModel = viewModel("MyProposalsCategoryViewModel"){
        CategoryViewModel(savedStateHandle = createSavedStateHandle())
    }

    private val _additionalModels = MutableValue(
        MyProposalsComponent.AdditionalModels(
            listingBaseVM, listingCategoryModel
        )
    )

    override val additionalModels: Value<MyProposalsComponent.AdditionalModels> = _additionalModels


    private val viewModel = viewModel("MyProposalsViewModel"){
        MyProposalsViewModel(type, this@DefaultMyProposalsComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        MyProposalsComponent.Model(
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyProposalsComponent.Model> = _model

    private val analyticsHelper = viewModel.analyticsHelper

    private val updateBackHandlerItem = MutableValue(1L)

    val backCallback = object : BackCallback(){
        override fun onBack() {
            goToBack()
        }
    }

    init {
        backHandler.register(backCallback)

        lifecycle.doOnResume {
            viewModel.scope.launch {
                viewModel.updateUserInfo()
                if (UserData.token == "") {
                    goToBack()
                }
            }

            if (updateBackHandlerItem.value != 1L) {
                viewModel.setUpdateItem(updateBackHandlerItem.value)
                updateBackHandlerItem.value = 1L
            }
        }
        lifecycle.doOnDestroy {
            viewModel.onClear()
            listingBaseVM.onClear()
            listingCategoryModel.onClear()
        }
        val eventParameters = mapOf(
            "user_id" to UserData.login.toString(),
            "profile_source" to "proposals"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        updateBackHandlerItem.value = offer.id
        offerSelected(offer.id)
    }

    override fun selectMyProposalsPage(select: LotsType) {
        selectedMyProposalsPage(select)
    }

    override fun goToDialog(dialogId: Long?) {
        navigateToDialog(dialogId)
    }

    override fun goToBack() {
        viewModel.onBackNavigation {
            navigateBack()
        }
    }

    override fun onRefresh() {
        viewModel.updatePage()
    }

    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }

    override fun goToProposal(offerId: Long, proposalType: ProposalType) {
        updateBackHandlerItem.value = offerId
        navigateToProposal(offerId, proposalType)
    }
}
