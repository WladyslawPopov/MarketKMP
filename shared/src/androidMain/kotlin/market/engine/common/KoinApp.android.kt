package market.engine.common

import android.content.Context
import market.engine.di.InstanceModule.appModule
import org.koin.android.ext.koin.androidContext

lateinit var appContext: Context

fun startKoin(context: Context) {
    appContext = context
    org.koin.core.context.startKoin {
        androidContext(appContext)
        modules(appModule)
    }
}

actual fun initKoin() {

}
