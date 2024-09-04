package market.engine.di

import market.engine.business.core.KtorHttpClient.createKtorClient
import application.market.auction_mobile.business.core.network.functions.ConversationsOperations
import application.market.auction_mobile.business.core.network.functions.FileUpload
import application.market.auction_mobile.business.core.network.functions.OfferOperations
import application.market.auction_mobile.business.core.network.functions.OrderOperations
import application.market.auction_mobile.business.core.network.functions.PrivateMessagesOperation
import application.market.auction_mobile.business.core.network.functions.SubscriptionOperations
import application.market.auction_mobile.business.core.network.functions.UserOperations
import application.market.core.network.APIService
import application.market.auction_mobile.business.core.network.functions.CategoryOperations
import market.engine.ui.home.HomeViewModel
import org.koin.dsl.module

object InstanceModule {
    val appModule = module {
        single { createKtorClient() }
        single { APIService(get()) }

//        single { OfferPagingRepository(get()) }
//        single { ChatPagingRepository(get(),get()) }
//        single { ConversationPagingRepository(get()) }
//        single { DealsPagingRepository(get()) }
//        single { SubPagingRepository(get()) }
//        single { ReportPagingRepository(get()) }

        single { CategoryOperations(get()) }
        single { ConversationsOperations(get()) }
        single { FileUpload(get()) }
        single { OfferOperations(get()) }
        single { OrderOperations(get()) }
        single { PrivateMessagesOperation(get()) }
        single { SubscriptionOperations(get()) }
        single { UserOperations(get()) }
        single { HomeViewModel(get()) }

//        viewModel { OfferViewModel(get()) }
//        viewModel { CreateOfferViewModel(get()) }
//        viewModel { LoginViewModel(get()) }
//        viewModel { MessengerViewModel(get(), get()) }
//        viewModel { NewOrderViewModel(get()) }
//        viewModel { OrderViewModel(get()) }
//        viewModel { ProfileViewModel(get(), get(), get(), get(), get()) }
//        viewModel { ProposalViewModel(get()) }
//        viewModel { BasketViewModel(get()) }
//        viewModel { CategoryViewModel(get()) }
//        viewModel { HomeViewModel(get()) }
//        viewModel { ListingViewModel(get(), get()) }
//        viewModel { SupServViewModel(get()) }
//        viewModel { SubscriptionViewModel(get(), get(), get()) }
//        viewModel { RegistrationViewModel(get()) }
//        viewModel { BaseViewModel() }
    }
}
