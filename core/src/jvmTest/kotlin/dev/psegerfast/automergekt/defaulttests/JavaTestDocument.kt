package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.TransactionInProgress
import org.junit.jupiter.api.Assertions

class JavaTestDocument : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testConstructWithActorId() {
        val doc = Document("actorId".toByteArray())
        Assertions.assertArrayEquals("actorId".toByteArray(), doc.actorId)
    }

    @Test
    fun startAndCommitTransaction() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            tx.commit()
        }
        val result = doc[ObjectId.ROOT, "key"]
        Assertions.assertEquals(1.23, (result.get() as AmValue.F64).value)
    }

    @Test
    fun testThrowsOnSaveWhileTransactionInProgress() {
        val doc = Document()
        val tx = doc.startTransaction()
        tx[ObjectId.ROOT, "key"] = 1.23
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc.save()
        }
    }

    @Test
    fun testSaveAndLoad() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            tx.commit()
        }
        val bytes = doc.save()
        val doc2 = Document.load(bytes)
        val result = doc2[ObjectId.ROOT, "key"]
        Assertions.assertEquals(1.23, (result.get() as AmValue.F64).value)
    }

    @Test
    fun testThrowsAutomergeExceptionOnInvalidBytes() {
        val badBytes = "badbytes".toByteArray()
        Assertions.assertThrows(
            RuntimeException::class.java // TODO - Should be AutomergeException but it's package-private.
        ) {
            Document.load(badBytes)
        }
    }

    @Test
    fun testFree() {
        val doc = Document()
        doc.free()
    }

    @Test
    fun testNullObjectIdThrows() {
        // this test is actually a test for any path which uses an `ObjectId`
        // as the code which throws the exception is in the rust implementation
        // which converts the `ObjectId` into an internal rust type
        val doc = Document()
        Assertions.assertThrows(
            IllegalArgumentException::class.java
        ) {
            doc[null, "key"]
        }
    }
}
