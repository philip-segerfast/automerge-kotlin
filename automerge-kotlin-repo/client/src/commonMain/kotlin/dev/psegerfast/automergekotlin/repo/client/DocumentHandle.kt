package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.repo.shared.classLogger

class DocumentHandleAlreadyDisposedException(message: String) : Exception(message)

internal class LogicalDocumentHandle(
    private val physicalDocHandle: DocumentHandle,
    private val onDisposed: (LogicalDocumentHandle) -> Unit,
) : DisposableDocumentHandle, DocumentHandle by physicalDocHandle {
    private val logger = classLogger(this::class.simpleName!!)

    override fun dispose() {
        if(disposed) throw DocumentHandleAlreadyDisposedException("Document handle with ID ${physicalDocHandle.actorId} already disposed.")
        logger.logMethod("dispose()")
        disposed = true
        onDisposed(this)
    }

    /** When this document.proto has been deleted externally. */
    suspend fun onDeleted() {
        logger.logMethod("onDeleted()")
        deleted = true
        dispose()
    }
}