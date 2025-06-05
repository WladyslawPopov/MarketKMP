package market.engine.fragments.root.main.profile.myBids

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
import market.engine.core.utils.setNewParams


interface MyBidsComponent {
    val model : Value<Model>
    data class Model(
        val pagingDataFlow : Flow<PagingData<OfferItem>>,
        val viewModel: MyBidsViewModel,
        var type : LotsType,
        val backHandler: BackHandler
    )

    fun goToUser(userId : Long)
    fun goToPurchases()
    fun goToOffer(offer: OfferItem, isTopPromo : Boolean = false)
    fun selectMyBidsPage(select : LotsType)
    fun goToDialog(dialogId : Long?)
    fun goToBack()
    fun onRefresh()
    fun updateItem(item: OfferItem?)
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

    private val viewModel : MyBidsViewModel = MyBidsViewModel(type)

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

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
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

    override fun onRefresh() {
        viewModel.onRefresh()
    }

    override fun updateItem(oldItem: OfferItem?) {
        viewModel.viewModelScope.launch {
            val offer = withContext(Dispatchers.Default) {
                viewModel.getOfferById(viewModel.updateItem.value!!)
            }
            withContext(Dispatchers.Main) {
                if (offer != null)
                    oldItem?.setNewParams(offer)

                viewModel.updateItemTrigger.value++
                viewModel.updateItem.value = null
            }
        }
    }

    override fun goToUser(userId: Long) {
        navigateToUser(userId)
    }

    override fun goToPurchases() {
        navigateToPurchases()
    }
}
