package market.engine.fragments.root.main.offer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.launch
import market.engine.core.data.filtersObjects.ListingFilters
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Region
import market.engine.core.network.networkObjects.User
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.ProposalType
import org.koin.mp.KoinPlatform.getKoin

interface OfferComponent {

    val model : Value<Model>

    data class Model(
        val id: Long,
        val isSnapshot: Boolean,
        val offerViewModel: OfferViewModel,
        val backHandler: BackHandler
    )

    fun navigateToOffers(id: Long)
    fun onBackClick()
    fun goToCategory(cat: Category)
    fun goToUsersListing(sellerData : User?)
    fun goToRegion(region : Region?)
    fun goToUser(userId: Long, aboutMe: Boolean)
    fun goToCreateOffer(type: CreateOfferType,catPath: List<Long>?, offerId: Long, externalImages : List<String>?)
    fun goToCreateOrder(item : Pair<Long, List<SelectedBasketItem>>)
    fun goToDialog(dialogId: Long?)
    fun goToProposalPage(type: ProposalType)
    fun goToDynamicSettings(type: String, offerId : Long? = null)
    fun goToLogin()
    fun goToSubscribes()
}

class DefaultOfferComponent(
    val id: Long,
    val isSnapshot: Boolean,
    componentContext: ComponentContext,
    val selectOffer: (Long) -> Unit,
    val navigationBack: () -> Unit,
    val navigationListing: (listingData: ListingData) -> Unit,
    val navigateToUser: (Long, Boolean) -> Unit,
    val navigationCreateOffer: (
        type: CreateOfferType,
        catPath : List<Long>?,
        offerId: Long,
        externalImages : List<String>?
    ) -> Unit,
    val navigateToCreateOrder: (item : Pair<Long, List<SelectedBasketItem>>) -> Unit,
    val navigateToLogin: () -> Unit,
    val navigateToDialog: (dialogId: Long?) -> Unit,
    val navigationSubscribes: () -> Unit,
    val navigateToProposalPage: (offerId: Long, type: ProposalType) -> Unit,
    val navigateDynamicSettings: (type: String, offerId: Long?) -> Unit,
) : OfferComponent, ComponentContext by componentContext {

    val viewModel : OfferViewModel = OfferViewModel(
        getKoin().get(), this, id, isSnapshot
    )

    private val _model = MutableValue(
        OfferComponent.Model(
            id = id,
            isSnapshot = isSnapshot,
            offerViewModel = viewModel,
            backHandler = backHandler
        )
    )
    override val model: Value<OfferComponent.Model> = _model
    private val offerViewModel = model.value.offerViewModel

    private val shouldRefreshOnResume = MutableValue(false)

    init {
        lifecycle.doOnResume {
            if (shouldRefreshOnResume.value) {
                viewModel.refreshPage()
                shouldRefreshOnResume.value = false
            }
        }

        lifecycle.doOnDestroy {
            offerViewModel.viewModelScope.launch {
                offerViewModel.addHistory(model.value.id)
            }
            offerViewModel.clearTimers()
        }
    }

    override fun navigateToOffers(id: Long) {
        selectOffer(id)
    }

    override fun onBackClick() {
        navigationBack()
    }

    override fun goToCategory(cat: Category) {
        val ld = ListingData()
        ld.searchData.searchCategoryID = cat.id
        ld.searchData.searchCategoryName = cat.name ?: ""
        ld.searchData.searchParentID = cat.parentId
        ld.searchData.searchIsLeaf = cat.isLeaf
        navigationListing(ld)
    }

    override fun goToUsersListing(sellerData : User?) {
        if (sellerData == null) return

        val ld = ListingData()
        ld.searchData.userID = sellerData.id
        ld.searchData.userLogin = sellerData.login
        ld.searchData.userSearch = true
        navigationListing(ld)
    }

    override fun goToRegion(region : Region?) {
        if (region!= null){
            val ld = ListingData()
            val listingData = ld.data
            listingData.filters = ListingFilters.getEmpty()
            listingData.filters.find { it.key == "region" }?.value = region.code.toString()
            listingData.filters.find { it.key == "region" }?.interpretation = region.name
            navigationListing(ld)
        }
    }

    override fun goToUser(userId: Long, aboutMe: Boolean) {
        navigateToUser(userId, aboutMe)
    }

    override fun goToCreateOffer(
        type: CreateOfferType,
        catPath: List<Long>?,
        offerId: Long,
        externalImages: List<String>?
    ) {
        shouldRefreshOnResume.value = true
        navigationCreateOffer(type, catPath, offerId, externalImages)
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {
        shouldRefreshOnResume.value = true
        navigateToCreateOrder(item)
    }

    override fun goToDialog(dialogId: Long?) {
        navigateToDialog(dialogId)
    }

    override fun goToProposalPage(type: ProposalType) {
        shouldRefreshOnResume.value = true
        navigateToProposalPage(id, type)
    }

    override fun goToDynamicSettings(type: String, offerId: Long?) {
        shouldRefreshOnResume.value = true
        navigateDynamicSettings(type, offerId)
    }

    override fun goToLogin() {
        navigateToLogin()
    }

    override fun goToSubscribes() {
        navigationSubscribes()
    }
}
