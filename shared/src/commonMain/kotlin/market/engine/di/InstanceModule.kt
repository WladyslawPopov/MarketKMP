package market.engine.di

import application.market.auction_mobile.business.core.network.functions.ConversationsOperations
import application.market.auction_mobile.business.core.network.functions.FileUpload
import application.market.auction_mobile.business.core.network.functions.OfferOperations
import application.market.auction_mobile.business.core.network.functions.OrderOperations
import application.market.auction_mobile.business.core.network.functions.PrivateMessagesOperation
import application.market.auction_mobile.business.core.network.functions.SubscriptionOperations
import application.market.auction_mobile.business.core.network.functions.UserOperations
import market.engine.business.core.network.APIService
import market.engine.business.core.network.functions.CategoryOperations
import market.engine.business.core.network.paging.offer.OfferPagingRepository
import market.engine.common.getKtorClient
import market.engine.ui.home.HomeViewModel
import market.engine.ui.listing.ListingViewModel
import market.engine.ui.category.CategoryViewModel
import market.engine.ui.search.SearchViewModel
import org.koin.dsl.module

object InstanceModule {
    val appModule = module {
        single { getKtorClient() }
        single { APIService(get()) }
        single { OfferPagingRepository(get()) }

        single { CategoryOperations(get()) }
        single { ConversationsOperations(get()) }
        single { FileUpload(get()) }
        single { OfferOperations(get()) }
        single { OrderOperations(get()) }
        single { PrivateMessagesOperation(get()) }
        single { SubscriptionOperations(get()) }
        single { UserOperations(get()) }


        single { HomeViewModel(get()) }
        single { ListingViewModel(get()) }
        single { CategoryViewModel(get()) }
        single { SearchViewModel(get()) }
    }
}
