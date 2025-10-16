package dev.psegerfast.automergekotlin.core

/**
 * Decode a previously encoded sync state
 *
 * @param encoded
 *            The encoded sync state
 * @return The decoded sync state
 * @throws AutomergeException
 *             if the encoded sync state is not valid
 */
expect fun KSyncState.Companion.decode(encoded: ByteArray): KSyncState

expect operator fun KSyncState.Companion.invoke(): KSyncState

interface KSyncState {
    /**
     * Encode the sync state for storage
     *
     * @return The encoded sync state
     */
    fun encode(): ByteArray

    /** Free the memory associated with this sync state */
    fun free()

    /**
     * Whether the other end of this sync connection is in sync with `doc`
     *
     * @param doc
     *            The document to check we are in sync with
     * @return Whether we are in sync
     */
    fun isInSync(doc: KDocument): Boolean

    companion object
}
