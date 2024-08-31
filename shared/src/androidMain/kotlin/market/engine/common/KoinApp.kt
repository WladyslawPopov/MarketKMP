package market.engine.common

import android.app.Application
import android.content.Context
import market.engine.di.InstanceModule.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

lateinit var appContext: Context

fun startKoin(context: Context) {
    appContext = context
    startKoin {
        androidContext(appContext)
        modules(appModule)
    }
}

actual fun initKoin() {

}
