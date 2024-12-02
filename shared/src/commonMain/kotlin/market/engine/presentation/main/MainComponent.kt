package market.engine.presentation.main

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.Value
import market.engine.core.navigation.children.ChildBasket
import market.engine.core.navigation.children.ChildCategory
import market.engine.core.navigation.children.ChildFavorites
import market.engine.core.navigation.children.ChildHome
import market.engine.core.navigation.children.ChildMain
import market.engine.core.navigation.children.ChildProfile
import market.engine.core.navigation.configs.BasketConfig
import market.engine.core.navigation.configs.CategoryConfig
import market.engine.core.navigation.configs.FavoritesConfig
import market.engine.core.navigation.configs.HomeConfig
import market.engine.core.navigation.configs.MainConfig
import market.engine.core.navigation.configs.ProfileConfig
import market.engine.core.types.FavScreenType
import market.engine.core.types.LotsType
import market.engine.core.types.ProfileScreenType
import market.engine.presentation.profileMyOffers.MyOffersComponent

interface MainComponent {

    val modelNavigation: Value<ModelNavigation>

    val childMainStack: Value<ChildStack<*, ChildMain>>

    val childHomeStack: Value<ChildStack<*, ChildHome>>

    val childCategoryStack: Value<ChildStack<*, ChildCategory>>

    val childBasketStack: Value<ChildStack<*, ChildBasket>>

    val childFavoritesStack: Value<ChildStack<*, ChildFavorites>>

    val childProfileStack: Value<ChildStack<*, ChildProfile>>

    val myOffersPages: Value<ChildPages<*, MyOffersComponent>>

    val homeStack : Value<MutableList<HomeConfig>>

    val categoryStack : Value<MutableList<CategoryConfig>>

    val basketStack : Value<MutableList<BasketConfig>>

    val favoritesStack : Value<MutableList<FavScreenType>>

    val profileStack : Value<MutableList<ProfileScreenType>>

    data class ModelNavigation(
        val homeNavigation : StackNavigation<HomeConfig>,
        val categoryNavigation : StackNavigation<CategoryConfig>,
        val basketNavigation : StackNavigation<BasketConfig>,
        val favoritesNavigation : StackNavigation<FavoritesConfig>,
        val profileNavigation : StackNavigation<ProfileConfig>,
    )

    fun selectMyOfferPage(type: LotsType)

    fun navigateToBottomItem(config: MainConfig)

    fun goToLogin()
}








