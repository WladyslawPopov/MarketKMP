package market.engine.fragments.root.main.user

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.serialization.Serializable
import market.engine.core.data.baseFilters.ListingData
import market.engine.core.data.types.DealTypeGroup
import market.engine.core.network.networkObjects.User
import market.engine.core.data.types.ReportPageType
import market.engine.fragments.root.main.user.feedbacks.DefaultFeedbacksComponent
import market.engine.fragments.root.main.user.feedbacks.FeedbacksComponent

interface UserComponent {
    val model : Value<Model>

    data class Model(
        val userId: Long,
        var isClickedAboutMe: Boolean,
        val userViewModel: UserViewModel,
        val backHandler: BackHandler
    )

    val feedbacksPages: Value<ChildPages<*, FeedbacksComponent>>

    fun updateUserInfo()

    fun selectAllOffers(user : User)

    fun onBack()

    fun selectFeedbackPage(type: ReportPageType)

    fun onTabSelect(index: Int)

    fun goToSubscriptions()
}

class DefaultUserComponent(
    userId: Long,
    isClickedAboutMe: Boolean = false,
    componentContext: ComponentContext,
    val goToListing: (ListingData) -> Unit,
    val navigateBack: () -> Unit,
    val navigateToSnapshot: (Long) -> Unit,
    val navigateToOrder: (Long, DealTypeGroup) -> Unit,
    val navigateToUser: (Long) -> Unit,
    val navigateToSubscriptions: () -> Unit,
) : UserComponent, ComponentContext by componentContext {

    val viewModel : UserViewModel = UserViewModel()

    private val _model = MutableValue(
        UserComponent.Model(
            userId = userId,
            isClickedAboutMe = isClickedAboutMe,
            userViewModel = viewModel,
            backHandler = backHandler
        )
    )

    override val model = _model

    init {
        updateUserInfo()
    }

    override fun updateUserInfo() {
        model.value.userViewModel.getUserInfo(model.value.userId)
    }

    override fun selectAllOffers(user : User) {
        val ld = ListingData()
        val searchData = ld.searchData
        searchData.value.userID = user.id
        searchData.value.userSearch = true
        searchData.value.userLogin = user.login
        goToListing(ld)
    }

    override fun onBack() {
        navigateBack()
    }

    private val navigation = PagesNavigation<FeedbackConfig>()

    override val feedbacksPages: Value<ChildPages<*, FeedbacksComponent>> by lazy {
        childPages(
            source = navigation,
            serializer = FeedbackConfig.serializer(),
            handleBackButton = true,
            initialPages = {
                Pages(
                    listOf(
                        FeedbackConfig(type = ReportPageType.ALL_REPORTS),
                        FeedbackConfig(type = ReportPageType.FROM_BUYERS),
                        FeedbackConfig(type = ReportPageType.FROM_SELLERS),
                        FeedbackConfig(type = ReportPageType.FROM_USER),
                        FeedbackConfig(type = ReportPageType.ABOUT_ME),
                    ),
                    selectedIndex = if (model.value.isClickedAboutMe) 4 else 0,
                )
            },
            key = "FeedbacksStack",
            childFactory = ::itemFeedback
        )
    }

    private fun itemFeedback(config: FeedbackConfig, componentContext: ComponentContext) : FeedbacksComponent {
        return DefaultFeedbacksComponent(
            userId = model.value.userId,
            type = config.type,
            componentContext = componentContext,
            navigateToSnapshot = {
                navigateToSnapshot(it)
            },
            navigateToOrder = { id, type ->
                navigateToOrder(id, type)
            },
            navigateToUser = {
                navigateToUser(it)
            }
        )
    }

    override fun onTabSelect(index: Int) {
        val select = when(index){
            0 -> ReportPageType.ALL_REPORTS
            1 -> ReportPageType.FROM_BUYERS
            2 -> ReportPageType.FROM_SELLERS
            3 -> ReportPageType.FROM_USER
            4 -> ReportPageType.ABOUT_ME
            else -> {
                ReportPageType.ALL_REPORTS
            }
        }

        selectFeedbackPage(select)
    }

    override fun goToSubscriptions() {
        navigateToSubscriptions()
    }

    override fun selectFeedbackPage(type: ReportPageType) {
        when (type) {
            ReportPageType.ALL_REPORTS -> {
                navigation.select(0)
            }
            ReportPageType.FROM_BUYERS -> {
                navigation.select(1)
            }
            ReportPageType.FROM_SELLERS -> {
                navigation.select(2)
            }
            ReportPageType.FROM_USER -> {
                navigation.select(3)
            }
            ReportPageType.ABOUT_ME -> {
                navigation.select(4)
            }
        }
    }
}

@Serializable
data class FeedbackConfig(
    @Serializable
    val type: ReportPageType
)


