package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.KWrite
import dev.psegerfast.automergekotlin.core.invoke
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryDocumentRepository : DocumentRepository {
    private val documents = mutableMapOf<DocumentId, KDocument>()
    private val rootHandles = mutableMapOf<DocumentId, MemoryDocHandle>()
    private val childHandles = mutableMapOf<DocumentId, MutableList<LogicalDocumentHandle>>()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val mutex = Mutex()

    // Notes
    // We need to notify MainDocumentRepository when a document.proto is deleted so that it can be deleted
    // in other repositories too.
    // Solutions:
    //  Have some event bus and detect events from outside.
    //  Have a callback function.

    override suspend fun init() = Unit // No-op

    override fun get(documentId: DocumentId): DisposableDocumentHandle? {
        val root = rootHandles[documentId]
        root?.let {
            LogicalDocumentHandle(
                physicalDocHandle = root,
                onDisposed = ::onChildDisposed
            )
        }

        return documents[documentId]?.let { createPhysicalDocHandle(documentId) }
    }

    private fun createPhysicalDocHandle(documentId: DocumentId): MemoryDocHandle = MemoryDocHandle(
        actorId = documentId,
        onDisposed = { onRootHandleDisposed(documentId) },
        onDelete = {},
        documentProvider = { documents[documentId] },
    )

    private fun onRootHandleDisposed(documentId: DocumentId) {

    }

    /** A child handle was just disposed. */
    private fun onChildDisposed(child: LogicalDocumentHandle) {
        val actorId = child.actorId
        val job = scope.launch {
            mutex.withLock {
                val children = childHandles[actorId]!!
                children.remove(child)
            }
            onAfterChildDisposed(actorId)
        }
    }

    private fun onAfterChildDisposed(actorId: DocumentId) {
        // Check if there are no more children and dispose root if not
        val job = scope.launch {
            mutex.withLock {
                if(childHandles[actorId]?.isEmpty() == true) {
                    disposeRootHandle(actorId)
                }
            }
        }
    }

    private fun disposeRootHandle(actorId: DocumentId) {
        scope.launch {
            mutex.withLock {
                rootHandles[actorId]?.dispose()
            }
        }
    }

    override fun create(actorId: DocumentId): DisposableDocumentHandle {
        return createInternal(actorId)
    }

    override fun create(): DisposableDocumentHandle {
        return createInternal(null)
    }

    private fun createInternal(actorId: DocumentId?): DisposableDocumentHandle {
        // Create a new Document
        val document = actorId?.let { KDocument(it) } ?: KDocument()
        val documentId = document.actorId
        val docHandle = createPhysicalDocHandle(documentId)

        documents[documentId] = document
        rootHandles[documentId] = docHandle

        return docHandle
    }

    override fun getOrCreate(documentId: DocumentId): DisposableDocumentHandle {
        return get(documentId) ?: create(documentId)
    }

    override suspend fun delete(documentId: DocumentId): Boolean {
        // Delete document.proto from database
        var removed = false
        rootHandles.remove(documentId)
            ?.onDelete?.invoke()
            ?.also { removed = true }
        return removed
    }

}

private class MemoryDocHandle(
    override val actorId: DocumentId,
    val onDisposed: () -> Unit,
    val onDelete: suspend () -> Unit,
    val documentProvider: () -> KDocument?,
) : DisposableDocumentHandle {
    override var deleted: Boolean = false
    override var disposed: Boolean = false

    private val _currentSnapshot = MutableStateFlow<DocumentSnapshot?>(null)
    override val currentSnapshot: StateFlow<DocumentSnapshot?> = _currentSnapshot.asStateFlow()

    private val docMutex = Mutex()

    private fun log(msg: String, tag: String) {
        println("[InMemoryDocHandle($actorId)#$tag] $msg")
    }

    @Throws(DocumentDeletedException::class)
    private fun requireDocument(): KDocument {
        return documentProvider() ?: throw DocumentDeletedException("Document with actorId $actorId was deleted.")
    }

    override suspend fun edit(editBlock: KWrite.() -> Unit): KChangeHash? {
        val tag = "edit"
        log("waiting for lock...", tag)
        docMutex.lock()
        log("lock acquired", tag)
        val document = requireDocument()
        val changeHash = document.startTransaction().also { it.editBlock() }.commit()
        emitChanges(document)
        docMutex.unlock()
        return changeHash
    }

    suspend fun emitChanges(document: KDocument) {
        _currentSnapshot.emit(DocumentSnapshotImpl(document))
    }

    override suspend fun sync(update: DocumentSnapshot) {
        if(disposed || deleted) error("Disposed or deleted!")
        val tag = "sync($update)"
        log("waiting for lock...", tag)
        docMutex.withLock {
            log("Lock acquired", tag)
            // You should be able to merge a DocumentSnapshot into a KDocument
            val doc = requireDocument()
            val changes = update.encodeChangesSince(doc.heads)
            doc.applyEncodedChanges(changes)
            emitChanges(doc)
        }
    }

    override suspend fun delete() {
        if(deleted) return
        // Change state to DOCUMENT_DELETED or something
        log("invoke", "delete()")
        _currentSnapshot.value = null
        deleted = true
        onDelete()
        dispose()
    }

    override fun dispose() {
        if(disposed) return
        log("invoke()", "dispose()")
        disposed = true
        onDisposed()
    }
}
