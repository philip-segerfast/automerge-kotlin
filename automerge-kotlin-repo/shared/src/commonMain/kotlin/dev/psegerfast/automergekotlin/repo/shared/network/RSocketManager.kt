package dev.psegerfast.automergekotlin.repo.shared.network

import io.ktor.client.HttpClient
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.ktor.client.rSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class RSocketConnectionState {
    data object Disconnected : RSocketConnectionState()
    data object Connecting : RSocketConnectionState()
    data class Connected(val rsocket: RSocket) : RSocketConnectionState()
    data class Error(val message: String, val cause: Throwable? = null) : RSocketConnectionState()
}

interface RSocketManager {
    val connectionState: StateFlow<RSocketConnectionState>
    val currentRSocket: RSocket?
    suspend fun getOrAwaitClient(timeoutMillis: Duration = 30.seconds): RSocket
    fun connect()
    fun disconnect()
}

class RSocketManagerImpl(
    private val httpClient: HttpClient,
) : RSocketManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _connectionState = MutableStateFlow<RSocketConnectionState>(RSocketConnectionState.Disconnected)
    override val connectionState: StateFlow<RSocketConnectionState> = _connectionState.asStateFlow()

    private val mutex = Mutex()
    private var connectJob: Job? = null

    override val currentRSocket: RSocket?
        get() = (connectionState.value as? RSocketConnectionState.Connected)?.rsocket

    init {
        // Optionally, attempt to connect on initialization or on first demand
        // connect() // Or make connect() public and call it when needed
    }

    override fun connect() {
        // Ensure only one connection attempt is active
        if (_connectionState.value is RSocketConnectionState.Connecting || _connectionState.value is RSocketConnectionState.Connected) {
            return
        }

        connectJob?.cancel() // Cancel any previous job
        connectJob = scope.launch {
            mutex.withLock {
                if (_connectionState.value is RSocketConnectionState.Connecting || _connectionState.value is RSocketConnectionState.Connected) {
                    return@launch
                }
                _connectionState.value = RSocketConnectionState.Connecting
                try {
                    println("RSocketManager: Attempting to establish RSocket connection...")
                    // Replace with your actual RSocket setup logic
                    val rsocketClient = httpClient.rSocket(
                        host = "your.rsocket.server.com",
                        port = 7000
                    ) {
                        // Configuration
                    }

                    // Optional: Ping or verify connection
                    // rsocketClient.requestResponse(DefaultPayload.text("ping"))

                    _connectionState.value = RSocketConnectionState.Connected(rsocketClient)
                    println("RSocketManager: Connection established.")

                    // Monitor connection health if RSocket client provides a way
                    // For example, listen to its close/error events to update state
                    // rsocketClient.coroutineContext[Job]?.invokeOnCompletion { throwable ->
                    //     applicationScope.launch {
                    //         mutex.withLock {
                    //             if (_connectionState.value is RSocketConnectionState.Connected &&
                    //                 (_connectionState.value as RSocketConnectionState.Connected).rsocket == rsocketClient) {
                    //                 if (throwable != null) {
                    //                     _connectionState.value = RSocketConnectionState.Error("Connection lost", throwable)
                    //                 } else {
                    //                     _connectionState.value = RSocketConnectionState.Disconnected
                    //                 }
                    //             }
                    //         }
                    //     }
                    // }

                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e // Propagate cancellation
                    println("RSocketManager: Connection failed: ${e.message}")
                    _connectionState.value = RSocketConnectionState.Error("Connection failed", e)
                }
            }
        }
    }

    // Suspending function to get the client, waiting if necessary (up to a timeout)
    override suspend fun getOrAwaitClient(timeoutMillis: Duration): RSocket {
        // If already connected, return immediately
        currentRSocket?.let { return it }

        // If not connecting or disconnected, initiate connection
        if (_connectionState.value is RSocketConnectionState.Disconnected || _connectionState.value is RSocketConnectionState.Error) {
            connect()
        }

        // Wait for the connection state to become Connected
        return withContext(Dispatchers.IO) { // Or another appropriate context
            try {
                _connectionState.filterIsInstance<RSocketConnectionState.Connected>()
                    .map { it.rsocket }
                    .firstOrNull() // Or use .take(1).single() if you want it to throw on timeout from collect
                    ?: throw IllegalStateException("Failed to get RSocket client after timeout or permanent error.")
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                throw IllegalStateException("Timeout waiting for RSocket connection", e)
            }
        }
    }

    override fun disconnect() {
        scope.launch {
            mutex.withLock {
                connectJob?.cancel()
                connectJob = null
                (connectionState.value as? RSocketConnectionState.Connected)?.rsocket?.cancel() // If RSocket has a cancel/close
                _connectionState.value = RSocketConnectionState.Disconnected
                println("RSocketManager: Disconnected.")
            }
            scope.cancel()
        }
    }

}