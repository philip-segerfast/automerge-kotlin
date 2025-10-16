package dev.psegerfast.automergekotlin.repo.shared.framework.room

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun getDocumentDatabase(): DocumentDatabase {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "documents.db")
    return Room
        .databaseBuilder<DocumentDatabase>(
            name = dbFile.absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .build()
}