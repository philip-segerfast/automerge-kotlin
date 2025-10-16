package dev.psegerfast.automergekotlin.repo.shared.framework.koin

import dev.psegerfast.automergekotlin.repo.shared.docsync.DocumentSyncer
import dev.psegerfast.automergekotlin.repo.shared.network.RSocketDocumentSyncer
import dev.psegerfast.automergekotlin.repo.shared.storage.DocumentStorage
import dev.psegerfast.automergekotlin.repo.shared.storage.RoomDocumentStorage
import io.rsocket.kotlin.RSocket
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val sharedModule = module {
    factory<DocumentStorage> { RoomDocumentStorage(get()) }
    single<RSocket> {  }
    factory<DocumentSyncer> { RSocketDocumentSyncer(get(), get()) }
}
