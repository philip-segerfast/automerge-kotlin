package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.Heads
import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.KReadable
import dev.psegerfast.automergekotlin.repo.shared.DocumentId

interface DocumentRepository {
    /** Find existing [DisposableDocumentHandle] or null if it doesn't exist. */
    fun get(documentId: DocumentId): DisposableDocumentHandle?
    /** Create a new [DisposableDocumentHandle] with random ID. */
    fun create(): DisposableDocumentHandle
    /** Create a new [DisposableDocumentHandle] with the specified documentId */
    fun create(documentId: DocumentId): DisposableDocumentHandle
    fun getOrCreate(documentId: DocumentId): DisposableDocumentHandle
    /** Delete the document.proto with the associated documentId. */
    suspend fun delete(documentId: DocumentId): Boolean
    suspend fun init()

    interface DocumentRepositoryState
}

interface DocumentSnapshot: KReadable {
    val actorId: DocumentId
    fun encodeChangesSince(heads: Heads): ByteArray
}

/** Heads should maybe be used in equals/hashcode. */
class DocumentSnapshotImpl(private val document: KDocument): DocumentSnapshot, KReadable by document {
    // Note - equals/hashcode is always false.

    override fun encodeChangesSince(heads: Heads): ByteArray {
        return document.encodeChangesSince(heads)
    }

    override val actorId: DocumentId get() = document.actorId
}

class DocumentDeletedException(msg: String) : RuntimeException(msg)
