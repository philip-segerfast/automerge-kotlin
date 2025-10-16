package dev.psegerfast.automergekotlin.repo.shared.storage

import dev.psegerfast.automergekotlin.repo.shared.DocumentId

interface DocumentStorage {

    suspend fun read(documentId: DocumentId): ByteArray?

    suspend fun write(documentId: DocumentId, bytes: ByteArray)

    suspend fun delete(documentId: DocumentId)

}