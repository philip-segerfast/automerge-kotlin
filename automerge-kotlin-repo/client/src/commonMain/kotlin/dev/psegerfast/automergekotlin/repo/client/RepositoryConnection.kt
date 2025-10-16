package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.repo.shared.DocumentId

/**
 * Data transfer implementation details.
 * */
interface RepositoryConnection {
    /** Establish connection to remote repository */
    suspend fun connect()

    /** Disconnect from remote repository */
    suspend fun disconnect()

    /** Push a document.proto which may not exist on the remote. */
    suspend fun pushDocument(documentBytes: ByteArray)

    /** Get the full document.proto in bytes from the remote document.proto. */
    suspend fun retrieveDocumentBytes(documentId: DocumentId): ByteArray?

    /** Get document.proto changes since the given heads. */
    suspend fun getChangesSince(documentId: DocumentId, heads: Array<KChangeHash>): ByteArray

    /** Stream document.proto changes from [initialHeads]. Use [DocumentStreamSession] to update current heads. */
    suspend fun streamDocumentChanges(
        documentId: DocumentId,
        initialHeads: Array<KChangeHash>,
        onNewChanges: suspend (ByteArray) -> Unit
    ): DocumentStreamSession

    interface DocumentStreamSession {
        fun setCurrentHeads(heads: Array<KChangeHash>)
    }
}