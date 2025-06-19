package market.engine.fragments.root.main.profile.myOffers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.setNewParams


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
    fun isHideItem(offer: OfferItem) : Boolean
    fun updateItem(oldItem: OfferItem?)
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

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
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

    override fun onRefresh() {
        viewModel.onRefresh()
    }

    override fun isHideItem(offer: OfferItem): Boolean {
        return when (model.value.type) {
            LotsType.MY_LOT_ACTIVE -> {
                offer.state != "active" && offer.session == null
            }

            LotsType.MY_LOT_INACTIVE -> {
                offer.state == "active"
            }

            LotsType.MY_LOT_IN_FUTURE -> {
                val currentDate: Long? = getCurrentDate().toLongOrNull()
                if (currentDate != null) {
                    val initD = (offer.session?.start?.toLongOrNull() ?: 1L) - currentDate

                    offer.state != "active" && initD < 0
                }else{
                    false
                }
            }

            else -> {
                false
            }
        }
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
                    oldItem?.state = null
                }
                if (oldItem != null) {
                    if (!isHideItem(oldItem)) {
                        onRefresh()
                    }
                }

                viewModel.updateItem.value = null
                viewModel.updateItemTrigger.value++
            }
        }
    }
}
