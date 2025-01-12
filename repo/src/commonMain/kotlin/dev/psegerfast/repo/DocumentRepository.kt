package dev.psegerfast.repo

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.KRead
import dev.psegerfast.automergekotlin.core.KSyncState
import dev.psegerfast.automergekotlin.core.invoke
import dev.psegerfast.repo.DefaultRSocketConnection.DocumentStreamSession
import io.rsocket.kotlin.RSocket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf

typealias DocumentId = ByteArray

interface DocumentRepository {
    fun find(documentId: String): DocumentHandle

    interface DocumentRepositoryState
}

interface DocumentHandle {
    val handleState: StateFlow<DocumentHandleState>
    val currentSnapshot: StateFlow<DocumentSnapshot?>
}

class DocumentHandleState

class DocumentSnapshot(private val document: KDocument): KRead by document

interface RSocketDocumentRepositoryConnection {
    suspend fun connect()
    suspend fun disconnect()

    @Serializable
    class PushDocumentResponsePayload(
        val success: Boolean,
        val error: String?,
    )

    /**
     * Push a document which may not exist on the remote.
     * */
    suspend fun pushDocument(documentBytes: ByteArray)

    /** Get the full document in bytes from the remote document. */
    suspend fun retrieveDocumentBytes(documentId: DocumentId): ByteArray?
//    /**
//     * Send a sync request to the remote document. This will apply these changes to the target.
//     * Response is a SyncState (encoded) which can be used to apply any other changes from the remote document.
//     * */
//    suspend fun sync(documentId: DocumentId, syncMessage: ByteArray): SyncResponse
    suspend fun getChangesSince(documentId: DocumentId, heads: Array<KChangeHash>): ByteArray
    suspend fun streamDocumentChanges(
        documentId: DocumentId,
        initialHeads: Array<KChangeHash>,
        onNewChanges: suspend (ByteArray) -> Unit
    ): DocumentStreamSession

    class RetrieveDocumentResponse(
        val documentBytes: ByteArray,
    )

    class SyncResponse(
        /** Null if there's nothing to sync. */
        val syncState: ByteArray?,
    )

    class FetchChangesSinceHeadsResponse(
        val changes: ByteArray,
    )
}

object Routes {
    const val PUSH_DOCUMENT = "document.push"
    const val FETCH_DOCUMENT = "document.fetch"
//    const val SYNC_DOCUMENT = "document.sync"
    const val STREAM_DOCUMENT_START = "document.sync.start"
    const val STREAM_DOCUMENT_SET_HEADS = "document.sync.heads"
}

@OptIn(ExperimentalSerializationApi::class)
class DefaultRSocketConnection(
    connect: () -> RSocket,
    private val proto: ProtoBuf,
) : RSocketDocumentRepositoryConnection {

    private var rsocketOrNull: RSocket? = null
    private val onConnect = connect

    override suspend fun connect() {
        if(rsocketOrNull != null) {
            throw IllegalStateException("Already connected")
        }

        rsocketOrNull = onConnect()
    }

    private fun requireRSocket() = rsocketOrNull ?: throw IllegalStateException("Not connected")

    override suspend fun disconnect() {
        rsocketOrNull?.cancel("Manually disconnected.") ?: throw IllegalStateException("Already disconnected")
    }

    override suspend fun pushDocument(documentBytes: ByteArray) {
        val requestPayload = proto.encodeToPayload(Routes.FETCH_DOCUMENT, documentBytes)
        requireRSocket().requestResponse(TODO())
    }

    override suspend fun retrieveDocumentBytes(documentId: DocumentId): ByteArray? {
        @Serializable
        class RequestDocumentBytes(
            val documentId: DocumentId,
        )
        @Serializable
        class DocumentBytesResponse(
            val documentBytes: ByteArray?,
        )

        val requestPayload = proto.encodeToPayload(Routes.FETCH_DOCUMENT, RequestDocumentBytes(documentId))
        val responsePayload = requireRSocket().requestResponse(requestPayload)
        val serializedResponse: DocumentBytesResponse = proto.decodeFromPayload(responsePayload)
        return serializedResponse.documentBytes
    }

//    override suspend fun sync(
//        documentId: DocumentId,
//        syncMessage: ByteArray
//    ): RSocketDocumentRepositoryConnection.SyncResponse {
//        @Serializable
//        class RequestSyncDocument(
//            val documentId: DocumentId,
//            val syncMessage: ByteArray,
//        )
//        @Serializable
//        class SyncResponse(
//            val syncState: ByteArray?,
//        )
//
//        val requestPayload = proto.encodeToPayload(Routes.SYNC_DOCUMENT, RequestSyncDocument(documentId, syncMessage))
//        val responsePayload = requireRSocket().requestResponse(requestPayload)
//        val serializedResponse: SyncResponse = proto.decodeFromPayload(responsePayload)
//
//        TODO("Not yet implemented")
//    }

    override suspend fun getChangesSince(
        documentId: DocumentId,
        heads: Array<KChangeHash>
    ): ByteArray {
        TODO("Not yet implemented")
    }

    interface DocumentStreamSession {
        fun setCurrentHeads(heads: Array<KChangeHash>)
    }

    override suspend fun streamDocumentChanges(
        documentId: DocumentId,
        initialHeads: Array<KChangeHash>,
        onNewChanges: suspend (ByteArray) -> Unit
    ): DocumentStreamSession {
        @Serializable
        class CurrentHeadsPayload(
            val heads: Array<KChangeHash>,
        )

        @Serializable
        class ChangesResponse(
            val changes: ByteArray,
        )

        // Heads which are applied to the local document.
        val currentLocalHeads = MutableStateFlow(initialHeads)

        suspendCancellableCoroutine<Unit> { continuation ->
            val streamJob = requireRSocket().launch {

                val streamStarted = CompletableDeferred<Unit>(parent = this@launch.coroutineContext.job)

                launch {
                    requireRSocket()
                        .requestStream(proto.encodeToPayload(Routes.STREAM_DOCUMENT_SET_HEADS, CurrentHeadsPayload(initialHeads)))
                        // If there are multiple new changes to the upstream document then only the
                        // last will be applied on next invocation of collect.
                        .conflate()
                        .collect { payload ->
                            val changes: ByteArray = proto.decodeFromPayload<ChangesResponse>(payload).changes
                            onNewChanges(changes)
                        }
                }

                // Wait until stream is actually started before notifying server about other updates to heads.
                streamStarted.await()

                launch {
                    currentLocalHeads
                        .collect { heads ->
                            // If current local heads are changed, notify the rsocket connection.
                            setStreamSessionHeads(documentId, heads)
                        }
                }

            }

            continuation.invokeOnCancellation {
                streamJob.cancel()
            }
        }

        return object : DocumentStreamSession {
            override fun setCurrentHeads(heads: Array<KChangeHash>) {
                currentLocalHeads.value = heads
            }
        }
    }

    class StreamSessionSetHeadsPayload(
        val documentId: DocumentId,
        val heads: Array<KChangeHash>,
    )

    private suspend fun setStreamSessionHeads(documentId: DocumentId, heads: Array<KChangeHash>) {
        requireRSocket()
            .requestResponse(
                payload = proto.encodeToPayload(
                    Routes.STREAM_DOCUMENT_SET_HEADS,
                    StreamSessionSetHeadsPayload(documentId, heads)
                )
            )
    }

}

fun test() {
    val doc = KDocument()
    doc.actorId
    val heads = doc.heads

    val syncState = KSyncState.Companion()
    val bytes = doc.generateSyncMessage(syncState)
//    doc.receiveSyncMessage(syncState, bytes)

    val changes: ByteArray = doc.encodeChangesSince(heads)




}

class RSocketDocumentRepository(
    private val rsocketConnection: RSocketDocumentRepositoryConnection,
) : DocumentRepository {
    override fun find(documentId: String): DocumentHandle {
        TODO("Not yet implemented")
    }
}

class DataStoreDocumentRepository : DocumentRepository {
    override fun find(documentId: String): DocumentHandle {
        TODO("Not yet implemented")
    }
}

class MainDocumentRepository private constructor(
    private val localRepository: DocumentRepository,
    private val remoteRepository: DocumentRepository,
) : DocumentRepository {
    override fun find(documentId: String): DocumentHandle {
        return MainDocumentHandle()
    }

    companion object {
        fun create(): MainDocumentRepository {
            return MainDocumentRepository(
                object : DocumentRepository {
                    override fun find(documentId: String): DocumentHandle {
                        TODO("Not yet implemented")
                    }
                },
                object : DocumentRepository {
                    override fun find(documentId: String): DocumentHandle {
                        TODO("Not yet implemented")
                    }
                }
            )
        }
    }

    sealed interface MainDocumentRepositoryState : DocumentRepository.DocumentRepositoryState {

    }

    class MainDocumentHandle : DocumentHandle {
        override val handleState: StateFlow<DocumentHandleState> = MutableStateFlow(DocumentHandleState())
        override val currentSnapshot: StateFlow<DocumentSnapshot?> = MutableStateFlow(DocumentSnapshot(KDocument.Companion()))
    }
}