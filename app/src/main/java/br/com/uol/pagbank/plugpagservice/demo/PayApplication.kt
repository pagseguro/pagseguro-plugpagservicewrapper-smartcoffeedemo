package br.com.uol.pagbank.plugpagservice.demo

import android.app.Application
import br.com.uol.pagbank.plugpagservice.demo.di.assetModule
import br.com.uol.pagbank.plugpagservice.demo.di.packageManagerModule
import br.com.uol.pagbank.plugpagservice.demo.di.plugpagModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PayApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PayApplication)
            modules(plugpagModule)
            modules(packageManagerModule)
            modules(assetModule)
        }
    }
}
