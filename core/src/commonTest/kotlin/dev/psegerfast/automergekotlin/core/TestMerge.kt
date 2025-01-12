package dev.psegerfast.automergekotlin.core

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions

internal class TestMerge : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestKotlin { super.rootTests() }

    @Test
    fun testMerge() {
        val doc1 = KDocument()
        doc1.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key1"] = 1.23
            tx.commit()
        }
        val doc2 = KDocument()
        doc2.startTransaction().use { tx ->
            tx[KObjectId.Companion.ROOT, "key2"] = 4.56
            tx.commit()
        }
        doc1.merge(doc2)
        Assertions.assertEquals(1.23, (doc1[KObjectId.Companion.ROOT, "key1"] as KAmValue.F64).value)
        Assertions.assertEquals(4.56, (doc1[KObjectId.Companion.ROOT, "key2"] as KAmValue.F64).value)
    }

    @Test
    fun testMergeThrowsIfTransactionInProgress() {
        val doc1 = KDocument()
        doc1.startTransaction()
        val doc2 = KDocument()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc1.merge(doc2)
        }
    }

    @Test
    fun testMergeThrowsIfOtherTransactionInProgress() {
        val doc1 = KDocument()
        val doc2 = KDocument()
        doc2.startTransaction()
        Assertions.assertThrows(
            TransactionInProgressException::class.java
        ) {
            doc1.merge(doc2)
        }
    }
}
