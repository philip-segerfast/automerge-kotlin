package dev.psegerfast.automergekotlin.repo.client.new

import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.KReadable
import dev.psegerfast.automergekotlin.core.KTransaction
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.dochandle.DocHandle2
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


object RSocketFactory {
    suspend fun create(): RSocket {
        //create ktor client
        println("Creating client...")
        val client = HttpClient(CIO) {
            install(WebSockets) // rsocket requires websockets plugin installed
            install(RSocketSupport) {
                // configure rSocket connector (all values have defaults)
                connector {
                    connectionConfig {
                        // payload for setup frame
                        setupPayload {
                            buildPayload {
                                data("""{ "data": "setup" }""")
                            }
                        }

                        // mime types
                        payloadMimeType = PayloadMimeType(
                            data = WellKnownMimeType.ApplicationProtoBuf,
                            metadata = WellKnownMimeType.MessageRSocketCompositeMetadata
                        )
                    }
                }
            }
        }

        println("Connecting...")
        //connect to some url
        var rSocket: RSocket? = null

        var attempt = 0
        val delay = 5.seconds
        while(rSocket == null) {
            try {
                attempt++
                println("[#$attempt] Connecting to rsocket server...")
                rSocket = client.rSocket(
                    host = "0.0.0.0",
                    port = 8080,
                    path = "/rsocket"
                )
            } catch (e: Exception) {
                println("Failed to connect to rsocket server: $e. Retrying in $delay...")
                delay(delay)
            }
        }

        return rSocket
    }
}

/** This is the DocHandle that the client interacts with. Only an in-memory document.proto reference. */
internal class DefaultDocumentHandleImpl(
    private val document: KDocument,
    private val scope: CoroutineScope,
) : DocHandle2 {
    override val documentId: DocumentId = document.actorId

    override var deleted = false
        private set

    private val _data = MutableSharedFlow<KReadable?>(replay = 1)
    override val data: SharedFlow<KReadable?> = _data.asSharedFlow()

    init {
        scope.launch {
            _data.emit(document)
        }
    }

    override suspend fun change(block: (KTransaction) -> Unit) {
        document.startTransaction().use { tx ->
            block(tx)
            tx.commit()
        }
        _data.emit(document)
    }

    override suspend fun delete() {
        deleted = true
        scope.cancel()
        document.free()
        _data.emit(null)
    }

}

fun test(doc: KDocument) {
//    doc.applyEncodedChanges()
}
