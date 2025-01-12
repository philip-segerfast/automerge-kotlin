package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.TransactionInProgress
import org.junit.jupiter.api.Assertions

internal class JavaTestFork : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testFork() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            tx.commit()
        }
        val doc2 = doc.fork()
        doc2.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 4.56
            tx.commit()
        }
        val result = doc[ObjectId.ROOT, "key"]
        Assertions.assertEquals(1.23, (result.get() as AmValue.F64).value)
        val result2 = doc2[ObjectId.ROOT, "key"]
        Assertions.assertEquals(4.56, (result2.get() as AmValue.F64).value)
    }

    @Test
    fun testForkWithActor() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            tx.commit()
        }
        val doc2 = doc.fork("actor2".toByteArray())
        val result = doc[ObjectId.ROOT, "key"]
        Assertions.assertEquals(1.23, (result.get() as AmValue.F64).value)
        Assertions.assertArrayEquals("actor2".toByteArray(), doc2.actorId)
    }

    @Test
    fun testForkWhileTransactionInProgressThrows() {
        val doc = Document()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc.fork()
        }
    }

    @Test
    fun testForkWithActorWhileTransactionInProgressThrows() {
        val doc = Document()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc.fork("actor2".toByteArray())
        }
    }

    @Test
    fun testForkAt() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            tx.commit()
        }
        val heads = doc.heads
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 4.56
            tx.commit()
        }
        val doc2 = doc.fork(heads)
        val result = doc2[ObjectId.ROOT, "key"]
        Assertions.assertEquals(1.23, (result.get() as AmValue.F64).value)
    }

    @Test
    fun testForkAtWithActor() {
        val doc = Document()
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 1.23
            tx.commit()
        }
        val heads = doc.heads
        doc.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key"] = 4.56
            tx.commit()
        }
        val doc2 = doc.fork(heads, "actor2".toByteArray())
        val result = doc2[ObjectId.ROOT, "key"]
        Assertions.assertEquals(1.23, (result.get() as AmValue.F64).value)
        Assertions.assertArrayEquals("actor2".toByteArray(), doc2.actorId)
    }

    @Test
    fun testForkAtWhileTransactionInProgressThrows() {
        val doc = Document()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc.fork(arrayOf())
        }
    }

    @Test
    fun testForkAtWithActorWhileTransactionInProgressThrows() {
        val doc = Document()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc.fork(arrayOf(), "actor2".toByteArray())
        }
    }
}
