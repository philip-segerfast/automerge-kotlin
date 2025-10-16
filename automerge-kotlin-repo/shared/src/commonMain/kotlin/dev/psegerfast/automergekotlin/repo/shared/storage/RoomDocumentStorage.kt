package dev.psegerfast.automergekotlin.repo.shared.storage

import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.framework.room.DocumentDao
import dev.psegerfast.automergekotlin.repo.shared.framework.room.DocumentEntity

class RoomDocumentStorage(
    private val documentDao: DocumentDao,
) : DocumentStorage {

    override suspend fun read(documentId: DocumentId): ByteArray? {
        return documentDao.getById(documentId)?.document
    }

    override suspend fun write(
        documentId: DocumentId,
        bytes: ByteArray
    ) {
        documentDao.upsert(DocumentEntity(documentId, bytes))
    }

    override suspend fun delete(documentId: DocumentId) {
        documentDao.delete(documentId)
    }

}
