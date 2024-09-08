package market.engine.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import market.engine.ui.home.HomeContent
import market.engine.ui.search.SearchContent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.launch
import market.engine.business.constants.ThemeResources.colors
import market.engine.business.constants.ThemeResources.drawables
import market.engine.business.constants.ThemeResources.strings
import market.engine.business.items.NavigationItem
import market.engine.business.types.WindowSizeClass
import market.engine.business.util.getWindowSizeClass
import market.engine.ui.basket.BasketContent
import market.engine.ui.favorites.FavoritesContent
import market.engine.ui.profile.ProfileContent
import market.engine.widgets.DrawerContent
import market.engine.widgets.appbars.HomeAppBar
import market.engine.widgets.bottombars.getBottomNavBar
import market.engine.widgets.bottombars.getRailNavBar
import okio.FileSystem
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalCoilApi::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier
) {

    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }

    val childStack by component.childStack.subscribeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val windowClass = getWindowSizeClass()
    val showNavigationRail = windowClass == WindowSizeClass.Big

    val currentScreen = when (childStack.active.instance) {
        is RootComponent.Child.HomeChild -> "Home"
        is RootComponent.Child.SearchChild -> "Search"
        is RootComponent.Child.BasketChild -> "Basket"
        is RootComponent.Child.FavoritesChild -> "Favorites"
        is RootComponent.Child.ProfileChild -> "Profile"
    }
    
    val listItems = listOf(
        NavigationItem(
            title = stringResource(strings.homeTitle),
            icon =  drawables.home,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.searchTitle),
            icon = drawables.search,
            tint = colors.black,
            hasNews = false,
            badgeCount = null
        ),
        NavigationItem(
            title = stringResource(strings.basketTitle),
            icon = drawables.basketIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = 6
        ),
        NavigationItem(
            title = stringResource(strings.favoritesTitle),
            icon = drawables.favoritesIcon,
            tint = colors.black,
            hasNews = false,
            badgeCount = 687
        ),
        NavigationItem(
            title = stringResource(strings.profileTitleBottom),
            icon = drawables.profileIcon,
            tint = colors.black,
            hasNews = true,
            badgeCount = null
        )
    )

    fun openMenu(){
        scope.launch {
            drawerState.open()
        }
    }
    
    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.backgroundColor
    ){
        ModalNavigationDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerContent = { DrawerContent(drawerState, scope) },
            gesturesEnabled = drawerState.isOpen,
        ){
            Scaffold(
                topBar = {
                    when(currentScreen){
                        "Home" -> HomeAppBar(modifier,showNavigationRail) { openMenu() }
                        "Search" -> {}
                        "Basket" -> {}
                        "Favorites" -> {}
                        "Profile" -> {}
                    }
                },
                bottomBar = {
                    if (!showNavigationRail) {
                        getBottomNavBar(component, modifier,listItems)
                    } else {
                        getRailNavBar(component, modifier, listItems) { openMenu() }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { innerPadding ->
                Children(
                    stack = childStack,
                    modifier = modifier.then(
                        if (showNavigationRail) modifier.padding(start = 82.dp, top = 70.dp)
                        else modifier.padding(innerPadding))
                        .fillMaxSize(),
                    animation = stackAnimation(fade())
                ) { child ->
                    when (val screen = child.instance) {
                        is RootComponent.Child.HomeChild -> HomeContent(
                            screen.component,
                            modifier
                        )
                        is RootComponent.Child.SearchChild -> SearchContent(screen.component)
                        is RootComponent.Child.BasketChild -> BasketContent(screen.component)
                        is RootComponent.Child.FavoritesChild -> FavoritesContent(screen.component)
                        is RootComponent.Child.ProfileChild -> ProfileContent(screen.component)
                    }
                }
            }
        }
    }
}

fun navigateFromBottomBar(index: Int, component: RootComponent){
    when(index){
        0 -> component.navigateTo(Config.Home)
        1 -> component.navigateTo(Config.Search(itemId = 1))
        2 -> component.navigateTo(Config.Basket)
        3 -> component.navigateTo(Config.Favorites)
        4 -> component.navigateTo(Config.Profile)
    }
}

fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder().maxSizePercent(context,0.3)
                .strongReferencesEnabled(true)
                .build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .diskCache {
            newDiskCache()
        }.crossfade(true).logger(DebugLogger()).build()
}

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY)
        .maxSizeBytes(1024L*1024*1024).build()
}



