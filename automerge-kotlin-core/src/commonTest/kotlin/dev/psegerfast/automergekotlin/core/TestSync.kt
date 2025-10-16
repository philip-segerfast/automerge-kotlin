package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

internal class TestSync : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testSync() {
        val doc1 = KDocument()
        doc1.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value"
            tx.commit()
        }
        val doc2 = doc1.fork()
        doc2.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key2"] = "value2"
            tx.commit()
        }
        doc1.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key3"] = "value3"
            tx.commit()
        }
        sync(doc1, doc2)

        Assertions.assertEquals("value", (doc1[KObjectId.Companion.ROOT, "key"] as KAmValue.Str).value)
        Assertions.assertEquals("value2", (doc1[KObjectId.Companion.ROOT, "key2"] as KAmValue.Str).value)
        Assertions.assertEquals("value3", (doc1[KObjectId.Companion.ROOT, "key3"] as KAmValue.Str).value)
        Assertions.assertEquals("value", (doc2[KObjectId.Companion.ROOT, "key"] as KAmValue.Str).value)
        Assertions.assertEquals("value2", (doc2[KObjectId.Companion.ROOT, "key2"] as KAmValue.Str).value)
        Assertions.assertEquals("value3", (doc2[KObjectId.Companion.ROOT, "key3"] as KAmValue.Str).value)
    }

    @Test
    fun testGenerateSyncMessageThrowsInTransaction() {
        val doc1 = KDocument()
        val tx = doc1.startTransaction()
        val syncState = KSyncState()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc1.generateSyncMessage(syncState)
        }
    }

//    @Test
//    fun testRecieveSyncMessageThrowsInTransaction() {
//        val doc1 = KDocument()
//        doc1.startTransaction().use { tx ->
//            tx[KObjectId.ROOT, "key"] = "value"
//            tx.commit()
//        }
//        val syncState = KSyncState()
//        val message = syncState.generateSyncMessage(doc1)
//        val doc2 = KDocument()
//        val tx = doc2.startTransaction()
//        val syncState2 = KSyncState()
//        Assertions.assertThrows(
//            TransactionInProgressException::class.java
//        ) {
//            doc2.receiveSyncMessage(syncState2, message)
//        }
//    }

    @Test
    fun testEncodeDecodeSyncState() {
        val state = KSyncState()
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value"
            tx.commit()
        }
        val message = doc.generateSyncMessage(state)
        val encodedState = state.encode()
        val state2 = KSyncState.decode(encodedState)
        val message2 = doc.generateSyncMessage(state2)
        Assertions.assertArrayEquals(message, message2)
    }

    @Test
    fun testFree() {
        val state = KSyncState()
        state.free()
    }

    fun sync(docA: KDocument, docB: KDocument) {
        val atob = KSyncState()
        val btoa = KSyncState()
        sync(atob, btoa, docA, docB)
    }

    fun sync(atob: KSyncState, btoa: KSyncState, docA: KDocument, docB: KDocument) {
        var iterations = 0
        while (true) {
            val message1 = docA.generateSyncMessage(atob)
            if (message1 != null) {
                docB.receiveSyncMessage(btoa, message1)
            }
            val message2 = docB.generateSyncMessage(btoa)
            if (message2 != null) {
                docA.receiveSyncMessage(atob, message2)
            }
            if (message1 == null && message2 == null) {
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
        val doc1 = KDocument()
        doc1.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value"
            tx.commit()
        }
        val doc2 = KDocument()

        val patches = syncForPatches(doc1, doc2)

        Assertions.assertEquals(patches.size, 1)
        val patch = patches[0]
        Assertions.assertEquals(patch.path, ArrayList<Any>())
        Assertions.assertEquals(patch.objectId, KObjectId.Companion.ROOT)
        val action = patch.action as KPatchAction.PutMap
        Assertions.assertEquals(action.key, "key")
        Assertions.assertEquals((action.value as KAmValue.Str).value, "value")
    }

    // Run receiveSyncMessageForPatches on docB and return all patches that were
    // generated
    fun syncForPatches(docA: KDocument, docB: KDocument): List<KPatch> {
        val atob = KSyncState()
        val btoa = KSyncState()
        val patchLog = KPatchLog()
        var iterations = 0
        while (true) {
            val message1 = docA.generateSyncMessage(atob)
            if (message1 != null) {
                docB.receiveSyncMessage(btoa, patchLog, message1)
            }
            val message2 = docB.generateSyncMessage(btoa)
            if (message2 != null) {
                docA.receiveSyncMessage(atob, message2)
            }
            if (message1 == null && message2 == null) {
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
        val doc1 = KDocument()
        doc1.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = "value"
            tx.commit()
        }
        val doc2 = doc1.fork()
        doc2.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key2"] = "value2"
            tx.commit()
        }
        val atob = KSyncState()
        val btoa = KSyncState()
        Assertions.assertFalse(atob.isInSync(doc1))
        Assertions.assertFalse(btoa.isInSync(doc2))

        sync(atob, btoa, doc1, doc2)
        Assertions.assertTrue(atob.isInSync(doc1))
        Assertions.assertTrue(btoa.isInSync(doc2))
    }
}
