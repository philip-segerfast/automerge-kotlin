package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.core.KWrite
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import kotlinx.coroutines.flow.StateFlow

class RSocketDocumentRepository(
    private val rsocketConnection: RepositoryConnection,
) : DocumentRepository {
    private var initialized = false
    private var connected = false
    private var connectionError: Exception? = null

    private val physicalDocHandles = mutableMapOf<DocumentId, RSocketDocumentHandle>()
    private val logicalDocHandles = mutableMapOf<DocumentId, MutableList<LogicalDocumentHandle>>()

    override suspend fun init() {
        if(connected) error("Already initialized")

        try {
            rsocketConnection.connect()
            connected = true
            connectionError = null
            initialized = true
        } catch (e: Exception) {
            println("Error when establishing rsocket connection: $e")
            connected = false
            connectionError = e
        }
    }

    override fun get(documentId: DocumentId): DisposableDocumentHandle {
        // Check if handle already exists
        // If yes, wrap it in a LogicalDocumentHandle and return it
        // If no, create a new RSocketDocumentHandle
        val current = physicalDocHandles[documentId]
        if(current != null) return LogicalDocumentHandle(current) {

        }

        TODO()
    }

//    private fun onHandleDisposed()

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
}

private class RSocketDocumentHandle : DisposableDocumentHandle {
    override var deleted: Boolean = false
    override var disposed: Boolean = false

    override val currentSnapshot: StateFlow<DocumentSnapshot?> = TODO()

    override val actorId: DocumentId

    override suspend fun edit(editBlock: KWrite.() -> Unit): KChangeHash? {
        TODO("Not yet implemented")
    }

    override suspend fun sync(update: DocumentSnapshot) {
        TODO("Not yet implemented")
    }

    override suspend fun delete() {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

}