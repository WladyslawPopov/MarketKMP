package market.engine.di

import application.market.agora.business.core.network.functions.ConversationsOperations
import application.market.agora.business.core.network.functions.FileUpload
import application.market.agora.business.core.network.functions.OfferOperations
import application.market.agora.business.core.network.functions.OrderOperations
import application.market.agora.business.core.network.functions.PrivateMessagesOperation
import application.market.agora.business.core.network.functions.SubscriptionOperations
import application.market.agora.business.core.network.functions.UserOperations
import market.engine.common.AnalyticsFactory
import market.engine.common.createSettings
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.core.network.paging.offer.OfferPagingRepository
import market.engine.common.createSqlDriver
import market.engine.common.getKtorClient
import market.engine.core.baseFilters.CategoryBaseFilters
import market.engine.core.repositories.SAPIRepository
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import market.engine.presentation.category.CategoryViewModel
import market.engine.presentation.home.HomeViewModel
import market.engine.presentation.listing.ListingViewModel
import market.engine.presentation.login.LoginViewModel
import market.engine.presentation.main.MainViewModel
import market.engine.presentation.search.SearchViewModel
import market.engine.shared.MarketDB
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

object InstanceModule {
    val appModule = listOf(
        networkModule,
        databaseModule,
        operationsModule,
        repositoryModule,
        viewModelModule,
        filtersModule
    )
}

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ListingViewModel(get(), get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { LoginViewModel(get()) }
}

val networkModule = module {
    single { getKtorClient() }
    single { APIService(get()) }
    single { AnalyticsFactory.createAnalyticsHelper() }
}

val databaseModule = module {
    single { createSqlDriver() }
    single { MarketDB(get()) }
    single { createSettings() }
}

val operationsModule = module {
    single { CategoryOperations(get(), get()) }
    single { ConversationsOperations(get()) }
    single { FileUpload(get()) }
    single { OfferOperations(get()) }
    single { OrderOperations(get()) }
    single { PrivateMessagesOperation(get()) }
    single { SubscriptionOperations(get()) }
    single { UserOperations(get()) }
}

val repositoryModule = module {
    single { OfferPagingRepository(get()) }
    single { SAPIRepository() }
    single { SettingsRepository(get()) }
    single { UserRepository(get(), get(), get()) }
}

val filtersModule = module {
    single { CategoryBaseFilters }
}
