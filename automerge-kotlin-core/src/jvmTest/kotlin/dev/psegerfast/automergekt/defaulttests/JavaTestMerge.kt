package dev.psegerfast.automergekt.defaulttests

import io.kotest.core.spec.RootTest
import io.kotest.core.spec.style.AnnotationSpec
import org.automerge.AmValue
import org.automerge.Document
import org.automerge.ObjectId
import org.automerge.TransactionInProgress
import org.junit.jupiter.api.Assertions

internal class JavaTestMerge : AnnotationSpec() {

    override fun rootTests(): List<RootTest> = takeIfShouldTestJava { super.rootTests() }

    @Test
    fun testMerge() {
        val doc1 = Document()
        doc1.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key1"] = 1.23
            tx.commit()
        }
        val doc2 = Document()
        doc2.startTransaction().use { tx ->
            tx[ObjectId.ROOT, "key2"] = 4.56
            tx.commit()
        }
        doc1.merge(doc2)
        Assertions.assertEquals(1.23, (doc1[ObjectId.ROOT, "key1"].get() as AmValue.F64).value)
        Assertions.assertEquals(4.56, (doc1[ObjectId.ROOT, "key2"].get() as AmValue.F64).value)
    }

    @Test
    fun testMergeThrowsIfTransactionInProgress() {
        val doc1 = Document()
        doc1.startTransaction()
        val doc2 = Document()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc1.merge(doc2)
        }
    }

    @Test
    fun testMergeThrowsIfOtherTransactionInProgress() {
        val doc1 = Document()
        val doc2 = Document()
        doc2.startTransaction()
        Assertions.assertThrows(
            TransactionInProgress::class.java
        ) {
            doc1.merge(doc2)
        }
    }
}
