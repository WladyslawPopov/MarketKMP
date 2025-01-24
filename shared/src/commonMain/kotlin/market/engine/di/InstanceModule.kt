package market.engine.di

import market.engine.core.network.functions.ConversationsOperations
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
import market.engine.fragments.root.main.basket.BasketViewModel
import market.engine.fragments.createOffer.CreateOfferViewModel
import market.engine.fragments.createOrder.CreateOrderViewModel
import market.engine.fragments.root.main.favorites.FavViewModel
import market.engine.fragments.root.main.home.HomeViewModel
import market.engine.fragments.listing.ListingViewModel
import market.engine.fragments.root.login.LoginViewModel
import market.engine.fragments.offer.OfferViewModel
import market.engine.fragments.listing.search.SearchViewModel
import market.engine.fragments.messenger.DialogsViewModel
import market.engine.fragments.root.main.profile.ProfileViewModel
import market.engine.fragments.root.main.favorites.subscriptions.SubViewModel
import market.engine.fragments.root.main.profile.conversations.ConversationsViewModel
import market.engine.fragments.user.UserViewModel
import market.engine.fragments.user.feedbacks.FeedbacksViewModel
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
    viewModelOf(::BasketViewModel)
    viewModelOf(::CreateOrderViewModel)
    viewModelOf(::ConversationsViewModel)
    viewModelOf(::DialogsViewModel)
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

