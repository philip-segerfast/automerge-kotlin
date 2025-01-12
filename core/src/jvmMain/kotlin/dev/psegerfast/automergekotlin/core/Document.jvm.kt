package dev.psegerfast.automergekotlin.core

import org.automerge.Cursor
import org.automerge.PatchLog
import kotlin.jvm.optionals.getOrNull
import org.automerge.Document as JavaDocument

typealias PlatformDocument = JavaDocument

@JvmInline
value class KPatchLogImpl(val value: PatchLog): KPatchLog {
    override fun free() = value.free()
}

actual typealias KCursor = Cursor

internal fun JavaDocument.toCommon(): KDocument = KDocumentImpl(this)

actual operator fun KPatchLog.Companion.invoke(): KPatchLog = KPatchLogImpl(PatchLog())

actual operator fun KDocument.Companion.invoke(): KDocument = KDocumentImpl()

actual operator fun KDocument.Companion.invoke(actorId: ByteArray): KDocument = KDocumentImpl(actorId)

actual fun KDocument.Companion.load(bytes: ByteArray): KDocument = KDocumentImpl.load(bytes)

internal class KDocumentImpl internal constructor(
    internal val document: JavaDocument
) : KDocument, KRead by KReadDelegate(document) {

    companion object {
        fun load(bytes: ByteArray): KDocument = JavaDocument.load(bytes).toCommon()
    }

    constructor() : this(PlatformDocument())

    constructor(actorId: ByteArray) : this(PlatformDocument(actorId))

    override val actorId: ByteArray get() = document.actorId

    override fun free() =
        document.free()

    override fun save(): ByteArray =
        document.save()

    override fun fork(): KDocument =
        document.fork().toCommon()

    override fun fork(newActor: ByteArray): KDocument =
        document.fork(newActor).toCommon()

    override fun fork(heads: Array<KChangeHash>) =
        document.fork(heads.toJava()).toCommon()

    override fun fork(heads: Array<KChangeHash>, newActor: ByteArray) =
        document.fork(heads.toJava(), newActor).toCommon()

    override fun merge(other: KDocument) =
        document.merge((other as KDocumentImpl).document)

    override fun merge(other: KDocument, patchLog: KPatchLog) =
        document.merge((other as KDocumentImpl).document, (patchLog as KPatchLogImpl).value)

    override fun encodeChangesSince(heads: Array<KChangeHash>): ByteArray =
        document.encodeChangesSince(heads.toJava())

    override fun applyEncodedChanges(changes: ByteArray) =
        document.applyEncodedChanges(changes)

    override fun applyEncodedChanges(changes: ByteArray, patchLog: KPatchLog) =
        document.applyEncodedChanges(changes, (patchLog as KPatchLogImpl).value)

    override fun startTransaction(): KTransaction =
        document.startTransaction().toCommon()

    override fun startTransaction(patchLog: KPatchLog): KTransaction =
        document.startTransaction((patchLog as KPatchLogImpl).value).toCommon()

    override fun startTransactionAt(patchLog: KPatchLog, heads: Array<KChangeHash>): KTransaction =
        document.startTransactionAt((patchLog as KPatchLogImpl).value, heads.toJava()).toCommon()

    override fun generateSyncMessage(syncState: KSyncState): ByteArray? =
        document.generateSyncMessage((syncState as KSyncStateImpl).javaSyncState).getOrNull()

    override fun receiveSyncMessage(syncState: KSyncState, message: ByteArray) =
        document.receiveSyncMessage((syncState as KSyncStateImpl).javaSyncState, message)

    override fun receiveSyncMessage(syncState: KSyncState, patchLog: KPatchLog, message: ByteArray) =
        document.receiveSyncMessage((syncState as KSyncStateImpl).javaSyncState, (patchLog as KPatchLogImpl).value, message)

    override fun makePatches(patchLog: KPatchLog): List<KPatch> =
        document
            .makePatches((patchLog as KPatchLogImpl).value)
            .map { patch ->
                KPatch(
                    objectId = patch.obj.toCommon(),
                    path = patch.path.map { it.toCommon() },
                    action = patch.action.toCommon(),
                )
            }

    override fun diff(before: Array<KChangeHash>, after: Array<KChangeHash>): List<KPatch> =
        document
            .diff(
                before.map { (it as KChangeHashImpl).value }.toTypedArray(),
                after.map { (it as KChangeHashImpl).value }.toTypedArray()
            )
            .map { it.toCommon() }

}
