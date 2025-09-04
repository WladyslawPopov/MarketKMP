package market.engine.fragments.root

import androidx.lifecycle.SavedStateHandle

import com.arkivanov.decompose.router.stack.active
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import market.engine.common.showReviewManager
import market.engine.core.data.globalData.SAPI
import market.engine.core.data.items.DeepLink
import market.engine.core.utils.printLogD
import market.engine.fragments.base.CoreViewModel
import market.engine.fragments.root.DefaultRootComponent.Companion.goToMain

class RootVewModel(val component: RootComponent, savedStateHandle: SavedStateHandle) : CoreViewModel(savedStateHandle) {
    init {
        val isFirstLaunch = settings.getSettingValue("isFirstLaunch", true)
        if (isFirstLaunch == true) {
            settings.setSettingValue("isFirstLaunch", false)
            analyticsHelper.reportEvent("launch_first_time", mapOf())
        }

        analyticsHelper.reportEvent("start_session", mapOf("traffic_source" to "direct"))

        val appAttributes = mapOf("app_version" to SAPI.version)
        analyticsHelper.updateUserProfile(appAttributes)

        val countLaunch = settings.getSettingValue("count_start", 0) ?: 0
        settings.setSettingValue("count_start", countLaunch+1)
        printLogD("countLaunch", countLaunch.toString())

        if(countLaunch == 10) {
            showReviewManager()
        }
    }

    fun goToDeepLink(url : DeepLink){
        scope.launch {
            try {
                delay(300)
                withContext(Dispatchers.Main) {
                    var component = (this@RootVewModel.component.childStack.active.instance as?
                                RootComponent.Child.MainChild)?.component
                    if (component != null) {
                        component.model.value.viewModel.handleDeepLink(url)
                    } else {
                        goToMain()
                        component =
                            (this@RootVewModel.component.childStack.active.instance as? RootComponent.Child.MainChild)?.component
                        component?.model?.value?.viewModel?.handleDeepLink(url)
                    }
                }
            }catch (e : Exception){
                println("Ignoring deep link update during navigation: ${e.message}")
            }
        }
    }
}
