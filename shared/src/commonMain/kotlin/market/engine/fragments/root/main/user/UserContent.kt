package market.engine.fragments.root.main.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import market.engine.common.Platform
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.constants.alphaBars
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.drawables
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.items.NavigationItem
import market.engine.core.data.items.NavigationItemUI
import market.engine.core.data.items.SimpleAppBarData
import market.engine.core.data.items.TabWithIcon
import market.engine.core.data.types.PlatformWindowType
import market.engine.core.data.types.ReportPageType
import market.engine.fragments.base.EdgeToEdgeScaffold
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.user.feedbacks.FeedbacksContent
import market.engine.widgets.tabs.TabRow
import market.engine.fragments.base.screens.OnError
import market.engine.fragments.base.screens.NoItemsFoundLayout
import market.engine.widgets.bars.UserPanel
import market.engine.widgets.bars.appBars.SimpleAppBar
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.rows.UserRow
import market.engine.widgets.tabs.PageTab
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserContent(
    component: UserComponent,
    modifier: Modifier
) {
    val modelState = component.model.subscribeAsState()
    val viewModel = modelState.value.userViewModel
    val user by viewModel.userInfo.collectAsState()
    val blackList by viewModel.statusList.collectAsState()

    val isLoading by viewModel.isShowProgress.collectAsState()
    val isError by viewModel.errorMessage.collectAsState()

    val feedbacksPages by component.feedbacksPages.subscribeAsState()

    val toastItem by viewModel.toastItem.collectAsState()

    val error : (@Composable () -> Unit)? = remember(isError) {
        if (isError.humanMessage != "") {
            {
                OnError(isError) {
                    viewModel.refresh()
                   viewModel.getUserInfo()
                }
            }
        }else{
            null
        }
    }

    val isVisibleUserPanel = remember { viewModel.isVisibleUserPanel }

    val selectedTabIndex = remember {
        mutableStateOf(0)
    }

    val tabs = listOf(
        TabWithIcon(
            stringResource(strings.allFeedbackToUserLabel),
        ),
        TabWithIcon(
            stringResource(strings.fromBuyerLabel),
        ),
        TabWithIcon(
            stringResource(strings.fromSellerLabel),
        ),
        TabWithIcon(
            stringResource(strings.fromUsersLabel),
        ),
        TabWithIcon(
            stringResource(strings.aboutMeLabel),
        )
    )

    LaunchedEffect(isVisibleUserPanel.value) {
        snapshotFlow {
            isVisibleUserPanel.value
        }.collectLatest { value ->
            viewModel.isVisibleUserPanel.value = value
        }
    }

    EdgeToEdgeScaffold(
       topBar = {
           AnimatedVisibility(
               visible = isVisibleUserPanel.value,
               enter = expandVertically(),
               exit = shrinkVertically()
           )
           {
               Column (
                   modifier = Modifier.padding(top = TopAppBarDefaults.TopAppBarExpandedHeight).fillMaxWidth(),
                   horizontalAlignment = Alignment.Start,
                   verticalArrangement = Arrangement.spacedBy(dimens.smallPadding, Alignment.CenterVertically)
               ) {
                   NavigationArrowButton {
                       component.onBack()
                   }

                   UserPanel(
                       user = user,
                       goToUser = {
                           isVisibleUserPanel.value = !isVisibleUserPanel.value
                       },
                       goToAllLots = {
                           component.selectAllOffers(user)
                       },
                       goToAboutMe = {
                           component.onTabSelect(4)
                       },
                       addToSubscriptions = { callback ->
                           if (UserData.token != "") {
                               viewModel.addNewSubscribe(
                                   LD(),
                                   SD().copy(
                                       userLogin = user.login,
                                       userID = user.id,
                                       userSearch = true
                                   ),
                                   onSuccess = {
                                       viewModel.getUserInfo()
                                   },
                                   errorCallback = { es ->
                                       callback(es)
                                   }
                               )
                           } else {
                               goToLogin(false)
                           }
                       },
                       goToSubscriptions = {
                           component.goToSubscriptions()
                       },
                       goToSettings = {
                           component.goToSettings(it)
                       },
                       isBlackList = blackList
                   )
               }
           }

           AnimatedVisibility(
               visible = !isVisibleUserPanel.value,
               enter = expandVertically(),
               exit = shrinkVertically()
           )
           {
               SimpleAppBar(
                   modifier = Modifier.clickable{
                       isVisibleUserPanel.value = true
                   },
                   data = SimpleAppBarData(
                       listItems = listOf(
                           NavigationItemUI(
                               NavigationItem(
                                   title = "",
                                   hasNews = false,
                                   isVisible = (Platform().getPlatform() == PlatformWindowType.DESKTOP),
                                   badgeCount = null,
                               ),
                               icon = drawables.recycleIcon,
                               tint = colors.inactiveBottomNavIconColor,
                               onClick = {
                                   viewModel.getUserInfo()
                               }
                           ),
                           NavigationItemUI(
                               NavigationItem(
                                   title = stringResource(strings.searchUserStringChoice),
                                   hasNews = false,
                                   badgeCount = null,
                               ),
                               icon = drawables.iconArrowDown,
                               tint = colors.black,
                               onClick = {
                                   isVisibleUserPanel.value = true
                               }
                           )
                       ),
                       onBackClick = {
                           component.onBack()
                       }
                   ),
                   color = colors.primaryColor
               ) {
                   if (!isVisibleUserPanel.value) {
                       UserRow(user)
                   }
               }
           }

           TabRow(
               tabs,
               selectedTab = selectedTabIndex.value,
               containerColor = colors.primaryColor,
               modifier = Modifier
                   .background(colors.primaryColor.copy(alphaBars))
                   .fillMaxWidth(),
           ) { index, tab ->
               PageTab(
                   tab = tab,
                   selectedTab = selectedTabIndex.value,
                   currentIndex = index,
                   textStyle = MaterialTheme.typography.titleSmall,
                   modifier = Modifier.clickable {
                       component.onTabSelect(index)
                   },
               )
           }
       },
       modifier = modifier.fillMaxSize(),
       isLoading = isLoading,
       error = error,
       toastItem = toastItem,
       onRefresh = {
           viewModel.refresh()
           viewModel.getUserInfo()
       }
    ) { contentPadding ->
        if (!user.markedAsDeleted) {
            ChildPages(
                modifier = Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                    .fillMaxSize(),
                pages = feedbacksPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    val select = when (it) {
                        0 -> ReportPageType.ALL_REPORTS
                        1 -> ReportPageType.FROM_BUYERS
                        2 -> ReportPageType.FROM_SELLERS
                        3 -> ReportPageType.FROM_USER
                        4 -> ReportPageType.ABOUT_ME
                        else -> {
                            ReportPageType.ALL_REPORTS
                        }
                    }
                    selectedTabIndex.value = it
                    component.selectFeedbackPage(select)
                }
            ) { _, page ->
                FeedbacksContent(
                    component = page,
                    aboutMe = user.aboutMe,
                    onScrollDirectionChange = { isAtTop ->
                        isVisibleUserPanel.value = isAtTop
                    }
                )
            }
        } else {
            NoItemsFoundLayout(
                title = stringResource(strings.userDeletedLabel),
                modifier = Modifier.padding(contentPadding)
            ) {
                viewModel.getUserInfo()
            }
        }
    }
}
