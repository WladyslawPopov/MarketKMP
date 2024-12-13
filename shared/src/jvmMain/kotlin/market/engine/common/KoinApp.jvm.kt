package market.engine.common

import market.engine.di.InstanceModule.appModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

actual fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
