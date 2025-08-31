package market.engine.fragments.root.main.profile.myBids

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
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.OfferItem
import market.engine.core.data.types.LotsType
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.widgets.filterContents.categories.CategoryViewModel

interface MyBidsComponent {

    val additionalModels : Value<AdditionalModels>
    data class AdditionalModels(
        val listingBaseViewModel: ListingBaseViewModel,
        val categoryViewModel: CategoryViewModel,
    )

    val model : Value<Model>
    data class Model(
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
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultMyBidsComponent(
    componentContext: JetpackComponentContext,
    val type: LotsType = LotsType.MY_BIDS_ACTIVE,
    val offerSelected: (Long) -> Unit,
    val selectedMyBidsPage: (LotsType) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToPurchases: () -> Unit,
    val navigateToDialog: (Long?) -> Unit,
    val navigateBack: () -> Unit
) : MyBidsComponent, JetpackComponentContext by componentContext {

    val listingBaseVM = viewModel("myBidsBaseViewModel"){
        ListingBaseViewModel(
            savedStateHandle = createSavedStateHandle()
        )
    }

    val listingCategoryModel = viewModel("myBidsCategoryViewModel"){
        CategoryViewModel(savedStateHandle = createSavedStateHandle())
    }

    private val _additionalModels = MutableValue(
        MyBidsComponent.AdditionalModels(
            listingBaseVM, listingCategoryModel
        )
    )

    override val additionalModels: Value<MyBidsComponent.AdditionalModels> = _additionalModels

    private val viewModel = viewModel("myBidsViewModel"){
        MyBidsViewModel(type, this@DefaultMyBidsComponent, createSavedStateHandle())
    }

    private val _model = MutableValue(
        MyBidsComponent.Model(
            viewModel = viewModel,
            type = type,
            backHandler = backHandler
        )
    )

    override val model: Value<MyBidsComponent.Model> = _model

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

        lifecycle.doOnDestroy {
            viewModel.onClear()
            listingBaseVM.onClear()
            listingCategoryModel.onClear()
        }
    }

    override fun goToOffer(offer: OfferItem, isTopPromo : Boolean) {
        updateBackHandlerItem.value = offer.id
        offerSelected(offer.id)
    }

    override fun selectMyBidsPage(select: LotsType) {
        selectedMyBidsPage(select)
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
        updateBackHandlerItem.value = userId
        navigateToUser(userId)
    }

    override fun goToPurchases() {
        navigateToPurchases()
    }
}
