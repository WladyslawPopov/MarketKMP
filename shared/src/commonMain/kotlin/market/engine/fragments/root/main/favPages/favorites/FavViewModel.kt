package market.engine.fragments.root.main.favPages.favorites

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import market.engine.core.data.baseFilters.Filter
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
import market.engine.core.data.states.CategoryState
import market.engine.core.data.types.ActiveWindowListingType
import market.engine.core.data.types.CreateOfferType
import market.engine.core.data.types.FavScreenType
import market.engine.core.data.types.LotsType
import market.engine.core.data.types.ProposalType
import market.engine.core.network.networkObjects.Offer
import market.engine.core.repositories.OfferRepository
import market.engine.core.repositories.PagingRepository
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.base.listing.ListingBaseViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToDynamicSettings
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.widgets.filterContents.categories.CategoryViewModel
import org.jetbrains.compose.resources.getString


class FavViewModel(
    val favType : FavScreenType,
    val idList : Long?,
    component: FavoritesComponent
) : CoreViewModel() {

    private val pagingRepository: PagingRepository<Offer> = PagingRepository()

    val listingBaseViewModel = ListingBaseViewModel(
        deleteSelectedItems = {
            deleteSelectsItems()
        }
    )

    val listingData = listingBaseViewModel.listingData

    val selectItems = listingBaseViewModel.selectItems

    val activeType = listingBaseViewModel.activeWindowType

    val offerListFilter = Filter(
        "list_id",
        "$idList",
        "",
        null
    )

    val filtersCategoryState = CategoryState(
        activeType.value == ActiveWindowListingType.CATEGORY_FILTERS,
        CategoryViewModel(
            isFilters = true,
        )
    )

    val pagingParamsFlow: Flow<ListingData> = combine(
        listingData,
        updatePage
    ) { listingData, _ ->
        resetScroll()
        listingData
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<OfferRepository>> = pagingParamsFlow
        .flatMapLatest { listingParams ->
            pagingRepository.getListing(
                listingParams,
                apiService,
                Offer.serializer()
            ){ tc ->
                listingBaseViewModel.setTotalCount(tc)
            }.map { pagingData ->
                pagingData.map { offer ->
                    OfferRepository(
                        offer,
                        listingParams,
                        OfferRepositoryEventsImpl(this, component),
                        this
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
            val ld = when (favType) {
                FavScreenType.FAVORITES -> {
                    LD(
                        filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES),
                        methodServer = "get_cabinet_listing_watched_by_me",
                        objServer = "offers"
                    )
                }

                FavScreenType.NOTES -> {
                    LD(
                        filters = OfferFilters.getByTypeFilter(LotsType.FAVORITES),
                        methodServer = "get_cabinet_listing_my_notes",
                        objServer = "offers"
                    )
                }

                FavScreenType.FAV_LIST -> {
                    LD(
                        filters = OfferFilters.getByTypeFilter(null) + offerListFilter,
                        methodServer = "get_cabinet_listing_in_list",
                        objServer = "offers"
                    )
                }

                else -> {
                    LD()
                }
            }

            listingBaseViewModel.setListingData(
                listingBaseViewModel.listingData.value.copy(
                    data = ld
                )
            )

            listingBaseViewModel.setListItemsFilterBar(
                buildList {
                    val filterString = getString(strings.filter)
                    val sortString = getString(strings.sort)
                    val filters = ld.filters.filter {
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
                            hasNews = ld.sort != null,
                            badgeCount = null,
                            onClick = {
                                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.SORTING)
                            }
                        )
                    )
                }
            )

            analyticsHelper.reportEvent("open_favorites", mapOf("type" to favType.name))
        }
    }

    fun onBackNavigation(){
        when(activeType.value){
            ActiveWindowListingType.CATEGORY_FILTERS -> {
                if (filtersCategoryState.categoryViewModel.searchData.value.searchCategoryID != 1L){
                    filtersCategoryState.categoryViewModel.navigateBack()
                }else{
                    listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
                }
            }
            else -> {
                listingBaseViewModel.setActiveWindowType(ActiveWindowListingType.LISTING)
            }
        }
    }

    fun deleteSelectsItems() {
        viewModelScope.launch {
            selectItems.value.forEach { item ->
                val body = HashMap<String, JsonElement>()

                val type = when (favType) {
                    FavScreenType.NOTES -> {
                        "delete_note"
                    }

                    FavScreenType.FAVORITES -> {
                        "unwatch"
                    }

                    FavScreenType.FAV_LIST -> {
                        body["offers_list_id"] = JsonPrimitive(idList)
                        "remove_from_list"
                    }

                    else -> {
                        ""
                    }
                }

                postOperationFields(
                    item,
                    type,
                    "offers",
                    body,
                    onSuccess = {
                        listingBaseViewModel.clearSelectedItems()
                        updatePage()
                    },
                    errorCallback = {

                    }
                )
            }
        }
    }
}

data class OfferRepositoryEventsImpl(
    val viewModel: FavViewModel,
    val component: FavoritesComponent
): OfferRepositoryEvents {
    override fun goToCreateOffer(
        type: CreateOfferType,
        catpath: List<Long>,
        id: Long,
        externalImages: List<String>?
    ) {
        component.goToCreateOffer(type, id)
    }

    override fun goToProposalPage(
        offerId: Long,
        type: ProposalType
    ) {
        component.goToProposal(type, offerId)
    }

    override fun openCabinetOffer(offer: OfferItem) {
        if (viewModel.selectItems.value.isNotEmpty()) {
            if (viewModel.selectItems.value.contains(offer.id)) {
                viewModel.listingBaseViewModel.removeSelectItem(offer.id)
            } else {
                viewModel.listingBaseViewModel.addSelectItem(offer.id)
            }
        } else {
            component.goToOffer(offer)
        }
    }

    override fun goToDynamicSettings(type: String, id: Long) {
        goToDynamicSettings(type, id, null)
    }

    override fun goToLogin() {
        goToLogin(false)
    }
    override fun scrollToBids() {}
    override fun refreshPage() {}
    override fun updateBidsInfo(item: OfferItem) { }
    override fun goToDialog(id: Long?) {}
    override fun goToCreateOrder(item: Pair<Long, List<SelectedBasketItem>>) {}
    override fun goToUserPage(sellerId: Long) { }
}
