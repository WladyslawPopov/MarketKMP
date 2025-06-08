package market.engine.fragments.root.main.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import market.engine.core.data.baseFilters.LD
import market.engine.core.data.baseFilters.SD
import market.engine.core.data.globalData.ThemeResources.colors
import market.engine.core.data.globalData.ThemeResources.dimens
import market.engine.core.data.globalData.ThemeResources.strings
import market.engine.core.data.globalData.UserData
import market.engine.core.data.globalData.isBigScreen
import market.engine.core.data.items.Tab
import market.engine.core.data.types.ReportPageType
import market.engine.core.network.ServerErrorException
import market.engine.fragments.base.BaseContent
import market.engine.fragments.root.DefaultRootComponent.Companion.goToLogin
import market.engine.fragments.root.main.user.feedbacks.FeedbacksContent
import market.engine.widgets.dialogs.CreateSubscribeDialog
import market.engine.fragments.base.BackHandler
import market.engine.widgets.tabs.TabRow
import market.engine.fragments.base.onError
import market.engine.fragments.base.showNoItemLayout
import market.engine.widgets.bars.UserPanel
import market.engine.widgets.rows.ColumnWithScrollBars
import market.engine.widgets.rows.UserRow
import market.engine.widgets.tabs.PageTab
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserContent(
    component: UserComponent,
    modifier: Modifier
) {
    val modelState = component.model.subscribeAsState()
    val userViewModel = modelState.value.userViewModel
    val user = userViewModel.userInfo.collectAsState()
    val blackList = userViewModel.statusList.collectAsState()

    val isLoading = userViewModel.isShowProgress.collectAsState()
    val isError = userViewModel.errorMessage.collectAsState()

    val feedbacksPages by component.feedbacksPages.subscribeAsState()

    val error : (@Composable () -> Unit)? = if (isError.value.humanMessage != "") {
        {
            onError(isError.value) {
                userViewModel.onError(ServerErrorException())
                component.updateUserInfo()
            }
        }
    }else{
        null
    }

    BackHandler(modelState.value.backHandler){
        component.onBack()
    }

    val isVisibleUserPanel = remember { userViewModel.isVisibleUserPanel }

    val selectedTabIndex = remember {
        mutableStateOf(0)
    }

    val tabs = listOf(
        Tab(
            stringResource(strings.allFeedbackToUserLabel),
        ),
        Tab(
            stringResource(strings.fromBuyerLabel),
        ),
        Tab(
            stringResource(strings.fromSellerLabel),
        ),
        Tab(
            stringResource(strings.fromUsersLabel),
        ),
        Tab(
            stringResource(strings.aboutMeLabel),
        )
    )

    LaunchedEffect(isVisibleUserPanel.value) {
        snapshotFlow {
            isVisibleUserPanel.value
        }.collectLatest { value ->
            userViewModel.isVisibleUserPanel.value = value
        }
    }

    BaseContent(
        topBar = {
            UserAppBar(
                titleContent = {
                    user.value?.let { UserRow(it) }
                },
                isVisibleUserPanel = isVisibleUserPanel.value,
                onUserSliderClick = {
                    isVisibleUserPanel.value = !isVisibleUserPanel.value
                },
                onBackClick = {
                    component.onBack()
                },
                onRefresh = {
                    component.updateUserInfo()
                }
            )
        },
       modifier = modifier.fillMaxSize(),
       isLoading = isLoading.value,
       error = error,
       toastItem = userViewModel.toastItem,
       onRefresh = {
           component.updateUserInfo()
       },
    ) {
        if(user.value?.markedAsDeleted != true) {
            Column {
                AnimatedVisibility(
                    isVisibleUserPanel.value,
                    modifier = Modifier.background(colors.white).fillMaxWidth(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    ColumnWithScrollBars(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if(isBigScreen.value) 250.dp else 320.dp)
                    ) {
                        val errorString = remember { mutableStateOf("") }

                        UserPanel(
                            modifier = Modifier.wrapContentSize()
                                .align(Alignment.CenterHorizontally)
                                .padding(dimens.mediumPadding),
                            user = user.value,
                            updateTrigger = userViewModel.updateItemTrigger.value,
                            goToUser = {
                                isVisibleUserPanel.value = !isVisibleUserPanel.value
                            },
                            goToAllLots = {
                                if (user.value != null) {
                                    component.selectAllOffers(user.value!!)
                                }
                            },
                            goToAboutMe = {
                                component.onTabSelect(4)
                            },
                            addToSubscriptions = {
                                if (UserData.token != "") {
                                    userViewModel.addNewSubscribe(
                                        LD(),
                                        SD().copy(
                                            userLogin = user.value?.login,
                                            userID = user.value?.id ?: 1L,
                                            userSearch = true
                                        ),
                                        onSuccess = {
                                            component.updateUserInfo()
                                        },
                                        errorCallback = { es ->
                                            errorString.value = es
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
                            isBlackList = blackList.value
                        )

                        CreateSubscribeDialog(
                            errorString.value != "",
                            errorString.value,
                            onDismiss = {
                                errorString.value = ""
                            },
                            goToSubscribe = {
                                component.goToSubscriptions()
                                errorString.value = ""
                            }
                        )
                    }
                }

                TabRow(
                    tabs,
                    selectedTab = selectedTabIndex.value,
                    edgePadding = dimens.smallPadding,
                    containerColor = colors.primaryColor,
                    modifier = Modifier.fillMaxWidth(),
                ){ index, tab ->
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

                ChildPages(
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
                        aboutMe = user.value?.aboutMe,
                        onScrollDirectionChange = { isAtTop ->
                            isVisibleUserPanel.value = isAtTop
                        }
                    )
                }
            }
        }else{
            showNoItemLayout(
                title = stringResource(strings.userDeletedLabel)
            ){
                component.updateUserInfo()
            }
        }
    }
}
