package dev.psegerfast.automergekotlin.repo.shared.framework.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Upsert
    suspend fun upsert(document: DocumentEntity)

    @Query("SELECT * FROM documents")
    suspend fun getAll(): List<DocumentEntity>

    @Query("SELECT * FROM documents")
    fun getAllFlow(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE documentId = :documentId")
    suspend fun getById(documentId: DocumentId): DocumentEntity?

    @Query("SELECT * FROM documents WHERE documentId = :documentId")
    fun getByIdFlow(documentId: DocumentId): Flow<DocumentEntity?>

    suspend fun delete(documentId: DocumentId)

}