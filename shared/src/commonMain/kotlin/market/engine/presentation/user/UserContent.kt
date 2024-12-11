package market.engine.presentation.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import market.engine.core.globalData.ThemeResources.dimens
import market.engine.core.types.ReportPageType
import market.engine.presentation.base.BaseContent
import market.engine.presentation.user.feedbacks.FeedbacksContent
import market.engine.widgets.buttons.NavigationArrowButton
import market.engine.widgets.exceptions.onError
import market.engine.widgets.rows.UserPanel

@Composable
fun UserContent(
    component: UserComponent,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()
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

    BaseContent(
       modifier = modifier.fillMaxSize(),
       isLoading = isLoading.value,
       error = error,
       toastItem = userViewModel.toastItem,
       onRefresh = {
           component.updateUserInfo()
       },
   ) {
        Column {
            Box {
                NavigationArrowButton {
                    component.onBack()
                }
                Spacer(modifier = Modifier.width(dimens.mediumSpacer))
                UserPanel(
                    modifier = Modifier.fillMaxWidth(),
                    user = user.value,
                    goToUser = null,
                    goToAllLots = {
                        if (user.value != null) {
                            component.selectAllOffers(user.value!!)
                        }
                    },
                    goToAboutMe = {

                    },
                    addToSubscriptions = {

                    },
                    goToSubscriptions = {

                    },
                    isBlackList = blackList.value
                )
            }

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
                    component.selectFeedbackPage(select)
                }
            ) { i, page ->
                FeedbacksContent(
                    index = i,
                    component = page
                )
            }
        }
   }
}
