package market.engine.di

import market.engine.core.network.functions.ConversationsOperations
import market.engine.core.network.functions.FileUpload
import market.engine.core.network.functions.OfferOperations
import market.engine.core.network.functions.OrderOperations
import market.engine.core.network.functions.PrivateMessagesOperation
import market.engine.core.network.functions.SubscriptionOperations
import market.engine.core.network.functions.UserOperations
import market.engine.common.createSettings
import market.engine.core.network.APIService
import market.engine.core.network.functions.CategoryOperations
import market.engine.common.createSqlDriver
import market.engine.common.getKtorClient
import market.engine.core.repositories.SAPIRepository
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import market.engine.presentation.createOffer.CreateOfferContent
import market.engine.presentation.createOffer.CreateOfferViewModel
import market.engine.presentation.favorites.FavViewModel
import market.engine.presentation.home.HomeViewModel
import market.engine.presentation.listing.ListingViewModel
import market.engine.presentation.login.LoginViewModel
import market.engine.presentation.offer.OfferViewModel
import market.engine.presentation.listing.search.SearchViewModel
import market.engine.presentation.profile.ProfileViewModel
import market.engine.presentation.subscriptions.SubViewModel
import market.engine.presentation.user.UserViewModel
import market.engine.presentation.user.feedbacks.FeedbacksViewModel
import market.engine.shared.MarketDB
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

object InstanceModule {
    val appModule = listOf(
        networkModule,
        databaseModule,
        operationsModule,
        repositoryModule,
        viewModelModule,
    )
}

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::ListingViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::FavViewModel)
    viewModelOf(::SubViewModel)
    viewModelOf(::OfferViewModel)
    viewModelOf(::UserViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::FeedbacksViewModel)
    viewModelOf(::CreateOfferViewModel)
}

val networkModule = module {
    singleOf(::getKtorClient)
    singleOf(::APIService)
}

val databaseModule = module {
    singleOf(::createSqlDriver)
    single {
        MarketDB.invoke(get())
    }
    singleOf(::createSettings)
}

val operationsModule = module {
    singleOf(::CategoryOperations)
    singleOf(::ConversationsOperations)
    singleOf(::FileUpload)
    singleOf(::OfferOperations)
    singleOf(::OrderOperations)
    singleOf(::PrivateMessagesOperation)
    singleOf(::SubscriptionOperations)
    singleOf(::UserOperations)
}

val repositoryModule = module {
    singleOf(::SAPIRepository)
    singleOf(::SettingsRepository)
    singleOf(::UserRepository)
}

