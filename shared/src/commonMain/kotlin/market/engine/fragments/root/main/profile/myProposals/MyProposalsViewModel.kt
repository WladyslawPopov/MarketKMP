package market.engine.fragments.root.main.profile.myProposals

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
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
import market.engine.core.utils.getCurrentDate
import market.engine.core.utils.parseToOfferItem
import market.engine.core.utils.setNewParams
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.ListingBaseViewModel
import market.engine.fragments.root.DefaultRootComponent
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform.getKoin

class MyProposalsViewModel(
    val type: LotsType,
    val component: MyProposalsComponent
) : CoreViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    private val offerOperations : OfferOperations by lazy { getKoin().get() }

    val listingBaseViewModel = ListingBaseViewModel()
    val ld = listingBaseViewModel.listingData
    val activeType = listingBaseViewModel.activeWindowType

    val categoryState = CategoryState(
        activeType.value == ActiveWindowListingType.CATEGORY_FILTERS,
        CategoryViewModel(isFilters = true)
    )

    val pagingParamsFlow: Flow<ListingData> = combine(
        ld,
        updatePage
    ) { listingData, _ ->
        listingData
    }

    private val filtersCategoryModel = CategoryViewModel(
        isFilters = true,
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
                            offer= item,
                            events = OfferRepositoryEventsImpl(this, item, component),
                            this
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
                        methodServer = "get_cabinet_listing_my_price_proposals",
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
                if (filtersCategoryModel.categoryId.value != 1L){
                    filtersCategoryModel.navigateBack()
                }else{
                    listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
                }
            }
            else -> {
                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
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
                if (!isHideItem(oldItem)) {
                    updatePage()
                }
                setUpdateItem(null)
            }
        }
    }

    fun isHideItem(offer: OfferItem): Boolean {
        return when (type) {
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
    val viewModel: MyProposalsViewModel,
    val offer: OfferItem,
    val component: MyProposalsComponent
): OfferRepositoryEvents {
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
    }

    override fun goToProposalPage(type: ProposalType) {
        component.goToProposal(offer.id, type)
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        DefaultRootComponent.Companion.goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        DefaultRootComponent.Companion.goToLogin(false)
    }

    override fun goToDialog(id: Long?) {
        component.goToDialog(id)
    }

    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {}
    override fun goToUserPage() {
        component.goToUser(offer.seller.id)
    }

    override fun openCabinetOffer() {
        component.goToOffer(offer)
    }

    override fun isHideCabinetOffer(): Boolean {
        return viewModel.isHideItem(offer)
    }

    override fun scrollToBids() {}
    override fun refreshPage() {}

    override fun updateItem(item: OfferItem) {
        viewModel.updateItem(offer)
    }
}
