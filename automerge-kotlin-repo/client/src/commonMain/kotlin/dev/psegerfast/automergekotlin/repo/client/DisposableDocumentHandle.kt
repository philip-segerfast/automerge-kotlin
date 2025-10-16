package dev.psegerfast.automergekotlin.repo.client

import dev.psegerfast.automergekotlin.core.KChangeHash
import dev.psegerfast.automergekotlin.core.KWrite
import dev.psegerfast.automergekotlin.repo.shared.DocumentId
import kotlinx.coroutines.flow.StateFlow

interface DocumentHandle {
    var deleted: Boolean
    var disposed: Boolean

    val currentSnapshot: StateFlow<DocumentSnapshot?>
    val actorId: DocumentId
    suspend fun edit(editBlock: KWrite.() -> Unit): KChangeHash?
    suspend fun sync(update: DocumentSnapshot)
    suspend fun delete()
}

interface Disposable {
    fun dispose()
}

/** Client-facing handle to a document.proto. */
interface DisposableDocumentHandle : DocumentHandle, Disposable

class DocumentHandleState {
    // Connecting
    // Connected
    // Deleted
    //
}

suspend fun test(repo: DocumentRepository) {
    val handle = repo.getOrCreate(byteArrayOf(0))
    val data = handle.currentSnapshot

    // use handle. Dispose when you're done with it to free any resources.
    handle.dispose()
}

/*
| Feature                   | Passing Entire Repository  | Passing SocketAdapter   |
| ------------------------- | -------------------------- | ----------------------- |
| Coupling                  | High                       | Medium                  |
| Interface                 | Broad                      | Narrow                  |
| Testability               | Harder                     | Easier                  |
| Complexity                | Simpler                    | Slightly More Complex   |
| Redundancy                | None                       | None                    |
| Centralized Connection    | Yes                        | Yes                     |
| Abstraction               | Less                       | More                    |
| Code Size                 | Less                       | More                    |
| Flexibility               | Less                       | More                    |
 */
