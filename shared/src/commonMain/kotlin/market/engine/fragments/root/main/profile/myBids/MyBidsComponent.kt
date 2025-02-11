package market.engine.fragments.root.main.profile.myBids

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.Flow
import market.engine.common.AnalyticsFactory
import market.engine.core.data.globalData.UserData
import market.engine.core.network.networkObjects.Offer
import market.engine.core.data.types.LotsType
import market.engine.fragments.root.main.profile.myProposals.MyProposalsViewModel


interface MyBidsComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<Offer>>,
        val viewModel: MyProposalsViewModel,
        var type : LotsType,
        val backHandler: BackHandler
    )

    fun goToUser(userId : Long)
    fun goToPurchases()
    fun goToOffer(offer: Offer, isTopPromo : Boolean = false)
    fun selectMyBidsPage(select : LotsType)
    fun goToDialog(dialogId : Long?)
    fun goToBack()
}

class DefaultMyBidsComponent(
    componentContext: ComponentContext,
    val type: LotsType = LotsType.MYBIDLOTS_ACTIVE,
    val offerSelected: (Long) -> Unit,
    val selectedMyBidsPage: (LotsType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToPurchases: () -> Unit,
    val navigateToDialog: (Long?) -> Unit,
    val navigateBack: () -> Unit
) : MyBidsComponent, ComponentContext by componentContext {

    private val viewModel : MyProposalsViewModel = MyProposalsViewModel(type)

    private val listingData = viewModel.listingData.value

    private val _model = MutableValue(
        MyBidsComponent.Model(
            pagingDataFlow = viewModel.init(),
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )
    override val model: Value<MyBidsComponent.Model> = _model

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
            "profile_source" to "bids"
        )
        analyticsHelper.reportEvent("view_seller_profile", eventParameters)
    }

    override fun goToOffer(offer: Offer, isTopPromo : Boolean) {
        offerSelected(offer.id)

        lifecycle.doOnResume {
            viewModel.updateItem.value = offer.id
        }
    }

    override fun selectMyBidsPage(select: LotsType) {
        selectedMyBidsPage(select)
    }

    override fun goToDialog(dialogId: Long?) {
        navigateToDialog(dialogId)
    }

    override fun goToBack() {
        navigateBack()
    }


    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }

    override fun goToPurchases() {
        navigateToPurchases()
    }
}
