package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

internal class TestFork : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testFork() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            tx.commit()
        }
        val doc2 = doc.fork()
        doc2.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 4.56
            tx.commit()
        }
        val result = doc[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
        val result2 = doc2[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(4.56, (result2 as KAmValue.F64).value)
    }

    @Test
    fun testForkWithActor() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            tx.commit()
        }
        val doc2 = doc.fork("actor2".toByteArray())
        val result = doc[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
        Assertions.assertArrayEquals("actor2".toByteArray(), doc2.actorId)
    }

    @Test
    fun testForkWhileTransactionInProgressThrows() {
        val doc = KDocument()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc.fork()
        }
    }

    @Test
    fun testForkWithActorWhileTransactionInProgressThrows() {
        val doc = KDocument()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc.fork("actor2".toByteArray())
        }
    }

    @Test
    fun testForkAt() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            tx.commit()
        }
        val heads = doc.heads
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 4.56
            tx.commit()
        }
        val doc2 = doc.fork(heads)
        val result = doc2[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
    }

    @Test
    fun testForkAtWithActor() {
        val doc = KDocument()
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 1.23
            tx.commit()
        }
        val heads = doc.heads
        doc.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key"] = 4.56
            tx.commit()
        }
        val doc2 = doc.fork(heads, "actor2".toByteArray())
        val result = doc2[KObjectId.Companion.ROOT, "key"]
        Assertions.assertEquals(1.23, (result as KAmValue.F64).value)
        Assertions.assertArrayEquals("actor2".toByteArray(), doc2.actorId)
    }

    @Test
    fun testForkAtWhileTransactionInProgressThrows() {
        val doc = KDocument()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc.fork(arrayOf())
        }
    }

    @Test
    fun testForkAtWithActorWhileTransactionInProgressThrows() {
        val doc = KDocument()
        val tx = doc.startTransaction()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc.fork(arrayOf(), "actor2".toByteArray())
        }
    }
}
