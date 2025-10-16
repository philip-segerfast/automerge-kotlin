package dev.psegerfast.automergekotlin.repo.shared.docsync

import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.message.Message
import dev.psegerfast.automergekotlin.repo.shared.message.SyncMessage
import kotlinx.coroutines.flow.Flow

/** Used by the client to sync a document.proto. */
interface DocumentSyncer {

    suspend fun startSync(documentId: DocumentId): Flow<Message>

    /**
     * Sends a [dev.psegerfast.automergekotlin.repo.shared.message.SyncMessage] to the network.
     * @param documentId the document.proto id.
     * @param message the message to send.
     */
    suspend fun sendSyncMessage(documentId: DocumentId, message: SyncMessage)

}