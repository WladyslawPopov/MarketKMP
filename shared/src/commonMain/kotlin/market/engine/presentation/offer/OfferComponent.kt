package market.engine.presentation.offer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.filtersObjects.EmptyFilters
import market.engine.core.items.ListingData
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Region
import market.engine.core.network.networkObjects.User
import market.engine.core.repositories.UserRepository
import org.koin.mp.KoinPlatform.getKoin

interface OfferComponent {

    val model : Value<Model>

    data class Model(
        val id: Long,
        val isSnapshot: Boolean,
        val offerViewModel: OfferViewModel
    )
    fun updateOffer(id: Long, isSnapshot: Boolean)
    fun navigateToOffers(id: Long)
    fun onBeakClick()
    fun goToCategory(cat: Category)
    fun goToUsersListing(sellerData : User?)
    fun goToRegion(region : Region?)
    fun goToCart()
    fun goToUser(userId: Long, aboutMe: Boolean)
}

class DefaultOfferComponent(
    val id: Long,
    isSnapshot: Boolean,
    componentContext: ComponentContext,
    val selectOffer: (id: Long) -> Unit,
    val navigationBack: () -> Unit,
    val navigationListing: (listingData: ListingData) -> Unit,
    val navigationBasket: () -> Unit,
    val navigateToUser: (Long, Boolean) -> Unit
) : OfferComponent, ComponentContext by componentContext {

    private val _model = MutableValue(
        OfferComponent.Model(
            id = id,
            isSnapshot = isSnapshot,
            offerViewModel = getKoin().get()
        )
    )
    override val model: Value<OfferComponent.Model> = _model
    private val offerViewModel = model.value.offerViewModel
    private val userRepository = getKoin().get<UserRepository>()

    init {
        userRepository.updateToken()
        userRepository.updateUserInfo(offerViewModel.viewModelScope)

        updateOffer(id, isSnapshot)
    }

    override fun updateOffer(id: Long, isSnapshot: Boolean) {
        offerViewModel.getOffer(id, isSnapshot)
    }

    override fun navigateToOffers(id: Long) {
        selectOffer(id)
    }

    override fun onBeakClick() {
        navigationBack()
    }

    override fun goToCategory(cat: Category) {
        val ld = ListingData()
        ld.searchData.value.searchCategoryID = cat.id
        ld.searchData.value.searchCategoryName = cat.name
        ld.searchData.value.searchParentID = cat.parentId
        ld.searchData.value.searchIsLeaf = cat.isLeaf
        ld.data.value.isOpenCategory.value = false
        navigationListing(ld)
    }

    override fun goToUsersListing(sellerData : User?) {
        if (sellerData == null) return

        val ld = ListingData()
        ld.searchData.value.userID = sellerData.id
        ld.searchData.value.userLogin = sellerData.login
        ld.searchData.value.userSearch = true
        ld.data.value.isOpenCategory.value = false
        navigationListing(ld)
    }

    override fun goToRegion(region : Region?) {
        if (region!= null){
            val ld = ListingData()
            val listingData = ld.data.value
            listingData.filters = arrayListOf()
            listingData.filters.addAll(EmptyFilters.getEmpty())
            listingData.filters.find { it.key == "region" }?.value = region.code.toString()
            listingData.filters.find { it.key == "region" }?.interpritation = region.name
            ld.data.value.isOpenCategory.value = false
            navigationListing(ld)
        }
    }

    override fun goToCart() {
        navigationBasket()
    }

    override fun goToUser(userId: Long, aboutMe: Boolean) {
        navigateToUser(userId, aboutMe)
    }
}
