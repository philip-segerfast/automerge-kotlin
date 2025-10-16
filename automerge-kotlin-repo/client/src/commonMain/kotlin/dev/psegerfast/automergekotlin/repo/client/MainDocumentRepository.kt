package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.core.KWrite
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import dev.psegerfast.automergekotlin.repo.shared.classLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ConnectionChannel {
    /** Establish connection to a document.proto */
    fun connect()

    /** Disconnect from a document.proto */
    fun disconnect()

    /** Dispose connection. This connection is no longer usable. */
    fun dispose()
}

/**
 * This is the DocumentRepository that the client and server interact with.
 * Instance is obtained and constructed using [MainDocumentRepository.create]
 * */
@OptIn(ExperimentalUuidApi::class)
class MainDocumentRepository private constructor(
    private val memoryRepository: DocumentRepository,
    private val persistentRepository: DocumentRepository,
    private val remoteRepository: DocumentRepository,
) : DocumentRepository {

    // Only one instance of each DocumentHandle which holds the actual resource.
    private val physicalDocHandles = mutableMapOf<DocumentId, PhysicalMainDocumentHandle>()
    // DocumentHandles which are used by the client. These keep references to a physicalDocHandle.
    private val logicalDocHandles = mutableMapOf<DocumentId, MutableList<LogicalDocumentHandle>>()

    private val docHandles = mutableMapOf<DocumentId, PhysicalMainDocumentHandle>()
    // TODO - Change dispatcher?
    private val scope = CoroutineScope(Dispatchers.Default)

    private val logger = classLogger("MainDocumentRepository")

    override suspend fun init() {
        logger.logMethod("init()")
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun create(): DisposableDocumentHandle {
        logger.logMethod("create")
        val docId = Uuid.random().toByteArray()
        return create(docId)
    }

    private fun DocumentId.uuidString() = Uuid.fromByteArray(this).toString()

    override fun create(documentId: DocumentId): DisposableDocumentHandle {
        return logger.logWithMethod("create(${documentId.uuidString()})") {
            // Check if this document.proto already exists
            if(docHandles.containsKey(documentId)) {
                error("Document already exists")
            }

            log("Document didn't already exist. Creating...")

            val physicalDocHandle = PhysicalMainDocumentHandle(
                actorId = documentId,
                cachedDocHandle = memoryRepository.getOrCreate(documentId),
                persistentDocHandle = persistentRepository.getOrCreate(documentId),
                remoteDocHandle = remoteRepository.getOrCreate(documentId),
            )

            physicalDocHandles[documentId] = physicalDocHandle

            LogicalDocumentHandle(
                physicalDocHandle = physicalDocHandle,
                onDisposed = {
                    onLogicalDocHandleDisposed(documentId)
                }
            )
        }
    }

    private fun onLogicalDocHandleDisposed(documentId: DocumentId) {
        logger.logWithMethod("onLogicalDocHandleDisposed") {
            // Get all logical doc handles for [documentId]
            val logicalDocHandlesForId = logicalDocHandles[documentId]!!
            // Find the doc handle with the specific documentId
            val handleWithIdToRemove = logicalDocHandlesForId.first { it.actorId == documentId }
            // Remove it
            logicalDocHandlesForId.remove(handleWithIdToRemove)
            // Check if there are any other active logicalDocHandles
            if(logicalDocHandlesForId.isEmpty()) {
                // There are no logical DocHandles left. Let's dispose the physical one.
                disposePhysicalDocHandle(documentId)
            }
        }
    }

    private fun disposePhysicalDocHandle(documentId: DocumentId) {
        docHandles.remove(documentId)?.dispose()
    }

    private suspend fun onLogicalDocHandleDeleted(handle: LogicalDocumentHandle) {
        // 1. Get all other docHandles
        val logicalDocHandlesForId: MutableList<LogicalDocumentHandle> = logicalDocHandles[handle.actorId]!!
        // 2. Invoke delete on them.
        logicalDocHandlesForId.forEach { docHandle ->
            if(!docHandle.deleted) {
                // DocHandles will be deleted and disposed.
                docHandle.onDeleted()
            }
        }
        val physicalDocHandle = physicalDocHandles[handle.actorId]!!
        physicalDocHandle.delete()
    }

    override fun getOrCreate(documentId: DocumentId): DisposableDocumentHandle {
        logger.logMethod("getOrCreate(${documentId.uuidString()})")
        return get(documentId) ?: create(documentId)
    }

    override fun get(documentId: DocumentId): DisposableDocumentHandle? {
        logger.logMethod("get(${documentId.uuidString()})")
        return docHandles[documentId]
    }

    override suspend fun delete(documentId: DocumentId): Boolean {
        logger.logMethod("delete(${documentId.uuidString()})")
        TODO("Not yet implemented")
        // 1. Get the documentHandle and invoke delete on it.
    }

    companion object {
        fun create(
            localRepository: DocumentRepository,
            remoteRepository: DocumentRepository,
        ): MainDocumentRepository {
            classLogger("MainDocumentRepository.Companion")
                .logMethod("""create(
                    |   localRepository = $localRepository,
                    |   remoteRepository = $remoteRepository
                    |)
                """.trimMargin())
            return MainDocumentRepository(
                MemoryDocumentRepository(),
                localRepository,
                remoteRepository,
            )
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun create(): MainDocumentRepository {
            classLogger("MainDocumentRepository.Companion").logMethod("create()")
            return create(
                localRepository = MemoryDocumentRepository(),
                remoteRepository = RSocketDocumentRepository(
                    DefaultRSocketConnection(
                        connect = { TODO() },
                        proto = ProtoBuf.Default,
                    )
                ),
            )
        }
    }
}

private class PhysicalMainDocumentHandle(
    override val actorId: DocumentId,
    private val cachedDocHandle: DisposableDocumentHandle,
    private val persistentDocHandle: DisposableDocumentHandle,
    private val remoteDocHandle: DisposableDocumentHandle,
) : DisposableDocumentHandle {
    override var deleted: Boolean = false
    override var disposed: Boolean = false

    private val scope = CoroutineScope(Dispatchers.Default)
    private val logger = classLogger(this::class.simpleName!!)

    private val _currentSnapshot = MutableStateFlow<DocumentSnapshot?>(null)
    override val currentSnapshot: StateFlow<DocumentSnapshot?> = _currentSnapshot.asStateFlow()

    init {
        init()
    }

    private fun init() {
        logger.logWithMethod("init()") {
            log("Setting current snapshot value to cached doc handle value...")
            _currentSnapshot.value = cachedDocHandle.currentSnapshot.value
            log("Starting handle sync process...")
            val job = startHandleSyncProcess().apply {
                invokeOnCompletion {
                    log("Handle sync process job completed.")
                }
            }
        }
    }

    /** Keep these handles in sync.
     * When client writes to a DocHandle it is actually written to the in-memory handle.
     * This write is detected and stored to disk and sent to the server.
     *
     * When the server writes to the remoteDocHandle this is picked up and the cachedDocHandle is synced.
     * This triggers a sync to the persistentDocHandle as well.
     *
     * Nothing is triggered when some data is stored to disk but it can be tracked by checking its state.
     * */
    fun startHandleSyncProcess(): Job = scope.launch {
        logger.withMethod("startHandleSyncProcess()") {
            launch {
                // When the in-memory docHandle is updated, make sure the persistent and remote docHandles are also updated.
                cachedDocHandle.currentSnapshot.collectLatest { snapshot ->
                    if(snapshot == null) {
                        log("LOCAL SNAPSHOT IS NULL")
                        return@collectLatest
                    }

                    coroutineScope {
                        launch {
                            persistentDocHandle.sync(snapshot)
                        }
                        launch {
                            remoteDocHandle.sync(snapshot)
                        }
                    }
                }
            }

            launch {
                // When the remote docHandle is updated (and it's not the client's latest changes), update the in-memory docHandle too.
                remoteDocHandle.currentSnapshot.collectLatest { snapshot ->

                    if(snapshot == null) {
                        println("REMOTE SNAPSHOT IS NULL")
                        return@collectLatest
                    } else {
                        println("Remote snapshot: $snapshot")
                    }

                    cachedDocHandle.sync(snapshot)
                }
            }
        }
    }

    override suspend fun edit(editBlock: KWrite.() -> Unit): KChangeHash? {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    // Update the local docHandle. This will also update the persistent and remote ones.
    override suspend fun sync(update: DocumentSnapshot) {
        cachedDocHandle.sync(update)
    }

    override suspend fun delete() {
        TODO("Not yet implemented")
    }
}
