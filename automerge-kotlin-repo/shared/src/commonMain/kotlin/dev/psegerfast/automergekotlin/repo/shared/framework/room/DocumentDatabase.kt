package dev.psegerfast.automergekotlin.repo.shared.framework.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DocumentEntity::class],
    version = 1,
)
abstract class DocumentDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
}
