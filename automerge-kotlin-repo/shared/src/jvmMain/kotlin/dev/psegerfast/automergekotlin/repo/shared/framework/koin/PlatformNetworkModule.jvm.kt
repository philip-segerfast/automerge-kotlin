package dev.psegerfast.automergekotlin.repo.shared.framework.koin

import dev.psegerfast.automergekotlin.repo.shared.network.RSocketManager
import org.koin.dsl.module

actual val platformNetworkModule = module {
    single<RSocketManager> { RSocketManager(get()) }
}