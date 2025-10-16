package dev.psegerfast.automergekotlin.repo.shared.adapter.repository

import dev.psegerfast.automergekotlin.repo.shared.DocumentId

interface DocumentRepository2 {
    /** Retrieves a document.proto by id. */
    fun find(documentId: DocumentId): DocHandle2?
    fun create(documentId: DocumentId): DocHandle2
    fun create(): DocHandle2
}