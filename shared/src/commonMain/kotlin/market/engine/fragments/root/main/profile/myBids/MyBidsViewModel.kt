package market.engine.fragments.root.main.profile.myBids

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.events.OfferRepositoryEvents
import market.engine.core.data.filtersObjects.OfferFilters
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.OfferItem
import market.engine.core.data.items.SelectedBasketItem
import market.engine.core.data.states.CabinetOfferItemState
import market.engine.core.data.states.CategoryState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.OfferRepository
import market.engine.core.repositories.PagingRepository
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin
import kotlin.getValue

class MyBidsViewModel(
    val type: LotsType,
    val component: MyBidsComponent
) : CoreViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val offerOperations : OfferOperations by lazy { getKoin().get() }

    val listingBaseViewModel = ListingBaseViewModel()
    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    private val _activeWindowType = MutableStateFlow(ActiveWindowListingType.LISTING)

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    val categoryState = CategoryState(
        activeType.value == ActiveWindowListingType.CATEGORY_FILTERS,
        CategoryViewModel(isFilters = true)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<CabinetOfferItemState>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Offer.serializer()
            ){ tc ->
                listingBaseViewModel.setTotalCount(tc)
            }.map { pagingData ->
                pagingData.map { offer ->
                    val item = offer.parseToOfferItem()
                    CabinetOfferItemState(
                        item = item,
                        offerRepository = OfferRepository(
                            item,
                            OfferRepositoryEventsImpl(this@MyBidsViewModel, item, component),
                            this@MyBidsViewModel
                        )
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            PagingData.empty()
        ).cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            listingBaseViewModel.setListingData(
                listingBaseViewModel.listingData.value.copy(
                    data = LD(
                        filters = OfferFilters.getByTypeFilter(type),
                        methodServer = "get_cabinet_listing_with_my_bids",
                        objServer = "offers"
                    )
                )
            )
            listingBaseViewModel.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val filters = ld.value.data.filters.filter {
                        it.value != "" &&
                                it.interpretation?.isNotBlank() == true
                    }

                    add(
                        NavigationItem(
                            title = filterString,
                            icon = drawables.filterIcon,
                            tint = colors.black,
                            hasNews = filters.find { it.interpretation?.isNotEmpty() == true } != null,
                            badgeCount = if (filters.isNotEmpty()) filters.size else null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.FILTERS)
                            }
                        )
                    )
                    add(
                        NavigationItem(
                            title = sortString,
                            icon = drawables.sortIcon,
                            tint = colors.black,
                            hasNews = ld.value.data.sort != null,
                            badgeCount = null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
                            }
                        )
                    )
                }
            )
        }
    }

    fun onBackNavigation(){
        when(activeType.value){
            ActiveWindowListingType.CATEGORY_FILTERS -> {
                if (categoryState.categoryViewModel.searchData.value.searchCategoryID != 1L){
                    categoryState.categoryViewModel.navigateBack()
                }else{
                    _activeWindowType.value = ActiveWindowListingType.LISTING
                }
            }
            else -> {
                _activeWindowType.value = ActiveWindowListingType.LISTING
            }
        }
    }

    fun updateItem(oldItem : OfferItem){
        viewModelScope.launch {
            val offer = withContext(Dispatchers.IO) {
                getOfferById(oldItem.id)
            }

            withContext(Dispatchers.Main) {
                if (offer != null) {
                    oldItem.setNewParams(offer)
                }else{
                    oldItem.session = null
                    oldItem.state = null
                }
                setUpdateItem(null)
            }
        }
    }

    suspend fun getOfferById(offerId: Long) : Offer? {
        return try {
            val response = offerOperations.getOffer(offerId)
            response.success?.let {
                return it
            }
        } catch (_: Exception) {
            null
        }
    }
}

data class OfferRepositoryEventsImpl(
    val viewModel: MyBidsViewModel,
    val offer: OfferItem,
    val component: MyBidsComponent
): OfferRepositoryEvents {
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {}

    override fun goToProposalPage(type: ProposalType) {}

    override fun goToDynamicSettings(type: String, id: Long) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        DefaultRootComponent.Companion.goToLogin(false)
    }

    override fun goToDialog(id: Long?) {
        component.goToDialog(id)
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {
        component.goToPurchases()
    }
    override fun goToUserPage() {
        component.goToUser(offer.seller.id)
    }

    override fun openCabinetOffer() {
        component.goToOffer(offer)
    }

    override fun isHideCabinetOffer(): Boolean {
        return false
    }

    override fun scrollToBids() {}
    override fun refreshPage() {}

    override fun updateItem(item: OfferItem) {
        viewModel.updateItem(offer)
    }
}
