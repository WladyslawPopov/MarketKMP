package market.engine.di

import application.market.agora.business.core.network.functions.ConversationsOperations
import application.market.agora.business.core.network.functions.FileUpload
import application.market.agora.business.core.network.functions.OfferOperations
import application.market.agora.business.core.network.functions.OrderOperations
import application.market.agora.business.core.network.functions.PrivateMessagesOperation
import application.market.agora.business.core.network.functions.SubscriptionOperations
import application.market.agora.business.core.network.functions.UserOperations
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.common.createSqlDriver
import market.engine.common.getKtorClient
import market.engine.presentation.category.CategoryViewModel
import market.engine.presentation.home.HomeViewModel
import market.engine.presentation.listing.ListingViewModel
import market.engine.presentation.search.SearchViewModel
import market.engine.shared.MarketDB
import org.koin.dsl.module

object InstanceModule {
    val appModule = module {
        single { getKtorClient() }
        single { APIService(get()) }
        single { createSqlDriver() }
        single { MarketDB(get()) } // Register MarketDB


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
