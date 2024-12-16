package market.engine.presentation.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.flow.collectLatest
import market.engine.core.globalData.ThemeResources.colors
import market.engine.core.types.ReportPageType
import market.engine.presentation.base.BaseContent
import market.engine.presentation.user.feedbacks.FeedbackTabs
import market.engine.presentation.user.feedbacks.FeedbacksContent
import market.engine.widgets.exceptions.onError
import market.engine.widgets.rows.UserPanel
import market.engine.widgets.rows.UserSimpleRow

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
        { onError(isError.value) { component.updateUserInfo() } }
    }else{
        null
    }

    val isVisibleUserPanel = remember { mutableStateOf(userViewModel.isVisibleUserPanel.value) }

    val selectedTabIndex = remember {
        mutableStateOf(0)
    }

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
                    user.value?.let { UserSimpleRow(it) }
                },
                isVisibleUserPanel = isVisibleUserPanel.value,
                onUserSliderClick = {
                    isVisibleUserPanel.value = !isVisibleUserPanel.value
                },
                onBackClick = {
                    component.onBack()
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
        Column {
            AnimatedVisibility(
                isVisibleUserPanel.value,
                enter = expandIn(),
                exit = fadeOut(),
            ) {
                UserPanel(
                    modifier = Modifier.background(colors.white)
                        .fillMaxWidth()
                        .clickable {
                            isVisibleUserPanel.value = !isVisibleUserPanel.value
                        },
                    user = user.value,
                    goToUser = null,
                    goToAllLots = {
                        if (user.value != null) {
                            component.selectAllOffers(user.value!!)
                        }
                    },
                    goToAboutMe = {
                        component.onTabSelect(4)
                    },
                    addToSubscriptions = {

                    },
                    goToSubscriptions = {

                    },
                    isBlackList = blackList.value
                )
            }

            FeedbackTabs(
                selectedTab = selectedTabIndex.value,
                onTabSelected = { index ->
                    component.onTabSelect(index)
                }
            )

            ChildPages(
                pages = feedbacksPages,
                scrollAnimation = PagesScrollAnimation.Default,
                onPageSelected = {
                    val select = when(it){
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
                    aboutMe = user.value?.aboutMe
                )
            }
        }
    }
}
