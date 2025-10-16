package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.repo.shared.ApiPayloads
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.Endpoints
import dev.psegerfast.automergekotlin.repo.shared.decodeFromPayload
import dev.psegerfast.automergekotlin.repo.shared.encodeToPayload
import io.rsocket.kotlin.RSocket
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
class DefaultRSocketConnection(
    connect: () -> RSocket,
    private val proto: ProtoBuf,
) : RepositoryConnection {

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
        val requestPayload = proto.encodeToPayload(Endpoints.FETCH_DOCUMENT, documentBytes)
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

        val requestPayload = proto.encodeToPayload(Endpoints.FETCH_DOCUMENT, RequestDocumentBytes(documentId))
        val responsePayload = requireRSocket().requestResponse(requestPayload)
        val serializedResponse: DocumentBytesResponse = proto.decodeFromPayload(responsePayload)
        return serializedResponse.documentBytes
    }

    override suspend fun getChangesSince(
        documentId: DocumentId,
        heads: Array<KChangeHash>
    ): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun streamDocumentChanges(
        documentId: DocumentId,
        initialHeads: Array<KChangeHash>,
        onNewChanges: suspend (ByteArray) -> Unit
    ): RepositoryConnection.DocumentStreamSession {

        // Heads which are applied to the local document.proto.
        val currentLocalHeads = MutableStateFlow(initialHeads)

        suspendCancellableCoroutine<Unit> { continuation ->
            val streamJob = requireRSocket().launch {

                val streamStarted = CompletableDeferred<Unit>(parent = this@launch.coroutineContext.job)

                launch {
                    requireRSocket()
                        .requestStream(
                            proto.encodeToPayload(
                                Endpoints.STREAM_DOCUMENT_START,
                                ApiPayloads.StreamDocumentChanges.SetCurrentHeadsPayload(documentId, initialHeads)
                            )
                        )
                        // If there are multiple new changes to the upstream document.proto then only the
                        // last will be applied on next invocation of collect.
                        .conflate()
                        .collect { payload ->
                            val changes: ByteArray =
                                proto.decodeFromPayload<ApiPayloads.StreamDocumentChanges.ReceiveChangesResponse>(
                                    payload
                                ).changes
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

        return object : RepositoryConnection.DocumentStreamSession {
            override fun setCurrentHeads(heads: Array<KChangeHash>) {
                currentLocalHeads.value = heads
            }
        }
    }

    private suspend fun setStreamSessionHeads(documentId: DocumentId, heads: Array<KChangeHash>) {
        requireRSocket().requestResponse(
            payload = proto.encodeToPayload(
                Endpoints.STREAM_DOCUMENT_SET_HEADS,
                ApiPayloads.StreamDocumentChanges.SetCurrentHeadsPayload(documentId, heads)
            )
        )
    }

}