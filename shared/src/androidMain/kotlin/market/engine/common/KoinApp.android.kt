package market.engine.common

import android.content.Context
import market.engine.di.InstanceModule.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


fun launchKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(appModule)
    }
}

actual fun initKoin() {

}
