package dev.psegerfast.repo

import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.KRead
import dev.psegerfast.automergekotlin.core.KSyncState
import dev.psegerfast.automergekotlin.core.invoke
import io.rsocket.kotlin.RSocket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

interface NetworkAdapter {

    val connectionState: StateFlow<ConnectionState>

    suspend fun connect()

    suspend fun disconnect()

    suspend fun sendSyncMessage(documentId: String, message: ByteArray): Result<ByteArray?>

    suspend fun pushDocument(document: KDocument): DocHandle

    /**
     * Fetch a document from the network with the given ID. Null if there's no such document.
     * */
    suspend fun fetchDocument(documentId: String): Result<KDocument?>

    sealed interface ConnectionState {
        data object Connecting : ConnectionState
        /** Connection is established. */
        data object Connected : ConnectionState
        /** Connection not yet established. */
        data object Idle : ConnectionState
        /** Connection is lost. */
        sealed interface Disconnected : ConnectionState {
            /** Something went wrong while trying to connect. */
            data class FailedToConnect(val error: Exception) : Disconnected
            data class LostConnection(val error: Exception) : Disconnected
            data object ExplicitDisconnection : Disconnected
        }
    }
}

data class SyncMessage(
    val documentId: String,
    val message: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SyncMessage

        if (documentId != other.documentId) return false
        if (!message.contentEquals(other.message)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = documentId.hashCode()
        result = 31 * result + (message?.contentHashCode() ?: 0)
        return result
    }
}

interface StorageAdapter {
    /** Load the document from storage with the given ID. */
    suspend fun load(documentId: String): DocHandle?
    /** Save the document to storage with the given ID. */
    suspend fun save(documentId: String, document: KDocument)
}

class RSocketAdapter(private val rsocket: RSocket) : NetworkAdapter {

//    private val _connectionState = MutableStateFlow(NetworkAdapter.ConnectionState.)
    override val connectionState: StateFlow<NetworkAdapter.ConnectionState> = TODO()

    override suspend fun connect() {
        TODO("Not yet implemented")
    }

    override suspend fun disconnect() {
        TODO("Not yet implemented")
    }

    override suspend fun sendSyncMessage(
        documentId: String,
        message: ByteArray
    ): Result<ByteArray?> {
        TODO("Not yet implemented")
    }

    override suspend fun pushDocument(document: KDocument): DocHandle {
        TODO("Not yet implemented")
    }

    override suspend fun fetchDocument(documentId: String): Result<KDocument?> {
        TODO("Not yet implemented")
    }

}

//class RSocketNetworkAdapter(rsocket: RSocket) : NetworkAdapter {
//    override val connectionState: StateFlow<NetworkAdapter.ConnectionState>
//        get() = TODO("Not yet implemented")
//
//    override suspend fun connect() {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun disconnect() {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun sendSyncMessage(
//        documentId: String,
//        message: ByteArray
//    ): Result<ByteArray?> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun pushDocument() {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun fetchDocument(documentId: String): Result<KDocument?> {
//        TODO("Not yet implemented")
//    }
//}

class DataStoreAdapter() : StorageAdapter {
    override suspend fun load(documentId: String): DocHandle? {
        TODO("Not yet implemented")
    }

    override suspend fun save(
        documentId: String,
        document: KDocument
    ) {
        TODO("Not yet implemented")
    }
}

fun test(rsocket: RSocket) {

//    val repo = Repo.create(
//        networkAdapter = RSocketNetworkAdapter(rsocket),
//        storageAdapter = DataStoreAdapter(),
//    )

    // Will initiate a connection using the networkAdapter and storageAdapter.
//    val handle = repo.create()
//    handle.applySync()

}

//data class DocHandleGroup(
//
//)

class Repo private constructor(
    private val networkAdapter: NetworkAdapter,
    private val storageAdapter: StorageAdapter,
) {
    private val handles = mutableMapOf<String, DocHandle>()

    private val _events = MutableSharedFlow<RepoEvent>()
    val events = _events.asSharedFlow()

    companion object {
        fun create(
            networkAdapter: NetworkAdapter,
            storageAdapter: StorageAdapter
        ) = Repo(networkAdapter, storageAdapter)
    }

    fun create(): CachedDocHandle {
        val document = KDocument()
        val cachedDocHandle = CachedDocHandle(document)
        val persistentDocHandle = PersistentDocHandle(storageAdapter)
        val remoteDocHandle = NetworkDocHandle(networkAdapter)

        return cachedDocHandle
    }

    fun findAsync(): DocHandle {

        TODO()
    }

    suspend fun find(): DocHandle {
        TODO()
    }

}

data class CombinedDocHandle(
    val cachedDocHandle: CachedDocHandle,
    val persistentDocHandle: PersistentDocHandle,
    val remoteDocHandle: NetworkDocHandle,
)

class PersistentDocHandle(private val storageAdapter: StorageAdapter) : DocHandle {
    override val state: StateFlow<HandleState>
        get() = TODO("Not yet implemented")
    override val heads: StateFlow<Array<ByteArray>?>
        get() = TODO("Not yet implemented")
    override val syncState: SharedFlow<KSyncState?>
        get() = TODO("Not yet implemented")

    override suspend fun applySync(
        syncState: KSyncState,
        message: ByteArray
    ): ByteArray? {
        TODO("Not yet implemented")
    }

}

class NetworkDocHandle(private val networkAdapter: NetworkAdapter) : DocHandle {
    override val state: StateFlow<HandleState>
        get() = TODO("Not yet implemented")
    override val heads: StateFlow<Array<ByteArray>?>
        get() = TODO("Not yet implemented")
    override val syncState: SharedFlow<KSyncState?>
        get() = TODO("Not yet implemented")

    override suspend fun applySync(
        syncState: KSyncState,
        message: ByteArray
    ): ByteArray? {
        TODO("Not yet implemented")
    }

}

class CachedDocHandle(private val document: KDocument) : DocHandle {

    val documentValue: KRead = document as KRead

    private val _state: MutableStateFlow<HandleState> = MutableStateFlow(HandleState.Created)
    override val state: StateFlow<HandleState> = _state.asStateFlow()

    private fun heads() = document.heads.map { it.bytes }.toTypedArray()

    private val _heads = MutableStateFlow(heads())
    override val heads: StateFlow<Array<ByteArray>?> = _heads.asStateFlow()

    private val _syncState = MutableSharedFlow<KSyncState>()
    override val syncState: SharedFlow<KSyncState> = _syncState.asSharedFlow()

    override suspend fun applySync(syncState: KSyncState, message: ByteArray): ByteArray? {
        document.receiveSyncMessage(syncState, message)
        return document.generateSyncMessage(syncState)
    }
}

sealed interface RepoEvent {
    sealed interface DocumentEvent : RepoEvent
}

/** DocHandle which is to a non-physical document. */
class LinkedDocHandle

interface DocHandle {
    /** The current state of the handle. */
    val state: StateFlow<HandleState>

    val heads: StateFlow<Array<ByteArray>?>

    /** The current sync state. This will be collected by the Repo and be applied on all other DocHandles. */
    val syncState: SharedFlow<KSyncState?>

    /** Apply an incoming update to the document. Return the current sync state of the document or null if it's all up-to-date. */
    suspend fun applySync(syncState: KSyncState, message: ByteArray): ByteArray?
}

sealed interface SyncState {

}

sealed interface HandleState {
    /** Initial state. */
    data object Created : HandleState
    /** Waiting for network. */
    data object AwaitingNetwork : HandleState
    /** Loading from storage. */
    data object Loading : HandleState
    /** Waiting for network response. */
    data object Requesting : HandleState
    /** Document is ready. */
    sealed interface Ready : HandleState {
        data class RequestingDocument(val timestamp: Long) : Ready
        data class CreatingDocument(val timestamp: Long) : Ready
        data object Idle : Ready
    }
    /** Document deleted. */
    data object Deleted : HandleState
    /** Document unavailable. */
    data object Unavailable : HandleState
}
