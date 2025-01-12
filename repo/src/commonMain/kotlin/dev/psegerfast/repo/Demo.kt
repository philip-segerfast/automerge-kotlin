package dev.psegerfast.repo

import dev.psegerfast.automergekotlin.core.KAmValue
import dev.psegerfast.automergekotlin.core.KDocument
import dev.psegerfast.automergekotlin.core.KObjectId
import dev.psegerfast.automergekotlin.core.KSyncState
import dev.psegerfast.automergekotlin.core.invoke
import dev.psegerfast.automergekotlin.core.load
import dev.psegerfast.automergekotlin.core.stringValue
import dev.psegerfast.automergekotlin.core.treeString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {

    val doc1 = KDocument()
    doc1.startTransaction().also { tx ->
        tx[KObjectId.ROOT, "text"] = "Hello World!"
    }.commit()

    val syncState1 = KSyncState()
    val doc1Bytes = doc1.save()

    println("Doc: ${ doc1.treeString() }")

    val doc2 = KDocument.load(doc1Bytes)

    println("Doc2: ${doc2.treeString()}")

}

// TODO - find some way to pass a Document.
abstract class TypedDocument(document: KDocument) {
    private val delegator = DocumentDelegator(document)

    abstract fun free()

    protected fun textReference(
        objectId: KObjectId = KObjectId.Companion.ROOT,
        key: String,
    ): ElementRef.Text {
        // Type is KAmValue.Text
        TODO()
    }

    init {
//        val docRef: GroupNotesDocumentReference = documentReference(
//            connection = rsocketDocumentRepository() OR peerToPeerConnection OR localConnection,
//            localDocument = localDocumentRepository(),
//        )

        GlobalScope.launch {
//            // Atomic operation
//            docRef.text.writeAtomic("Hello this is some text!")
//
//            // Write a value and suspend until is has been fully persisted
//            docRef.text.writeSuspending("Some more text...")
//
//            // Collect changes for a particular field
//            docRef.text.collectValue { text ->
//
//            }
//
//            // Collect entire document updates.
//            docRef.collectChanges { document ->
//
//            }
//
//            val doc: KDocument = null!!
//            doc.merge(other)
//            doc.applyEncodedChanges(changes: ByteArray)
//            val patches = doc.makePatches(patches: KPatchLog)
//
//            val syncState = KSyncState()
//            val syncMessageBytes = doc.generateSyncMessage(syncState)
//            doc.receiveSyncMessage(syncState, syncMessageBytes!!)

        }
    }
}

sealed interface DocumentUpdate {
    data class Merge(val other: KDocument) : DocumentUpdate
    data class ApplyEncodedChanges(val changes: ByteArray) : DocumentUpdate
}

typealias ActorId = ByteArray

//interface DocumentRepository {
//    /** Get document, either from a remote connection or local storage. */
//    suspend fun getDocument(actorId: ActorId): KDocument
//    /** Save document to a remote connection or local storage. */
//    suspend fun saveDocument(document: KDocument)
//    suspend fun getChangesSince(actorId: ByteArray, version: DocumentVersion) : DocumentChanges {
//        TODO()
//    }
//}

class DocumentChanges

//class LocalDocumentRepository : DocumentRepository {
//
//}
//
//class RSocketDocumentSource(rsocket: RSocket) : DocumentRepository {
//
//}

//class GroupNotesDocumentReference(
//    private val connection: DocumentConnection,
//    private val localDocumentRepository: LocalDocumentRepository,
//) : TypedDocument() {
//
//    private lateinit var document: KDocument
//
//    private val textNodeId = "text"
//
//    private val delegator = DocumentDelegator(document)
//
//    val text: ElementRef.Text = textReference()
//
//    init {
//        val value = document[KObjectId.ROOT, textNodeId] as KAmValue.Text
//        value.stringValue()
//    }
//
//    override fun free() {
//        document.free()
//    }
//
//}

class DocumentDelegator(private val document: KDocument) {
    fun textRef(objectId: KObjectId, key: String) : ElementRef.Text {
//            return (document.get(objectId, key) as KAmValue.Text).stringValue()

        ElementRef.Text(
            reader = {
                (document[objectId, key] as KAmValue.Text).stringValue()
            },
            writer = { value ->
                document.startTransaction().also {
                    it[KObjectId.Companion.ROOT, key] = value
                    it.commit()
                }
            }
        )

        TODO()
    }

    fun text(objectId: KObjectId, index: Int) {

    }
}

sealed interface ElementRef {

    class Text(
        reader: () -> String,
        writer: (value: String) -> Unit,
    ) : ElementRef {
        fun read(): String? {
            TODO()
        }
    }

}