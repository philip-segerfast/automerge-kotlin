package dev.psegerfast.automergekotlin.core

import org.automerge.SyncState

fun SyncState.toCommon(): KSyncState = KSyncStateImpl(this)

actual fun KSyncState.Companion.decode(encoded: ByteArray): KSyncState =
    KSyncStateImpl(SyncState.decode(encoded))

actual operator fun KSyncState.Companion.invoke(): KSyncState = KSyncStateImpl()

internal class KSyncStateImpl(val javaSyncState: SyncState) : KSyncState {

    constructor() : this(SyncState())

    override fun free() {
        javaSyncState.free()
    }

    override fun isInSync(doc: KDocument): Boolean =
        javaSyncState.isInSync((doc as KDocumentImpl).document)

    override fun encode(): ByteArray = javaSyncState.encode()

}
