package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KReadable
import dev.psegerfast.automergekotlin.core.KWrite
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.keepalive.KeepAlive
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

interface DocRef {
    val state: StateFlow<DocRefState>

    val id: Any

    /** The latest state of the document.proto. */
    val cachedReadable: StateFlow<KReadable?>

    suspend fun change(block: suspend ChangeBlock.() -> Unit)

    suspend fun sync(syncMessage: ByteArray)

    /** Establish connection to the referenced document.proto. */
    suspend fun connect()

    /** Disconnect from the referenced document.proto. */
    suspend fun disconnect()
}

interface ChangeBlock : KReadable, KWrite

sealed interface DocRefState {
    data object Initialized : DocRefState
    data object Connecting : DocRefState
    data object Connected : DocRefState
    sealed interface Disconnected : DocRefState {
        sealed interface LostConnection : Disconnected
        sealed interface ClientDisconnected : Disconnected
    }
}

suspend fun test2() {
    val client = HttpClient {
        install(WebSockets) //rsocket requires websockets plugin installed
        install(RSocketSupport) {
            //configure rSocket connector (all values have defaults)
            connector {
                maxFragmentSize = 1024

                connectionConfig {
                    keepAlive = KeepAlive(
                        interval = 30.seconds,
                        maxLifetime = 2.minutes
                    )

                    //payload for setup frame
                    setupPayload {
                        buildPayload {
                            data("""{ "data": "setup" }""")
                        }
                    }

                    //mime types
                    payloadMimeType = PayloadMimeType(
                        data = WellKnownMimeType.ApplicationJson,
                        metadata = WellKnownMimeType.MessageRSocketCompositeMetadata
                    )
                }

                //optional acceptor for server requests
                acceptor {
                    RSocketRequestHandler {
                        requestResponse { it } //echo request payload
                    }
                }
            }
        }
    }

    //connect to some url
    val rSocket: RSocket = client.rSocket("wss://demo.rsocket.io/rsocket")

    val stream: Flow<Payload> = rSocket.requestStream(
        buildPayload {
            data("""{ "data": "hello world" }""")
        }
    )

//    val handle: DocRef = repo.create()
//    handle.connect()
//
//    handle.change {
//
//    }

}

class RSocketNetworkAdapter() {

}

//class RSocketDocRef(private val rsocket: RSocket) : DocRef {
//    override val state: StateFlow<DocRefState>
//        get() = TODO("Not yet implemented")
//
//    override val id: Any
//        get() = TODO("Not yet implemented")
//
//    override val cachedReadable: StateFlow<KRead?>
//        get() = TODO("Not yet implemented")
//
//    override suspend fun change(block: suspend KTransaction.() -> Unit) {
//
//
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun sync(syncMessage: ByteArray) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun connect() {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun disconnect() {
//        TODO("Not yet implemented")
//    }
//
//}