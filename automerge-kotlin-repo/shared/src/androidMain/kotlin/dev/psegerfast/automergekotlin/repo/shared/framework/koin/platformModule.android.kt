package dev.psegerfast.automergekotlin.repo.shared.framework.koin

import dev.psegerfast.automergekotlin.repo.shared.framework.room.DocumentDao
import dev.psegerfast.automergekotlin.repo.shared.framework.room.DocumentDatabase
import dev.psegerfast.automergekotlin.repo.shared.framework.room.getDocumentDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DocumentDatabase> { getDocumentDatabase(get()) }
    single<DocumentDao> { get<DocumentDatabase>().documentDao() }
}
