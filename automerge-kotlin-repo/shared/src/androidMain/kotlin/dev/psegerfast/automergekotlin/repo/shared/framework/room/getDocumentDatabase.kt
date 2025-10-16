package dev.psegerfast.automergekotlin.repo.shared.framework.room

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun getDocumentDatabase(context: Context): DocumentDatabase {
    val dbFile = context.getDatabasePath("documents.db")
    return Room
        .databaseBuilder<DocumentDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath,
        )
        .setDriver(BundledSQLiteDriver())
        .build()

}