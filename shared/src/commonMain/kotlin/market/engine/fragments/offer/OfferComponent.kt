package market.engine.fragments.offer

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import market.engine.core.data.filtersObjects.EmptyFilters
import market.engine.core.data.items.ListingData
import market.engine.core.network.networkObjects.Category
import market.engine.core.network.networkObjects.Region
import market.engine.core.network.networkObjects.User
import market.engine.core.data.types.CreateOfferType
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
    fun goToUser(userId: Long, aboutMe: Boolean)
    fun goToCreateOffer(type: CreateOfferType,catPath: List<Long>?, offerId: Long, externalImages : List<String>?)
}

class DefaultOfferComponent(
    val id: Long,
    isSnapshot: Boolean,
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
    ) -> Unit
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


    init {
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
        navigationListing(ld)
    }

    override fun goToUsersListing(sellerData : User?) {
        if (sellerData == null) return

        val ld = ListingData()
        ld.searchData.value.userID = sellerData.id
        ld.searchData.value.userLogin = sellerData.login
        ld.searchData.value.userSearch = true
        navigationListing(ld)
    }

    override fun goToRegion(region : Region?) {
        if (region!= null){
            val ld = ListingData()
            val listingData = ld.data
            listingData.value.filters = arrayListOf()
            listingData.value.filters.addAll(EmptyFilters.getEmpty())
            listingData.value.filters.find { it.key == "region" }?.value = region.code.toString()
            listingData.value.filters.find { it.key == "region" }?.interpritation = region.name
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
        navigationCreateOffer(type, catPath, offerId, externalImages)
    }
}
