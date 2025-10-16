package dev.psegerfast.automergekotlin.repo.shared.init

import android.content.Context
import dev.psegerfast.automergekotlin.repo.shared.framework.koin.platformModule
import dev.psegerfast.automergekotlin.repo.shared.framework.koin.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun startAutomergeRepo(context: Context) {
    startKoin {
        androidContext(context)
        modules(sharedModule, platformModule)
    }
}