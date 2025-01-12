package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

class TestDocument : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testConstructWithActorId() {
        val doc = KDocument("actorId".toByteArray())
        Assertions.assertArrayEquals("actorId".toByteArray(), doc.actorId)
    }

    @Test
    fun startAndCommitTransaction() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            tx.commit()
        }
        val result = doc[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
    }

    @Test
    fun testThrowsOnSaveWhileTransactionInProgress() {
        val doc = KDocument()
        val tx = doc.startTransaction()
        tx[KObjectId.Companion.ROOT, "key"] = 1.23
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc.save()
        }
    }

    @Test
    fun testSaveAndLoad() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            tx.commit()
        }
        val bytes = doc.save()
        val doc2 = KDocument.load(bytes)
        val result = doc2[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
    }

    @Test
    fun testThrowsAutomergeExceptionOnInvalidBytes() {
        val badBytes = "badbytes".toByteArray()
        Assertions.assertThrows(
            RuntimeException::class.java // TODO - Should be AutomergeException but it's package-private.
        ) {
            KDocument.load(badBytes)
        }
    }

    @Test
    fun testFree() {
        val doc = KDocument()
        doc.free()
    }
}
