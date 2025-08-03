package market.engine.di

import app.cash.sqldelight.db.SqlDriver
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
import market.engine.core.network.functions.OffersListOperations
import market.engine.core.network.functions.OperationsMethods
import market.engine.core.repositories.SettingsRepository
import market.engine.core.repositories.UserRepository
import market.engine.shared.AuctionMarketDb
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object InstanceModule {
    val appModule = listOf(
        networkModule,
        databaseModule,
        operationsModule,
        repositoryModule,
    )
}

val networkModule = module {
    singleOf(::getKtorClient)
    singleOf(::APIService)
}

val databaseModule = module {
    singleOf(::createSqlDriver)
    single {
        val driver = get<SqlDriver>()
        AuctionMarketDb(driver)
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
    singleOf(::OperationsMethods)
    singleOf(::OffersListOperations)
}

val repositoryModule = module {
    singleOf(::SettingsRepository)
    singleOf(::UserRepository)
}

