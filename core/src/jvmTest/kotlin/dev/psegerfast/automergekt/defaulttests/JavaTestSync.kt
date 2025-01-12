package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.Patch
import org.automerge.PatchAction.PutMap
import org.automerge.PatchLog
import org.automerge.SyncState
import org.automerge.TransactionInProgress
import org.junit.jupiter.api.Assertions

internal class JavaTestSync : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testSync() {
        val doc1 = Document()
        doc1.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value"
            tx.commit()
        }
        val doc2 = doc1.fork()
        doc2.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key2"] = "value2"
            tx.commit()
        }
        doc1.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key3"] = "value3"
            tx.commit()
        }
        sync(doc1, doc2)

        Assertions.assertEquals("value", (doc1[ObjectId.ROOT, "key"].get() as AmValue.Str).value)
        Assertions.assertEquals("value2", (doc1[ObjectId.ROOT, "key2"].get() as AmValue.Str).value)
        Assertions.assertEquals("value3", (doc1[ObjectId.ROOT, "key3"].get() as AmValue.Str).value)
        Assertions.assertEquals("value", (doc2[ObjectId.ROOT, "key"].get() as AmValue.Str).value)
        Assertions.assertEquals("value2", (doc2[ObjectId.ROOT, "key2"].get() as AmValue.Str).value)
        Assertions.assertEquals("value3", (doc2[ObjectId.ROOT, "key3"].get() as AmValue.Str).value)
    }

    @Test
    fun testGenerateSyncMessageThrowsInTransaction() {
        val doc1 = Document()
        val tx = doc1.startTransaction()
        val syncState = SyncState()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc1.generateSyncMessage(syncState)
        }
    }

//    @Test
//    fun testRecieveSyncMessageThrowsInTransaction() {
//        val doc1 = Document()
//        doc1.startTransaction().use { tx ->
//            tx[ObjectId.ROOT, "key"] = "value"
//            tx.commit()
//        }
//        val syncState = SyncState()
//        val message = syncState.generateSyncMessage(doc1).get()
//        val doc2 = Document()
//        val tx = doc2.startTransaction()
//        val syncState2 = SyncState()
//        Assertions.assertThrows(
//            TransactionInProgress::class.java
//        ) {
//            doc2.receiveSyncMessage(syncState2, message)
//        }
//    }

    @Test
    fun testEncodeDecodeSyncState() {
        val state = SyncState()
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value"
            tx.commit()
        }
        val message = doc.generateSyncMessage(state).get()
        val encodedState = state.encode()
        val state2 = SyncState.decode(encodedState)
        val message2 = doc.generateSyncMessage(state2).get()
        Assertions.assertArrayEquals(message, message2)
    }

    @Test
    fun testFree() {
        val state = SyncState()
        state.free()
    }

    fun sync(docA: Document, docB: Document) {
        val atob = SyncState()
        val btoa = SyncState()
        sync(atob, btoa, docA, docB)
    }

    fun sync(atob: SyncState?, btoa: SyncState?, docA: Document, docB: Document) {
        var iterations = 0
        while (true) {
            val message1 = docA.generateSyncMessage(atob)
            if (message1.isPresent) {
                docB.receiveSyncMessage(btoa, message1.get())
            }
            val message2 = docB.generateSyncMessage(btoa)
            if (message2.isPresent) {
                docA.receiveSyncMessage(atob, message2.get())
            }
            if (!message1.isPresent && !message2.isPresent) {
                break
            }
            iterations += 1
            if (iterations >= 10) {
                throw RuntimeException("Sync failed to converge")
            }
        }
    }

    @Test
    fun testSyncForPatches() {
        val doc1 = Document()
        doc1.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value"
            tx.commit()
        }
        val doc2 = Document()

        val patches = syncForPatches(doc1, doc2)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.path, ArrayList<Any>())
        Assertions.assertEquals(patch.obj, ObjectId.ROOT)
        val action = patch.action as PutMap
        Assertions.assertEquals(action.key, "key")
        Assertions.assertEquals((action.value as AmValue.Str).value, "value")
    }

    // Run receiveSyncMessageForPatches on docB and return all patches that were
    // generated
    fun syncForPatches(docA: Document, docB: Document): List<Patch> {
        val atob = SyncState()
        val btoa = SyncState()
        val patchLog = PatchLog()
        var iterations = 0
        while (true) {
            val message1 = docA.generateSyncMessage(atob)
            if (message1.isPresent) {
                docB.receiveSyncMessage(btoa, patchLog, message1.get())
            }
            val message2 = docB.generateSyncMessage(btoa)
            if (message2.isPresent) {
                docA.receiveSyncMessage(atob, message2.get())
            }
            if (!message1.isPresent && !message2.isPresent) {
                break
            }
            iterations += 1
            if (iterations >= 10) {
                throw RuntimeException("Sync failed to converge")
            }
        }
        return docB.makePatches(patchLog)
    }

    @Test
    fun testInSync() {
        val doc1 = Document()
        doc1.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = "value"
            tx.commit()
        }
        val doc2 = doc1.fork()
        doc2.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key2"] = "value2"
            tx.commit()
        }
        val atob = SyncState()
        val btoa = SyncState()
        Assertions.assertFalse(atob.isInSync(doc1))
        Assertions.assertFalse(btoa.isInSync(doc2))

        sync(atob, btoa, doc1, doc2)
        Assertions.assertTrue(atob.isInSync(doc1))
        Assertions.assertTrue(btoa.isInSync(doc2))
    }
}
