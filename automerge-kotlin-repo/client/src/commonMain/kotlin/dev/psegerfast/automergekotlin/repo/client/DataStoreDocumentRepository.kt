package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.repo.shared.DocumentId

class DataStoreDocumentRepository : DocumentRepository {
    override fun get(documentId: DocumentId): DisposableDocumentHandle {
        TODO("Not yet implemented")
    }

    override fun create(): DisposableDocumentHandle {
        TODO("Not yet implemented")
    }

    override fun create(documentId: DocumentId): DisposableDocumentHandle {
        TODO("Not yet implemented")
    }

    override fun getOrCreate(documentId: DocumentId): DisposableDocumentHandle {
        TODO("Not yet implemented")
    }

    override suspend fun delete(documentId: DocumentId): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun init() {
        TODO("Not yet implemented")
    }
}