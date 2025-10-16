package dev.psegerfast.automergekotlin.core

interface KPatchLog {
    companion object

    fun free()
}

expect class KCursor

expect operator fun KPatchLog.Companion.invoke(): KPatchLog

expect fun KDocument.Companion.load(bytes: ByteArray): KDocument

expect operator fun KDocument.Companion.invoke(actorId: ByteArray? = null): KDocument

interface KDocument : KReadable {

    companion object

    /**
     * Get the actor ID for this document
     *
     * @return the actor ID for this document
     */
    val actorId: ByteArray

    /**
     * Free the memory associated with this document
     *
     * <p>
     * Once this method has been called any further attempts to interact with the
     * document will raise an exception. This call itself is idempotent though, so
     * it's safe to call mutliple times.
     */
    fun free()

    /**
     * Save a document
     *
     * <p>
     * The saved document can be loaded again with {@link load}
     *
     * @return The bytes of the saved document
     */
    fun save(): ByteArray

    /**
     * Create a copy of this document with a new random actor ID
     *
     * @return The new document
     */
    fun fork(): KDocument

    /**
     * Create a copy of this document with a new random actor ID
     *
     * @return The new document
     */
    fun fork(newActor: ByteArray): KDocument

    /**
     * Create a copy of this document as at the given heads
     *
     * @param heads
     *            The heads to fork the document at
     * @return The new document
     */
    fun fork(heads: Array<KChangeHash>): KDocument

    /**
     * Create a copy of this document as at the given heads with the given actor ID
     *
     * @param heads
     *            The heads to fork the document at
     * @param newActor
     *            The actor ID to use for the new document
     * @return The new document
     */
    fun fork(heads: Array<KChangeHash>, newActor: ByteArray): KDocument

    /**
     * Merge another document into this one
     *
     * @param other
     *            The document to merge into this one
     * @throws TransactionInProgress
     *             if there is a transaction in progress on this document or on the
     *             other document
     */
    fun merge(other: KDocument)

    /**
     * Merge another document into this one logging patches
     *
     * @param other
     * The document to merge into this one
     * @param patchLog
     * The patch log in which to record any changes to the current state
     * which occur as a result of the merge
     * @throws TransactionInProgress
     * if there is a transaction in progress on this document or on the
     * other document
     */
    fun merge(other: KDocument, patchLog: KPatchLog)

    /**
     * Encode changes since the given heads
     *
     * The encoded changes this method returns can be used in
     * [applyEncodedChanges]
     *
     * @param heads
     * The heads to encode changes since
     * @return The encoded changes
     */
    fun encodeChangesSince(heads: Array<KChangeHash>): ByteArray

    /**
     * Incorporate changes from another document into this document
     *
     * @param changes
     * The changes to incorporate. Produced by [encodeChangesSince]
     * or [save]
     * @throws TransactionInProgress
     * if a transaction is in progress
     * @throws AutomergeException
     * if the changes are not valid
     */
    fun applyEncodedChanges(changes: ByteArray)

    /**
     * The same as [applyEncodedChanges] but logs any changes to the current
     * state that result from applying the change in the given patch log
     *
     * Creating patches does imply a performance penalty, so if you don't need them
     * you should use [applyEncodedChanges]
     *
     * @param changes
     * The changes to incorporate. Produced by [encodeChangesSince]
     * or [save]
     * @param patchLog
     * The patch log in which to record any changes to the current state
     * @throws TransactionInProgress
     * if a transaction is in progress
     * @throws AutomergeException
     * if the changes are not valid
     */
    fun applyEncodedChanges(changes: ByteArray, patchLog: KPatchLog)

    /**
     * Start a transaction to change this document
     *
     * <p>
     * There can only be one active transaction per document. Any method which
     * mutates the document (e.g. {@link merge} or {@link receiveSyncMessage} will
     * throw an exception if a transaction is in progress. Therefore keep
     * transactions short lived.
     *
     * @return a new transaction
     * @throws TransactionInProgress
     *             if a transaction is already in progress
     */
    fun startTransaction(): KTransaction

    /**
     * Start a transaction to change this document which logs changes in a patch log
     *
     * <p>
     * There can only be one active transaction per document. Any method which
     * mutates the document (e.g. {@link merge} or {@link receiveSyncMessage} will
     * throw an exception if a transaction is in progress. Therefore keep
     * transactions short lived.
     *
     * @param patchLog
     *            the {@link PatchLog} to log changes to
     * @return a new transaction
     * @throws TransactionInProgress
     *             if a transaction is already in progress
     */
    fun startTransaction(patchLog: KPatchLog): KTransaction

    /**
     * Start a transaction to change this document based on the document at a given
     * heads
     *
     * <p>
     * There can only be one active transaction per document. Any method which
     * mutates the document (e.g. {@link merge} or {@link receiveSyncMessage} will
     * throw an exception if a transaction is in progress. Therefore keep
     * transactions short lived.
     *
     * @param patchLog
     *            the {@link PatchLog} to log changes to. Note that the the changes
     *            logged here will represent changes from the state as at the given
     *            heads, not the state of the document when calling this method.
     * @param heads
     *            the heads to begin the transaction at
     *
     * @return a new transaction
     * @throws TransactionInProgress
     *             if a transaction is already in progress
     */
    fun startTransactionAt(patchLog: KPatchLog, heads: Array<KChangeHash>): KTransaction

    /**
     * Generate a sync message
     *
     * @param syncState
     *            the {@link SyncState} for the connection you are syncing with
     * @return the sync message to send to the other side, or null
     *         if there is nothing to send
     */
    fun generateSyncMessage(syncState: KSyncState): ByteArray?

    /**
     * Applies a sync message to the document.
     *
     * <p>
     * If you need to know what changes happened as a result of the message use
     * {@link receiveSyncMessage(SyncState,PatchLog,ByteArray)} instead.
     *
     * @param syncState
     *            the {@link SyncState} for the connection you are syncing with
     * @param message
     *            The sync message to apply.
     * @throws TransactionInProgress
     *             if a transaction is already in progress
     */
    fun receiveSyncMessage(syncState: KSyncState, message: ByteArray)

    /**
     * Applies a sync message to the document logging any changes in a PatchLog.
     *
     * @param syncState
     *            the {@link SyncState} for the connection you are syncing with
     * @param patchLog
     *            the {@link PatchLog} to log changes to
     * @param message
     *            The sync message to apply.
     * @throws TransactionInProgress
     *             if a transaction is already in progress
     */
    fun receiveSyncMessage(syncState: KSyncState, patchLog: KPatchLog, message: ByteArray)

    fun makePatches(patchLog: KPatchLog): List<KPatch>

    /**
     * Return the patches that would be required to modify the state at `before` to
     * become the state at `after`
     *
     * @param before
     *            The heads of the state to start from
     * @param after
     *            The heads of the state to end at
     * @return The patches required to transform the state at `before` to the state
     *         at `after`
     */
    fun diff(before: Array<KChangeHash>, after: Array<KChangeHash>): List<KPatch>

}