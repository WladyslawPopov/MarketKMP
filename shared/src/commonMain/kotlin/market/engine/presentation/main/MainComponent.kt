package market.engine.presentation.main

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.value.Value
import market.engine.core.globalData.CategoryBaseFilters
import market.engine.core.navigation.configs.BasketConfig
import market.engine.core.navigation.configs.CategoryConfig
import market.engine.core.navigation.configs.FavoritesConfig
import market.engine.core.navigation.configs.HomeConfig
import market.engine.core.navigation.configs.MainConfig
import market.engine.core.navigation.configs.ProfileConfig
import market.engine.presentation.category.CategoryComponent
import market.engine.presentation.favorites.FavoritesComponent
import market.engine.presentation.home.HomeComponent
import market.engine.presentation.listing.ListingComponent
import market.engine.presentation.profile.ProfileComponent
import market.engine.presentation.search.SearchComponent

interface MainComponent {

    val modelNavigation: Value<ModelNavigation>

    val childStack: Value<ChildStack<*, Child>>

    val childHomeStack: Value<ChildStack<*, ChildHome>>

    val childCategoryStack: Value<ChildStack<*, ChildCategory>>

    val childBasketStack: Value<ChildStack<*, ChildBasket>>

    val childFavoritesStack: Value<ChildStack<*, ChildFavorites>>

    val childProfileStack: Value<ChildStack<*, ChildProfile>>

    val categoryData : CategoryBaseFilters

    data class ModelNavigation(
        val homeNavigation : StackNavigation<HomeConfig>,
        val categoryNavigation : StackNavigation<CategoryConfig>,
        val basketNavigation : StackNavigation<BasketConfig>,
        val favoritesNavigation : StackNavigation<FavoritesConfig>,
        val profileNavigation : StackNavigation<ProfileConfig>,
    )

    sealed class Child {
        data object HomeChild : Child()
        data object CategoryChild : Child()
        data object BasketChild : Child()
        data object FavoritesChild : Child()
        data object ProfileChild : Child()
    }

    sealed class ChildHome {
        class HomeChild(val component: HomeComponent) : ChildHome()
    }

    sealed class ChildCategory {
        class CategoryChild(val component: CategoryComponent) : ChildCategory()
        class ListingChild(val component: ListingComponent) : ChildCategory()
        class SearchChild(val component: SearchComponent) : ChildCategory()
    }

    sealed class ChildBasket {
        class BasketChild(val component: market.engine.presentation.basket.BasketComponent) : ChildBasket()
    }

    sealed class ChildFavorites {
        class FavoritesChild(val component: FavoritesComponent) : ChildFavorites()
    }

    sealed class ChildProfile {
        class ProfileChild(val component: ProfileComponent) : ChildProfile()
    }

    fun navigateToBottomItem(config: MainConfig)

    fun backPressedBottomItem()

    fun goToLogin()
}








